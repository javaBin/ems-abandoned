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

package no.java.ems.external.v1;

import fj.data.Option;
import fj.Unit;
import org.joda.time.LocalDate;

import java.net.URI;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface EmsV1Client {
    Option<EventV1> getEvent(String eventId);

    URI addEvent(EventV1 event);

    EventListV1 getEvents();

    SessionListV1 getSessions(String eventId);

    Option<SessionV1> getSession(String eventId, String sessionId);

    SessionListV1 findSessionsByDate(String eventId, LocalDate date);

    SessionListV1 findSessionsByTitle(String eventId, String title);

    SessionListV1 getSessionsByTitle(String eventId, String title);

    SessionListV1 searchForSessions(String eventId, String query);

    URI addSession(SessionV1 session);

    Unit updateSession(SessionV1 session);

    PersonListV1 getPeople();

    Option<PersonV1> getPerson(String personId);

    URI addPerson(PersonV1 personV1);

    Unit updatePerson(PersonV1 personV1);
}
