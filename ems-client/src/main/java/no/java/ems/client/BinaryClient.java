package no.java.ems.client;

import no.java.ems.domain.Binary;
import no.java.ems.service.EmsService;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.resource.InputRepresentation;
import org.restlet.Client;

public class BinaryClient extends AbstractClient<Binary> {

    private static final String BINARY_PREFIX = "/binaries/";

    BinaryClient(final String baseUri, Client client) {
        super(baseUri, client);
    }

    public Binary get(String id) {
        return deserialize(doRequest(new Request(Method.GET, baseUri + BINARY_PREFIX + id)), Binary.class);
    }

    public Binary getBinary(String binaryID) {
        return get(binaryID);
    }

    public Binary createBinary(final Binary binary) {
        Request request = new Request(Method.POST, baseUri + BINARY_PREFIX);
        request.setEntity(new InputRepresentation(binary.getDataStream(), MediaType.APPLICATION_OCTET_STREAM));
        Form form = new Form();
        form.add("X-binary-fileName", binary.getFileName());
        form.add("X-binary-mimeType", binary.getMimeType() == null ? MediaType.APPLICATION_OCTET_STREAM.toString() : binary.getMimeType());
        request.getAttributes().put("org.restlet.http.headers", form);
        return deserialize(doRequest(new Request(Method.GET, doRequest(request).getRedirectRef())), Binary.class);
    }

    public void updateBinary(final Binary binary) {
        throw new UnsupportedOperationException();
    }

    public void deleteBinary(final String binaryId) {
        doRequest(new Request(Method.DELETE, baseUri + BINARY_PREFIX + binaryId));
    }
}
