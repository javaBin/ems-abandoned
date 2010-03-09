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

import static fj.data.Option.some;
import static junit.framework.Assert.assertEquals;
import no.java.ems.cli.command.ImportDirectory;
import no.java.ems.client.ResourceHandle;
import no.java.ems.server.URIBuilder;
import no.java.ems.server.domain.Event;
import no.java.ems.server.domain.Session;
import no.java.ems.external.v2.SessionListV2;
import no.java.ems.external.v2.SessionV2;
import static org.codehaus.plexus.PlexusTestCase.getTestFile;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.net.URI;
import java.util.List;

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

        loadData("incogito");

        URI sessionURI = uriBuilder.sessions().sessions(eventId);
        for (SessionV2 session : ems.getSessions(new ResourceHandle(sessionURI)).getSession()) {
            System.out.println("session = " + session);
        }

        //TODO: replace with search
        /*SessionListV2 sessionsByDate = ems.findSessionsByDate(eventId, sep12);

        for (SessionV2 session : sessionsByDate.getSession()) {
            assertNotNull("Session was null", session);
        }

        String title = "Implementing external DSLs in Java";
        SessionListV2 ids = ems.findSessionsByTitle(eventId, title);

        assertNotNull(ids);
        // uhm, slight issue with duplicate sessions on import
        for (SessionV2 session : ids.getSession()) {
            assertEquals(title, session.getTitle());
        }
        */
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void loadData(String dataSet) throws Exception {

        List<Event> list = getEventDao().getEvents();
        if (list.size() == 0) {
            Event event = new Event();
            event.setName("IntegrationZone");
            event.setDate(sep12);
            getEventDao().saveEvent(event);
            eventId = event.getId();
        } else {
            eventId = list.get(0).getId();
        }

        System.err.println("eventId = " + eventId);
        ResourceHandle handle = new ResourceHandle(uriBuilder.sessions().sessions(eventId));
        new ImportDirectory(ems, handle, getTestFile("src/test/resources/test-data/" + dataSet)).run();

        LocalDateTime date = sep12.toLocalDateTime(LocalTime.MIDNIGHT);
        date = date.plusHours(9);

        for (Session session : getSessionDao().getSessions(eventId)) {
            session.setTimeslot(some(new Interval(date.toDateTime(), Minutes.minutes(60))));
            date.plusMinutes(75);
            getSessionDao().saveSession(session);
        }
    }
}
