package no.java.ems.client;

import no.java.ems.domain.Event;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.Client;

import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class EventsClient extends AbstractClient<Event> {

    private static final String EVENTS = "/events/";

    EventsClient(String baseUri, Client client) {
        super(baseUri, client);
    }

    public List<Event> listEvents() {
        Request request = new Request(Method.GET, baseUri + EVENTS);
        Response response = doRequest(request);
        //noinspection unchecked
        return deserialize(response, List.class);
    }

    public Event get(String id) {
        Request request = new Request(Method.GET, baseUri + EVENTS + id);
        Response response = doRequest(request);
        return deserialize(response, Event.class);
    }

    public Event getEvent(String id) {
        return get(id);
    }

    public void createEvent(Event event) {
        Request request = new Request(Method.POST, baseUri + EVENTS);
        request.setEntity(new ObjectRepresentation(event));
        Response response = doRequest(request);
        event.setId(extractId(response));
    }

    public void updateEvent(Event event) {
        Request request = new Request(Method.PUT, baseUri + EVENTS + event.getId());
        request.setEntity(new ObjectRepresentation(event));
        doRequest(request);
    }

    public void deleteEvent(Event event) {
        Request request = new Request(Method.DELETE, baseUri + EVENTS + event.getId());
        request.setEntity(new ObjectRepresentation(event));
        doRequest(request);
    }
}
