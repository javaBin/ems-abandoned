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
