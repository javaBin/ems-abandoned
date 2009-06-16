package no.java.ems.client;

import org.codehaus.httpcache4j.Status;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: #5 $ $Date: 2008/09/15 $
*/
public class HttpException extends RuntimeException {
    public HttpException(final URI pRequestURI, Status status) {
        this(pRequestURI, status, "");
    }

    public HttpException(final URI requestURI, Status status, final String message) {
        super(message + " " + requestURI + " " + status);
    }
}
