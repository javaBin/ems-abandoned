package no.java.ems.server.restlet;

import no.java.ems.dao.*;
import no.java.ems.server.restlet.filter.EmsStatusService;
import no.java.ems.server.restlet.filter.PersonFilter;
import no.java.ems.server.restlet.filter.SessionFilter;
import no.java.ems.server.search.SearchService;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.resource.Resource;

import java.io.File;

/**
 * Ems restlet application
 */
public class EmsApplication extends Application {

    public static final String SESSION_FILTER_ATTRIBUTE = "no.java.domain.session.filtered";

    private File emsHome;
    private EventDao eventDao;
    private PersonDao personDao;
    private SessionDao sessionDao;
    private BinaryDao binaryDao;
    private SearchService searchService;
    private boolean secure;
    private RoomDao roomDao;

    public EmsApplication(File emsHome, EventDao eventDao, PersonDao personDao, SessionDao sessionDao,
                          BinaryDao binaryDao, RoomDao roomDao, SearchService searchService, boolean secure) {
        this.emsHome = emsHome;
        this.eventDao = eventDao;
        this.personDao = personDao;
        this.sessionDao = sessionDao;
        this.binaryDao = binaryDao;
        this.roomDao = roomDao;
        this.searchService = searchService;
        this.secure = secure;
    }

    @Override
    public void start() throws Exception {
        super.start();

        getContext().getAttributes().put("ems.home", emsHome.getAbsolutePath());

        getContext().getAttributes().put("personDao", personDao);
        getContext().getAttributes().put("sessionDao", sessionDao);
        getContext().getAttributes().put("eventDao", eventDao);
        getContext().getAttributes().put("roomDao", roomDao);
        getContext().getAttributes().put("binaryDao", binaryDao);
        getContext().getAttributes().put("searchService", searchService);
    }

    public Restlet createRoot() {
        Filter authenticationFilter = new AuthenticationFilter(getContext(), secure);
        Router router = new Router(getContext());
        router.attach("/people", PersonResource.class);
        router.attach("/people/{pid}", new PersonFilter(getContext(), PersonResource.class));
        router.attach("/events", EventResource.class);
        router.attach("/events/{eid}", EventResource.class);
        router.attach("/events/{eid}/rooms/{rid}", RoomResource.class);
        router.attach("/events/{eid}/rooms/", RoomResource.class);
        attach(router, "/events/{eid}/sessions", SessionResource.class);
        attach(router, "/events/{eid}/sessions/search", SessionSearchResource.class);
        attach(router, "/events/{eid}/sessions/{sid}", SessionResource.class);
        attach(router, "/events/{eid}/sessions-by-{type}", SessionListResource.class);
        attach(router, "/events/{eid}/sessions-by-{type}/{param}", SessionListResource.class);
        router.attach("/binaries/{bid}", BinaryResource.class);
        router.attach("/binaries/", BinaryResource.class);
        router.attach("/rooms/", RoomResource.class);
        router.attach("/rooms/{rid}", RoomResource.class);
        router.setRoutingMode(Router.BEST);
        authenticationFilter.setNext(router);

        this.setStatusService(new EmsStatusService());

        if (secure) {
            return authenticationFilter;
        }
        else {
            return router;
        }
    }

    private void attach(Router router, String uriPattern, Class<? extends Resource> resourceType) {
        if (secure) {
            router.attach(uriPattern, new SessionFilter(getContext(), resourceType, sessionDao));
        }
        else {
            router.attach(uriPattern, resourceType);
        }
    }

    public static <T> T getService(Context context, Class<T> klass) {
        String name = klass.getSimpleName();
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        Object service = context.getAttributes().get(name);

        if (service == null) {
            throw new RuntimeException("Unable to look up service '" + name + "'.");
        }

        return klass.cast(service);
    }
}
