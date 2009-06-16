package no.java.ems.server.web;

import no.java.ems.server.DerbyService;
import no.java.ems.server.domain.EmsServer;
import no.java.ems.server.domain.EmsServerConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

import javax.servlet.*;
import java.net.URI;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsListener implements ServletContextListener, ServletRequestListener {
    private Log log = LogFactory.getLog(getClass());

    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContext context = getRequiredWebApplicationContext(sce.getServletContext());
        log.warn("Booting EMS");
        // These lookups ensure that the core beans are initialized before the rest of the Jersey stuff start up
        context.getBean("derbyService", DerbyService.class);
        context.getBean("emsServer", EmsServer.class);
        context.getBean("emsServerConfiguration", EmsServerConfiguration.class);
        log.warn("EMS web server started!");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        log.warn("EmsListener.contextDestroyed");
    }

    public void requestInitialized(ServletRequestEvent sre) {
    }

    public void requestDestroyed(ServletRequestEvent sre) {
    }
}
