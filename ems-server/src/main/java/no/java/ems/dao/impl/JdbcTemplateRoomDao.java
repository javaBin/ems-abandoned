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

import no.java.ems.dao.RoomDao;
import no.java.ems.server.domain.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Repository
public class JdbcTemplateRoomDao extends AbstractDao implements RoomDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTemplateRoomDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<Room> getRooms() {
        //noinspection unchecked
        return jdbcTemplate.query("select * from room", new RoomMapper());
    }

    public Room getRoom(String id) {
        return (Room) jdbcTemplate.queryForObject(
            "select * from room where id = ?",
            new Object[]{id},
            new int[]{Types.VARCHAR},
            new RoomMapper()
        );

    }

    public void save(Room room) {
        String sql;
        if (room.getId() == null) {
            sql = "insert into room(name, description, revision, id) values (?, ?, 0, ?)";
            room.setId(UUID.randomUUID().toString());
        } else {
            sql = "update room set name=?, description=?, revision=revision+1 where id = id=?";
        }
        jdbcTemplate.update(
            sql,
            new Object[]{
                room.getName(),
                room.getDescription(),
                room.getId(),
            },
            new int[]{
                Types.VARCHAR, // name
                Types.VARCHAR, // description
                Types.INTEGER, // description
            });
    }

    public static class RoomMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Room room = new Room(rs.getString("name"));
            room.setId(rs.getString("id"));
            room.setDescription(rs.getString("description"));
            return room;
        }
    }
}
