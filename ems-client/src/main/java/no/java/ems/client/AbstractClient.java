package no.java.ems.client;

import no.java.ems.service.RequestCallback;
import org.apache.commons.lang.SerializationUtils;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:erlend@hamnaberg.net>Erlend Hamnaberg</a>
 */
public abstract class AbstractClient<T> {

    protected final Logger logger = Logger.getLogger(getClass().getName());
    protected final String baseUri;
    private final Client client;
    private ChallengeResponse challenge;
    private RequestCallback requestCallback;

    public AbstractClient(final String baseUri, final Client client) {
        this.baseUri = baseUri;
        this.client = client;
    }

    public abstract T get(String id);

    public void setCredentials(String username, String password) {
        if (username != null) {
            challenge = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, username, password);
        } else {
            challenge = null;
        }
    }

    protected String extractId(final Response response) {
        String[] parts = response.getRedirectRef().toString().split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    protected Response doRequest(final Request request) {
        if (challenge != null) {
            System.out.println("Getting (as '" + challenge.getIdentifier() + "'): " + request.getMethod().toString() + " " + request.getResourceRef().getPath());
        } else {
            System.out.println("Requesting (anonymously): " + request.getMethod().toString() + " " + request.getResourceRef().getPath());
        }
        request.setChallengeResponse(challenge);
        request.getClientInfo().getAcceptedMediaTypes().add(0, new Preference<MediaType>(MediaType.APPLICATION_JAVA_OBJECT));
        Response response = client.handle(request);
        requestCallback.onComplete(response.getRequest().getResourceRef().toString(), response.getStatus().getCode());
        return response;
    }

    protected <T> T deserialize(final Response response, Class<T> klass) {
        try {
            InputStream data = response.getEntity().getStream();

            Object o = SerializationUtils.deserialize(data);

            if (!klass.isAssignableFrom(o.getClass())) {
                throw new RuntimeException("Got unexpected object back. Expected " + klass.getName() + ", got: " + o.getClass());
            }

            return klass.cast(o);
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("Unable to deserialize resource: " + response, e);
        }
    }

    public void setResultEvaluator(RequestCallback requestCallback) {
        this.requestCallback = requestCallback;
    }
}
