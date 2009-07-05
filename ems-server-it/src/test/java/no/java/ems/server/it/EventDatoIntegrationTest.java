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

package no.java.ems.server.it;

import no.java.ems.dao.EventDao;
import no.java.ems.dao.RoomDao;
import no.java.ems.server.domain.Event;
import no.java.ems.server.domain.Room;
import org.joda.time.Interval;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Trygve Laugstol
 */
public class EventDatoIntegrationTest extends AbstractIntegrationTest {

//    private static EmsServices emsServices;
//
//    @BeforeClass
//    public static void setup() throws Exception {
//        File emsHome = PlexusTestCase.getTestFile("target/ems-home-" + EventDatoIntegrationTest.class.getName());
//        FileUtils.deleteDirectory(emsHome);
//
//        emsServices = new EmsServices(emsHome, 0, true, false, 0, false);
//        emsServices.getDerbyService().maybeCreateTables(false);
//    }
//
//    @AfterClass
//    public static void tearDown() throws Exception {
//        if(emsServices != null){
//            emsServices.stop();
//        }
//    }

    @Test
    public void testDao() {
        RoomDao roomDao = getRoomDao();
        Room room1 = new Room("Scandinavia scene");
        Room room2 = new Room("Stockholm");
        Room room3 = new Room("Oslo");
        roomDao.save(room1);
        roomDao.save(room2);
        roomDao.save(room3);

        EventDao eventDao = getEventDao();

        Event jz06 = new Event();
        jz06.setName("JavaZone 2006");

        eventDao.saveEvent(jz06);
        assertNotNull(jz06.getId());
        assertEquals("JavaZone 2006", jz06.getName());

        // -----------------------------------------------------------------------
        // Test room manipulation
        // -----------------------------------------------------------------------

        {
            List<Room> rooms;
            jz06 = eventDao.getEvent(jz06.getId());
            assertNotNull(jz06.getRooms());
            assertEquals(0, jz06.getRooms().size());

            rooms = new ArrayList<Room>();
            rooms.add(room1);
            rooms.add(room2);
            jz06.setRooms(rooms);
            eventDao.saveEvent(jz06);
            jz06 = eventDao.getEvent(jz06.getId());
            assertNotNull(jz06.getRooms());
            assertEquals(2, jz06.getRooms().size());
            assertEquals("Scandinavia scene", jz06.getRooms().get(0).getName());
            assertEquals("Stockholm", jz06.getRooms().get(1).getName());

            rooms = new ArrayList<Room>(jz06.getRooms());
            rooms.remove(0);
            rooms.add(0, room3);
            jz06.setRooms(rooms);
            eventDao.saveEvent(jz06);
            jz06 = eventDao.getEvent(jz06.getId());
            assertNotNull(jz06.getRooms());
            assertEquals(2, jz06.getRooms().size());
            assertEquals("Oslo", jz06.getRooms().get(0).getName());
            assertEquals("Stockholm", jz06.getRooms().get(1).getName());
        }

        // -----------------------------------------------------------------------
        // Test timeslot manipulation
        // -----------------------------------------------------------------------

        {
            jz06 = eventDao.getEvent(jz06.getId());
            assertNotNull(jz06.getTimeslots());
            assertEquals(0, jz06.getTimeslots().size());

            List<Interval> timeslots = new ArrayList<Interval>(jz06.getTimeslots());
            timeslots.add(jz06sep13slot1);
            timeslots.add(jz06sep13slot2);
            timeslots.add(jz06sep13slot3);
            timeslots.add(jz06sep13slot4);
            timeslots.add(jz06sep13slot5);
            timeslots.add(jz06sep13slot6);
            jz06.setTimeslots(timeslots);
            eventDao.saveEvent(jz06);
            jz06 = eventDao.getEvent(jz06.getId());
            assertNotNull(jz06.getTimeslots());
            assertEquals(6, jz06.getTimeslots().size());
            assertEquals(9, jz06.getTimeslots().get(0).getStart().getHourOfDay());
            assertEquals(10, jz06.getTimeslots().get(1).getStart().getHourOfDay());
            assertEquals(11, jz06.getTimeslots().get(2).getStart().getHourOfDay());
            assertEquals(13, jz06.getTimeslots().get(3).getStart().getHourOfDay());
            assertEquals(14, jz06.getTimeslots().get(4).getStart().getHourOfDay());
            assertEquals(15, jz06.getTimeslots().get(5).getStart().getHourOfDay());

            timeslots = new ArrayList<Interval>(jz06.getTimeslots());
            timeslots.remove(0);
            timeslots.add(0, jz06sep13slot7);
            jz06.setTimeslots(timeslots);
            eventDao.saveEvent(jz06);
            jz06 = eventDao.getEvent(jz06.getId());
            assertEquals(6, jz06.getTimeslots().size());
            assertEquals(17, jz06.getTimeslots().get(0).getStart().getHourOfDay());
            assertEquals(10, jz06.getTimeslots().get(1).getStart().getHourOfDay());
            assertEquals(11, jz06.getTimeslots().get(2).getStart().getHourOfDay());
            assertEquals(13, jz06.getTimeslots().get(3).getStart().getHourOfDay());
            assertEquals(14, jz06.getTimeslots().get(4).getStart().getHourOfDay());
            assertEquals(15, jz06.getTimeslots().get(5).getStart().getHourOfDay());
        }

        eventDao.deleteEvent(jz06.getId());
    }
}
