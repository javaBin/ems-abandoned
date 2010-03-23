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

import no.java.ems.cli.ImportData;
import no.java.ems.client.RestEmsService;
import no.java.ems.server.domain.Event;
import no.java.ems.server.domain.Session;
import org.joda.time.*;
import org.junit.Test;

import java.util.List;

import static fj.data.Option.some;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test cases based on the use cases that Incogito has for EMS
 *
 * @author Trygve Laugstol
 */
public class IncogitoIntegrationTest extends AbstractIntegrationTest {

    private static String eventId;
    private static LocalDate sep12 = new LocalDate(2008, 9, 12);


    @Test
    public void testGetScheduleForDay() throws Exception {        
        loadData();
        RestEmsService service = new RestEmsService(baseUri.toString());
        /*for (no.java.ems.domain.Session session : service.getSessions(eventId)) {
            System.out.println("session = " + session);
        }*/
        assertNotNull(eventId);
        List<no.java.ems.domain.Session> sessionByDate = service.findSessionByDate(eventId, sep12);
        for (no.java.ems.domain.Session session : sessionByDate) {
            assertNotNull("Session was null", session);
        }

        String title = "Implementing external DSLs in Java";
        List<no.java.ems.domain.Session> ids = service.findSessionsByTitle(eventId, title);

        assertNotNull(ids);
        // uhm, slight issue with duplicate sessions on import
        for (no.java.ems.domain.Session session : ids) {
            assertEquals(title, session.getTitle());
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void loadData() throws Exception {

        List<Event> list = getEventDao().getEvents();
        if (list.size() == 0) {
            Event event = new Event();
            event.setName("IntegrationZone");
            event.setDate(sep12);
            getEventDao().saveEvent(event);
            eventId = event.getId();
        }
        else {
            eventId = list.get(0).getId();
        }

        ImportData.main(
                new String[]{
                        "--base-uri", baseUri.toString(),
                        "--file", getClass().getResource("/data.xml").toURI().getPath(),
                        "--event-id", eventId}
        );
        //ImportData.main(ems, handle, getTestFile("src/test/resources/test-data/" + dataSet));

        LocalDateTime date = sep12.toLocalDateTime(LocalTime.MIDNIGHT);
        date = date.plusHours(9);

        for (Session session : getSessionDao().getSessions(eventId)) {
            session.setTimeslot(some(new Interval(date.toDateTime(), Minutes.minutes(60))));
            date.plusMinutes(75);
            getSessionDao().saveSession(session);
        }
    }
}
