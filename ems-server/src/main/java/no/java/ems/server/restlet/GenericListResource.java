package no.java.ems.server.restlet;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Trygve Laugstol
 */
public abstract class GenericListResource extends Resource {
    public GenericListResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    protected static String urlDecode(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (
            UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to decode parameter", e);
        }
    }
}
