package no.java.ems.dao.impl;

import no.java.ems.dao.BinaryDao;
import no.java.ems.dao.RoomDao;
import no.java.ems.dao.SessionDao;
import no.java.ems.domain.Binary;
import no.java.ems.domain.Language;
import no.java.ems.domain.Session;
import no.java.ems.domain.Speaker;
import org.joda.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JdbcTemplateSessionDao extends AbstractDao implements SessionDao {

    private static final String DELIMITER = ",";
    private JdbcTemplate jdbcTemplate;
    private BinaryDao binaryDao;
    private RoomDao roomDao;

    public JdbcTemplateSessionDao(JdbcTemplate jdbcTemplate, BinaryDao binaryDao, RoomDao roomDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.binaryDao = binaryDao;
        this.roomDao = roomDao;
    }

    public Session getSession(String id) {
        return (Session)jdbcTemplate.queryForObject(
                "select * from session where id = ?",
                new Object[]{id},
                new int[]{Types.VARCHAR},
                new SessionMapper()
        );
    }

    public List<Session> getSessions(String eventId) {
        //noinspection unchecked
        return jdbcTemplate.query(
                "select * from session where eventId = ? order by title",
                new Object[]{eventId},
                new int[]{Types.VARCHAR},
                new SessionMapper()
        );
    }

    public List<String> getSessionIdsByEventId(String eventId) {
        //noinspection unchecked
        return jdbcTemplate.query(
                "select id from session where eventid = ? order by title",
                new Object[]{eventId},
                new int[]{Types.VARCHAR},
                new RowMapper() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString("id");
                    }
                }
        );
    }

    public List<String> findSessionsByDate(String eventId, LocalDate date) {
        //noinspection unchecked
        return jdbcTemplate.query(
                "select id from session where eventid = ? and date(start) = ? order by title",
                new Object[]{
                        eventId,
                        toSqlDate(date)
                },
                new int[]{
                        Types.VARCHAR,
                        Types.DATE
                },
                new RowMapper() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString("id");
                    }
                }
        );
    }

    public List<String> findSessionsBySpeakerName(String eventId, String name) {
        // todo: implement
        System.err.println("JdbcTemplateSessionDao.findSessionsBySpeakerName: " + name);
        return Collections.emptyList();
    }

    public List<String> findSessionsByTitle(String eventId, String title) {
        //noinspection unchecked
        return jdbcTemplate.query(
                "select id from session where eventId = ? and title = ? order by title",
                new Object[]{
                        eventId,
                        title
                },
                new int[]{
                        Types.VARCHAR,
                        Types.VARCHAR
                },
                new RowMapper() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString("id");
                    }
                }
        );
    }

    public void saveSession(Session session) {
        String sql;
        if (session.getId() == null) {
            sql = "insert into session(revision, title, start, durationMinutes, state, roomId, level, format, tags, keywords, language, eventId, lead, body, notes, feedback, expected, outline, equipment, published, id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            session.setId(UUID.randomUUID().toString());
            session.setRevision(1);
        } else {
            sql = "update session set revision = ?, title = ?, start = ?, durationMinutes = ?, state = ?, roomId = ?, level = ?, format = ?, tags = ?, keywords = ?, language = ?, eventId = ?, lead = ?, body = ?, notes = ?, feedback = ?, expected = ?, outline = ?, equipment = ?, published = ? where id = ?";
            session.setRevision(session.getRevision() + 1);
        }
        jdbcTemplate.update(
                sql,
                new Object[]{
                        session.getRevision(),
                        session.getTitle(),
                        session.getTimeslot() != null ? toSqlTimestamp(session.getTimeslot().getStart()) : null,
                        session.getTimeslot() != null ? session.getTimeslot().toPeriod().getMinutes() : null,
                        session.getState().name(),
                        session.getRoom() != null ? session.getRoom().getId() : null,
                        session.getLevel() != null ? session.getLevel().name() : null,
                        session.getFormat() != null ? session.getFormat().name() : null,
                        session.getTagsAsString(DELIMITER),
                        session.getKeywordsAsString(DELIMITER),
                        session.getLanguage() != null ? session.getLanguage().getIsoCode() : null,
                        session.getEventId(),
                        session.getLead(),
                        session.getBody(),
                        session.getNotes(),
                        session.getFeedback(),
                        session.getExpectedAudience(),
                        session.getOutline(),
                        session.getEquipment(),
                        session.isPublished() ? "y" : "n",
                        session.getId()
                },
                new int[]{
                        Types.INTEGER,     // revision
                        Types.VARCHAR,     // title
                        Types.TIMESTAMP,   // start
                        Types.INTEGER,     // durationMinutes
                        Types.VARCHAR,     // state
                        Types.VARCHAR,     // room id
                        Types.VARCHAR,     // level
                        Types.VARCHAR,     // format
                        Types.LONGVARCHAR, // tags
                        Types.LONGVARCHAR, // keywords
                        Types.VARCHAR,     // language
                        Types.VARCHAR,     // event id
                        Types.LONGVARCHAR, // lead
                        Types.LONGVARCHAR, // body
                        Types.LONGVARCHAR, // notes
                        Types.LONGVARCHAR, // feedback
                        Types.LONGVARCHAR, // expected
                        Types.LONGVARCHAR, // outline
                        Types.LONGVARCHAR, // equipment
                        Types.CHAR,        // published
                        Types.VARCHAR      // id
                }
        );
        jdbcTemplate.update("delete from session_speaker where sessionId = ?", new Object[]{session.getId()});
        List<Speaker> speakers = session.getSpeakers();
        for (int position = 0; position < speakers.size(); position++) {
            Speaker speaker = speakers.get(position);
            jdbcTemplate.update(
                    "insert into session_speaker values (?, ?, ?, ?, ?, ?)",
                    new Object[]{
                            session.getId(),
                            speaker.getPersonId(),
                            position,
                            speaker.getDescription(),
                            speaker.getTagsAsString(DELIMITER),
                            speaker.getPhoto() == null ? null : speaker.getPhoto().getId(),
                    },
                    new int[]{
                            Types.VARCHAR,     // sessionId
                            Types.VARCHAR,     // personId
                            Types.INTEGER,     // position
                            Types.LONGVARCHAR, // description
                            Types.LONGVARCHAR, // tags
                            Types.VARCHAR,     // photo
                    }
            );
        }
        jdbcTemplate.update("delete from session_attachement where sessionId = ?", new Object[]{session.getId()});
        List<Binary> attachements = session.getAttachements();
        for (int position = 0; position < attachements.size(); position++) {
            Binary attachement = attachements.get(position);
            if (attachement == null) {
                continue;
            }
            jdbcTemplate.update(
                    "insert into session_attachement values (?, ?, ?)",
                    new Object[]{
                            session.getId(),
                            attachement.getId(),
                            position,
                    },
                    new int[]{
                            Types.VARCHAR,     // sessionId
                            Types.VARCHAR,     // attachementId
                            Types.INTEGER,     // position
                    }

            );
        }
    }

    public void deleteSession(String id) {
        jdbcTemplate.update("delete from session_speaker where sessionId = ?", new Object[]{id});
        jdbcTemplate.update("delete from session_attachement where sessionId = ?", new Object[]{id});
        jdbcTemplate.update("delete from session where id = ?", new Object[]{id}, new int[]{Types.VARCHAR});
    }

    private class SessionMapper implements RowMapper {

        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Session session = new Session();
            session.setId(rs.getString("id"));
            session.setRevision(rs.getInt("revision"));
            session.setTitle(rs.getString("title"));
            session.setTimeslot(mapInterval(rs));
            session.setState(Session.State.valueOf(rs.getString("state")));
            String roomId = rs.getString("roomId");
            if (roomId != null) {
                session.setRoom(roomDao.getRoom(roomId));
            }
            session.setLevel(Session.Level.valueOf(rs.getString("level")));
            session.setFormat(Session.Format.valueOf(rs.getString("format")));
            session.setLanguage(Language.valueOf(rs.getString("language")));
            session.setEventId(rs.getString("eventId"));
            session.setLead(rs.getString("lead"));
            session.setBody(rs.getString("body"));
            session.setNotes(rs.getString("notes"));
            session.setEquipment(rs.getString("equipment"));
            session.setExpectedAudience(rs.getString("expected"));
            session.setFeedback(rs.getString("feedback"));
            session.setOutline(rs.getString("outline"));
            session.setPublished("y".equalsIgnoreCase(rs.getString("published")));
            String tags = rs.getString("tags");
            if (tags != null) {
                session.setTags(Arrays.asList(tags.split(DELIMITER)));
            }
            String keywords = rs.getString("keywords");
            if (keywords != null) {
                session.setKeywords(Arrays.asList(keywords.split(DELIMITER)));
            }
            //noinspection unchecked
            session.setSpeakers(
                    jdbcTemplate.query(
                            "select session_speaker.*, person.name as person_name from session_speaker, person where session_speaker.sessionId = ? and session_speaker.personId = person.id order by session_speaker.position ",
                            new Object[]{session.getId()},
                            new RowMapper() {
                                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    Speaker speaker = new Speaker(rs.getString("personId"), rs.getString("person_name"));
                                    speaker.setDescription(rs.getString("description"));
                                    String tags = rs.getString("tags");
                                    if (tags != null) {
                                        speaker.setTags(Arrays.asList(tags.split(DELIMITER)));
                                    }
                                    String photoId = rs.getString("photo");
                                    if (photoId != null) {
                                        speaker.setPhoto(binaryDao.getBinary(photoId));
                                    }
                                    return speaker;
                                }
                            }
                    )
            );
            //noinspection unchecked
            session.setAttachements(
                    jdbcTemplate.query(
                            "select attachementId from session_attachement where sessionId = ? order by position",
                            new Object[]{session.getId()},
                            new RowMapper() {
                                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    return binaryDao.getBinary(rs.getString("attachementId"));
                                }
                            }
                    )
            );
            return session;
        }
    }
}
