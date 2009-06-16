package no.java.ems.dao.impl;

import no.java.ems.dao.BinaryDao;
import no.java.ems.dao.EventDao;
import no.java.ems.server.domain.Binary;
import no.java.ems.server.domain.Event;
import no.java.ems.server.domain.Room;
import static no.java.ems.server.f.ExternalV1F.intervalGetStart;
import static no.java.ems.server.f.ExternalV1F.intervalToPeriod;
import static no.java.ems.server.f.ExternalV1F.periodGetMinutes;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import fj.data.Option;
import static fj.data.Option.some;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
@Repository
public class JdbcTemplateEventDao extends AbstractDao implements EventDao {

    private static final String DELIMITER = ",";
    private final JdbcTemplate jdbcTemplate;
    private BinaryDao binaryDao;

    @Autowired
    public JdbcTemplateEventDao(JdbcTemplate jdbcTemplate, BinaryDao binaryDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.binaryDao = binaryDao;
    }

    public Event getEvent(String id) {
        return (Event)jdbcTemplate.queryForObject(
            "select * from event where id = ?",
                new Object[]{id},
                new int[]{Types.VARCHAR},
                new EventMapper()
        );
    }

    public Event getEventByName(String name) {
        return (Event) DataAccessUtils.singleResult(jdbcTemplate.query(
            "select * from event where name = ?",
            new Object[]{name},
            new int[]{Types.VARCHAR},
            new EventMapper()
        ));
    }

    public ArrayList<Event> getEvents() {
        //noinspection unchecked
        return new ArrayList(jdbcTemplate.query("select * from event order by name", new EventMapper()));
    }

    public void saveEvent(Event event) {
        String updateQuery;
        if (event.getId() == null) {
            updateQuery = "insert into event(revision, name, eventdate, tags, notes, id) values(?, ?, ?, ?, ?, ?)";
            event.setId(UUID.randomUUID().toString());
            event.setRevision(1);
        } else {
            updateQuery = "update event set revision = ?, name = ?, eventdate = ?, tags = ?, notes = ? where id = ?";
            event.setRevision(event.getRevision() + 1);
        }
        jdbcTemplate.update(
                updateQuery,
                new Object[]{
                        event.getRevision(),
                        event.getName(),
                        toSqlDate(event.getDate()),
                        event.getTagsAsString(DELIMITER),
                        event.getNotes(),
                        event.getId()
                },
                new int[]{
                        Types.INTEGER,     // revision
                        Types.VARCHAR,     // name
                        Types.DATE,        // date
                        Types.LONGVARCHAR, // tags
                        Types.LONGVARCHAR, // notes
                        Types.VARCHAR      // id
                }
        );
        jdbcTemplate.update("delete from event_attachement where eventId = ?", new Object[]{event.getId()});
        List<Binary> attachements = event.getAttachments();
        for (int position = 0; position < attachements.size(); position++) {
            Binary attachement = attachements.get(position);
            jdbcTemplate.update(
                    "insert into event_attachement values (?, ?, ?)",
                    new Object[]{
                            event.getId(),
                            attachement.getId(),
                            position,
                    },
                    new int[]{
                            Types.VARCHAR,     // eventId
                            Types.VARCHAR,     // attachementId
                            Types.INTEGER,     // position
                    }
            );
        }

        jdbcTemplate.update("delete from event_room where eventId = ?", new Object[]{event.getId()});
        List<Room> rooms = event.getRooms();
        for (int position = 0; position < rooms.size(); position++) {
            Room room = rooms.get(position);
            jdbcTemplate.update(
                "insert into event_room values (?, ?, ?)",
                new Object[]{
                    event.getId(),
                    room.getId(),
                    position,
                },
                new int[]{
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.INTEGER,
                }
            );
        }

        jdbcTemplate.update("delete from event_timeslot where eventId = ?", new Object[]{event.getId()});
        List<Interval> timeslots = event.getTimeslots();
        for (int position = 0; position < timeslots.size(); position++) {
            Option<Interval> interval = some(timeslots.get(position));
            jdbcTemplate.update(
                "insert into event_timeslot values (?, ?, ?, ?)",
                new Object[]{
                    event.getId(),
                    position,
                    interval.map(intervalGetStart).map(dateTimeToSqlTimestamp).orSome((Timestamp) null),
                    interval.map(intervalToPeriod).map(periodGetMinutes).orSome(0),
                },
                new int[]{
                    Types.VARCHAR,
                    Types.INTEGER,
                    Types.TIMESTAMP,
                    Types.INTEGER,
                }
            );
        }
    }

    public void deleteEvent(String id) {
        //noinspection unchecked
        List<String> roomIds = jdbcTemplate.queryForList("select roomId from event_rootm where eventId= ? ",
            new Object[]{id});
        for (String roomId : roomIds) {
            jdbcTemplate.update("delete from room where roomId = ?", new Object[]{roomId});
        }
        jdbcTemplate.update("delete from event_room where eventId = ?", new Object[]{id});
        jdbcTemplate.update("delete from event_attachement where eventId = ?", new Object[]{id});
        jdbcTemplate.update("delete from event where id = ?", new Object[]{id}, new int[]{Types.VARCHAR});
    }

    private class EventMapper implements RowMapper {

        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Event event = new Event();
            event.setId(rs.getString("id"));
            event.setRevision(rs.getInt("revision"));
            event.setName(rs.getString("name"));
            final Date date = rs.getDate("eventdate");
            event.setDate(date == null ? null : LocalDate.fromDateFields(date));
            final String tags = rs.getString("tags");
            if (tags != null) {
                event.setTags(Arrays.asList(StringUtils.split(tags, DELIMITER)));
            }
            event.setNotes(rs.getString("notes"));

            //noinspection unchecked
            event.setAttachments(jdbcTemplate.query(
                            "select attachementId from event_attachement where eventId = ? order by position",
                            new Object[]{event.getId()},
                            new RowMapper() {
                                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    return binaryDao.getBinary(rs.getString("attachementId"));
                                }
                            }
                    )
            );

            //noinspection unchecked
            event.setRooms(
                jdbcTemplate.query(
                    "select r.* from room r, event_room er where er.eventId = ? and r.id = er.roomId order by er.position",
                    new Object[]{event.getId()},
                    new JdbcTemplateRoomDao.RoomMapper()
                )
            );

            //noinspection unchecked
            event.setTimeslots(
                jdbcTemplate.query(
                    "select * from event_timeslot where eventId = ? order by position",
                    new Object[]{event.getId()},
                    new RowMapper() {
                        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return mapInterval(rs).some();
                        }
                    }
                )
            );
            return event;
        }
    }
}
