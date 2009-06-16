package no.java.ems.server.restlet.filter;

import no.java.ems.dao.SessionDao;
import no.java.ems.domain.Session;
import org.restlet.Context;
import org.restlet.resource.Resource;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SessionFilter extends AbstractObjectFilter<Session> {
    private SessionDao sessionDao;

    public SessionFilter(Context context, Class<? extends Resource> resourceType, SessionDao sessionDao) {
        super(context, resourceType);
        this.sessionDao = sessionDao;
    }

    protected boolean allowAccess(String id) {
        Session s = sessionDao.getSession(id);

        return s != null && s.isPublished();
    }

    protected boolean allowAccess(Session session) {
        return allowAccess(session.getId());
    }
}
