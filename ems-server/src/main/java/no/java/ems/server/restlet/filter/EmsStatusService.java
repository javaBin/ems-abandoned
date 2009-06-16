package no.java.ems.server.restlet.filter;

import org.restlet.service.StatusService;
import org.restlet.data.Status;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
* @version $Id$
*/
public class EmsStatusService extends StatusService {
    public EmsStatusService() {
        super(true);
    }

    public Status getStatus(Throwable throwable, Request request, Response response) {
        if (!(throwable instanceof EmptyResultDataAccessException)) {
            return null;
        }

        return Status.CLIENT_ERROR_NOT_FOUND;
    }
}
