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

import java.net.URI;

import no.java.ems.client.ResourceHandle;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public interface EmsV1Client2 {
    Option<EventV1> getEvent(ResourceHandle handle);

    URI addEvent(EventV1 event);

    EventListV1 getEvents();

    SessionListV1 getSessions(ResourceHandle handle);

    Option<SessionV1> getSession(ResourceHandle handle);

    SessionListV1 findSessions(ResourceHandle handle, String json);

    URI addSession(ResourceHandle handle, SessionV1 session);

    Unit updateSession(ResourceHandle handle, SessionV1 session);

    PersonListV1 getPeople();

    Option<PersonV1> getPerson(ResourceHandle handle);

    URI addPerson(PersonV1 personV1);

    Unit updatePerson(PersonV1 personV1);
}
