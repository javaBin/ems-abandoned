/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package no.java.ems.dao.impl;

import no.java.ems.dao.BinaryDao;
import no.java.ems.dao.RoomDao;
import no.java.ems.dao.SessionDao;
import no.java.ems.server.domain.Binary;
import no.java.ems.server.domain.Language;
import no.java.ems.server.domain.Session;
import no.java.ems.server.domain.Speaker;
import static no.java.ems.server.f.ExternalV2F.*;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import static java.sql.Types.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcTemplateSessionDao extends AbstractDao implements SessionDao {

    private static final String DELIMITER = ",";
    private JdbcTemplate jdbcTemplate;
    private BinaryDao binaryDao;
    private RoomDao roomDao;

    @Autowired
    public JdbcTemplateSessionDao(JdbcTemplate jdbcTemplate, BinaryDao binaryDao, RoomDao roomDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.binaryDao = binaryDao;
        this.roomDao = roomDao;
    }

    public Session getSession(String id) {
        try {
            return (Session) jdbcTemplate.queryForObject(
                    "select * from session where id = ? and revision in (select max(revision) from session where id = ?)",
                    new Object[]{id, id},
                    new int[]{VARCHAR, VARCHAR},
                    new SessionMapper()
            );
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Session getSession(String eventId, String id) {
        try {
            return (Session) jdbcTemplate.queryForObject(
                    "select * from session where id = ? and eventId = ? and revision in (select max(revision) from session where id = ?)",
                    new Object[]{id, eventId, id},
                    new int[]{VARCHAR, VARCHAR, VARCHAR},
                    new SessionMapper()
            );
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }

    }

    public List<Session> getSessions(String eventId) {
        //noinspection unchecked
        return jdbcTemplate.query(
                "select * from session where eventId = ? order by title",
                new Object[]{eventId},
                new int[]{VARCHAR},
                new SessionMapper()
        );
    }

    public List<String> getSessionIdsByEventId(String eventId) {
        //noinspection unchecked
        return jdbcTemplate.query(
                "select id from session where eventid = ? order by title",
                new Object[]{eventId},
                new int[]{VARCHAR},
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
                        VARCHAR,
                        DATE
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
        // todo: when a method is unimplemented don't return a value, it makes the impression it's working. Throw exception
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
                        VARCHAR,
                        VARCHAR
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
            session.setId(UUID.randomUUID().toString());
        }
        sql = "insert into session (revision, title, start, durationMinutes, state, roomId, level, format, tags, keywords, language, eventId, lead, body, notes, feedback, expected, outline, equipment, published, id) VALUES((select count(*) + 1 from session where id = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                new Object[]{
                        session.getId(),
                        session.getTitle(),
                        session.getTimeslot().map(intervalGetStart).map(dateTimeToSqlTimestamp).orSome((Timestamp) null),
                        session.getTimeslot().map(intervalToPeriod).map(periodGetMinutes).orSome(0),
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
                        VARCHAR,     // id
                        VARCHAR,     // title
                        TIMESTAMP,   // start
                        INTEGER,     // durationMinutes
                        VARCHAR,     // state
                        VARCHAR,     // room id
                        VARCHAR,     // level
                        VARCHAR,     // format
                        LONGVARCHAR, // tags
                        LONGVARCHAR, // keywords
                        VARCHAR,     // language
                        VARCHAR,     // event id
                        LONGVARCHAR, // lead
                        LONGVARCHAR, // body
                        LONGVARCHAR, // notes
                        LONGVARCHAR, // feedback
                        LONGVARCHAR, // expected
                        LONGVARCHAR, // outline
                        LONGVARCHAR, // equipment
                        CHAR,        // published
                        VARCHAR      // id
                }
        );
        jdbcTemplate.update("delete from session_speaker where sessionId = ?", new Object[]{session.getId()});
        List<Speaker> speakers = session.getSpeakers();
        for (int position = 0; position < speakers.size(); position++) {
            Speaker speaker = speakers.get(position);
            jdbcTemplate.update(
                    "insert into session_speaker (sessionId, revision, personId, position, description, tags, photo) values (?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{
                            session.getId(),
                            session.getRevision(),
                            speaker.getPersonId(),
                            position,
                            speaker.getDescription(),
                            speaker.getTagsAsString(DELIMITER),
                            speaker.getPhoto() == null ? null : speaker.getPhoto().getId(),
                    },
                    new int[]{
                            VARCHAR,     // sessionId
                            INTEGER,     // revision
                            VARCHAR,     // personId
                            INTEGER,     // position
                            LONGVARCHAR, // description
                            LONGVARCHAR, // tags
                            VARCHAR,     // photo
                    }
            );
        }
        jdbcTemplate.update("delete from session_attachement where sessionId = ?", new Object[]{session.getId()});
        List<Binary> attachements = session.getAttachments();
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
                            VARCHAR,     // sessionId
                            VARCHAR,     // attachementId
                            INTEGER,     // position
                    }

            );
        }
    }

    public void deleteSession(String id) {
        jdbcTemplate.update("delete from session_speaker where sessionId = ?", new Object[]{id});
        jdbcTemplate.update("delete from session_attachement where sessionId = ?", new Object[]{id});
        jdbcTemplate.update("delete from session where id = ?", new Object[]{id}, new int[]{VARCHAR});
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
            session.setAttachments(
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
