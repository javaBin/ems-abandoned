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

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public interface EmsV2Client {
    Option<EventV2> getEvent(ResourceHandle handle);

    ResourceHandle addEvent(EventV2 event);

    Unit updateEvent(ResourceHandle handle, EventV2 event);

    EventListV2 getEvents();

    SessionListV2 getSessions(ResourceHandle handle);

    Option<SessionV2> getSession(ResourceHandle handle);    

    ResourceHandle addSession(ResourceHandle eventHandle, SessionV2 session);

    Unit updateSession(ResourceHandle handle, SessionV2 session);

    PersonListV2 getPeople();

    Option<PersonV2> getPerson(ResourceHandle handle);

    ResourceHandle addPerson(PersonV2 personV1);

    Unit updatePerson(PersonV2 personV1);

    

    void login(URI endpoint);
}
