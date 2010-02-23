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

package no.java.ems.external.v2;

import fj.data.Option;
import fj.Unit;
import no.java.ems.client.ResourceHandle;
import org.joda.time.LocalDate;


/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface EmsV2Client {
    Option<EventV2> getEvent(String eventId);

    ResourceHandle addEvent(EventV2 event);

    EventListV2 getEvents();

    SessionListV2 getSessions(String eventId);

    Option<SessionV2> getSession(String eventId, String sessionId);

    SessionListV2 findSessionsByDate(String eventId, LocalDate date);

    SessionListV2 findSessionsByTitle(String eventId, String title);

    SessionListV2 getSessionsByTitle(String eventId, String title);

    SessionListV2 searchForSessions(String eventId, String query);

    ResourceHandle addSession(SessionV2 session);

    Unit updateSession(SessionV2 session);

    PersonListV2 getPeople();

    Option<PersonV2> getPerson(String personId);

    ResourceHandle addPerson(PersonV2 personV1);

    Unit updatePerson(PersonV2 personV1);
}
