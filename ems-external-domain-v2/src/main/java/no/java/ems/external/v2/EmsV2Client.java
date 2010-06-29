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

import fj.*;
import fj.data.*;
import no.java.ems.client.*;
import no.java.ems.client.xhtml.Form;
import org.apache.abdera.model.Feed;

import java.net.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public interface EmsV2Client {
    Either<Exception, Option<EventV2>> getEvent(ResourceHandle handle);

    ResourceHandle addEvent(EventV2 event);

    Unit updateEvent(ResourceHandle handle, EventV2 event);

    Either<Exception, EventListV2> getEvents();

    Either<Exception, SessionListV2> getSessions(ResourceHandle handle);

    Either<Exception, Option<SessionV2>> getSession(ResourceHandle handle);

    ResourceHandle addSession(ResourceHandle eventHandle, SessionV2 session);

    Unit updateSession(ResourceHandle handle, SessionV2 session);

    Either<Exception, PersonListV2> getPeople();

    Either<Exception, Option<PersonV2>> getPerson(ResourceHandle handle);

    ResourceHandle addPerson(PersonV2 personV1);

    Unit updatePerson(PersonV2 personV1);

    void login(URI endpoint);

    Either<Exception, Option<Form>> getSearchForm();

    Either<Exception, Option<Feed>> search(Form form);
}
