package no.java.ems.client;

import static no.java.ems.client.ClientUtil.encode;
import no.java.ems.domain.Session;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Form;
import org.restlet.resource.ObjectRepresentation;

import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
@SuppressWarnings({"unchecked"})
public class SessionsClient extends AbstractClient<Session> {

    private static final String SESSION = "/events/%s/sessions/%s";

    SessionsClient(String baseUri, Client client) {
        super(baseUri, client);
    }

    public List<String> findSessionsBySpeakerName(String eventId, String name) {
        String url = String.format(baseUri + "/events/%s/sessions-by-speaker-name/%s", eventId, encode(name));
        return deserialize(doRequest(new Request(Method.GET, url)), List.class);
    }

    public List<String> findSessionIdsByEvent(String eventId) {
        Request request = newRequest(Method.GET, eventId, "");
        request.getAttributes().put("onlyid", true);
        return deserialize(doRequest(request), List.class);
    }

    public List<String> findSessionsByTitle(String eventId, String title) {
        String url = String.format(baseUri + "/events/%s/sessions-by-title/%s", eventId, encode(title));
        return deserialize(doRequest(new Request(Method.GET, url)), List.class);
    }

    public List<String> findSessionsByDate(String eventId, LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
        String dateString = formatter.print(date);
        String url = String.format(baseUri + "/events/%s/sessions-by-date/%s", eventId, dateString);
        return deserialize(doRequest(new Request(Method.GET, url)), List.class);
    }

    public List<Session> findSessionsByEvent(String eventId) {
        return deserialize(doRequest(newRequest(Method.GET, eventId, "")), List.class);
    }

    public Session get(String id) {
        return deserialize(doRequest(newRequest(Method.GET, null, id)), Session.class);
    }

    public Session getSession(String id) {
        return get(id);
    }

    public void deleteSession(String id) {
        doRequest(newRequest(Method.DELETE, null, id));
    }

    public void createSession(Session session) {
        Request request = newRequest(Method.POST, session.getEventId(), "");
        request.setEntity(new ObjectRepresentation(session));
        Response response = doRequest(request);
        session.setId(extractId(response));
    }

    public void updateSession(Session session) {
        Request request = newRequest(Method.PUT, session.getEventId(), session.getId());
        request.setEntity(new ObjectRepresentation(session));
        doRequest(request);
    }

    public List<String> search(String eventId, String query) {
        String url = String.format(baseUri + "/events/%s/sessions/search", eventId, encode(query));
        Request request = new Request(Method.GET, url);
        Form form = new Form();
        form.set("q", query, false);
        request.getResourceRef().setQuery(form.getQueryString());
        return deserialize(doRequest(request), List.class);
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private Request newRequest(Method method, String eventId, String path) {
        return new Request(method, String.format(baseUri + SESSION, eventId, path));
    }
}
