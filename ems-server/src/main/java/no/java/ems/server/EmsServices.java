package no.java.ems.server;

import no.java.ems.dao.*;
import no.java.ems.dao.impl.*;
import no.java.ems.domain.Event;
import no.java.ems.server.cli.Server;
import no.java.ems.server.restlet.EmsApplication;
import no.java.ems.server.search.SearchService;
import no.java.ems.server.search.SolrSearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.net.URI;

/**
 * Service locator and configuration manager.
 *
 * @author Trygve Laugstol
 */
public class EmsServices implements Stoppable {
    private Log log = LogFactory.getLog(getClass());

    // -----------------------------------------------------------------------
    // Services
    // -----------------------------------------------------------------------

    private JdbcTemplate jdbcTemplate;
    private BinaryDao binaryDao;
    private RoomDao roomDao;
    private PersonDao personDao;
    private SessionDao sessionDao;
    private EventDao eventDao;
    private EmsApplication application;
    private Component component;
    private DerbyService derbyService;
    private SearchService searchService;

    // -----------------------------------------------------------------------
    // Configuration
    // -----------------------------------------------------------------------

    private File emsHome;
    private File dbHome;
    private File indexHome;
    private int httpPort;
    private int derbyPort;

    public EmsServices(File emsHome, int httpPort, boolean startDatabase, boolean dropTables, int derbyPort,
                       boolean secure)
        throws Exception {
        this.emsHome = emsHome;
        this.httpPort = httpPort;
        this.derbyPort = derbyPort;

        if (!emsHome.exists()) {
            if(!emsHome.mkdirs()) {
                throw new RuntimeException("Unable to create home directory: " + emsHome.getAbsolutePath());
            }
        }

        final File binaries = new File(emsHome, "binaries");
        if (!binaries.exists()) {
            if(!binaries.mkdirs()) {
                throw new RuntimeException("Unable to create binaries directory: " + binaries.getAbsolutePath());
            }
        }

        dbHome = new File(emsHome, "database");
        indexHome = new File(emsHome, "index");
        File binariesHome = new File(emsHome, "binaries");

        EmbeddedConnectionPoolDataSource dataSource = new EmbeddedConnectionPoolDataSource();
        dataSource.setCreateDatabase("create");
        dataSource.setDatabaseName(new File(dbHome, "ems").getAbsolutePath());
        dataSource.setUser("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);
        binaryDao = new FileBinaryDao(binariesHome);
        roomDao = new JdbcTemplateRoomDao(jdbcTemplate);
        personDao = new JdbcTemplatePersonDao(jdbcTemplate, binaryDao);
        sessionDao = new JdbcTemplateSessionDao(jdbcTemplate, binaryDao, roomDao);
        eventDao = new JdbcTemplateEventDao(jdbcTemplate, binaryDao);
        searchService = new SolrSearchService(indexHome);
        //searchService = new LuceneSearchService(indexHome);
        derbyService = new DerbyService(jdbcTemplate, eventDao, personDao, sessionDao, roomDao, dbHome, derbyPort);
        application = new EmsApplication(emsHome, eventDao, personDao, sessionDao, binaryDao, roomDao, searchService, secure);

        component = new Component();

        if (startDatabase) {
            derbyService.maybeCreateTables(dropTables);
        }

        if (httpPort != 0) {
            component.getServers().add(Protocol.HTTP, httpPort);
            //TODO: Use SSL, however restlets does not support this on client side yet. As far as I have found out.
//            Series<Parameter> parameters = component.getServers().getContext().getParameters();
//            parameters.update("keystorePath", "keystore");
//            parameters.update("keystorePassword", "changeme");
//            parameters.update("keyPassword", "changeme");
        }

        if (derbyPort != 0) {
            derbyService.startNetworkServer();
        }

        component.getDefaultHost().attach(
            "/ems",
            application
        );

        component.start();

        log.info("All events: ");
        for (Event event : eventDao.getEvents()) {
            log.info(" * " + event.getName());
        }

        log.info("EMS Successfully started!");
        log.info("Directories");
        log.info(" Index: " + emsHome);
        log.info(" Database: " + dbHome);
        log.info(" Binary URI: " + getBinaryUri());
        log.info("Running with security: " + secure);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public BinaryDao getBinaryDao() {
        return binaryDao;
    }

    public RoomDao getRoomDao() {
        return roomDao;
    }

    public PersonDao getPersonDao() {
        return personDao;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    public EventDao getEventDao() {
        return eventDao;
    }

    public EmsApplication getApplication() {
        return application;
    }

    public Component getComponent() {
        return component;
    }

    public DerbyService getDerbyService() {
        return derbyService;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public File getEmsHome() {
        return emsHome;
    }

    public File getDbHome() {
        return dbHome;
    }

    public File getIndexHome() {
        return indexHome;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getDerbyPort() {
        return derbyPort;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static URI getBinaryUri() {
        String binaryURI = System.getProperty(Server.EMS_BINARY_URI, "http://localhost:3000/ems/binaries/");
        if (!binaryURI.endsWith("/")) {
            binaryURI += "/";
        }
        return URI.create(binaryURI);
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void stop() {
        stop(new StopTemplate() {
            void stop() throws Exception {
                component.stop();
            }
        });
        stop(derbyService);
    }

    private static abstract class StopTemplate {
        abstract void stop() throws Exception;
    }

    private void stop(StopTemplate stopTemplate) {
        try {
            stopTemplate.stop();
        } catch (Exception e) {
            // ignore
        }
    }

    private void stop(Stoppable stoppable) {
        try {
            stoppable.stop();
        } catch (Exception e) {
            // ignore
        }
    }
}
