package no.java.ems.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ClientUtil {
    protected static String encode(String string) {
        if(string == null) {
            return null;
        }

        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (
            UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to decode parameter", e);
        }
    }
}
