/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package no.java.ems.server;

import static fj.data.Option.some;
import no.java.ems.server.domain.EmsServerConfiguration;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.File;

/**
 * An easy way to embed EMS when running from a subverison checkout.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsSrcEmbedder {
    private final File emsServerSrc;
    private final File emsHome;
    private Server server;
    private WebApplicationContext applicationContext;
    private static final int PORT = 3000;

    public EmsSrcEmbedder(File emsServerSrc, File emsHome) {
        this.emsServerSrc = emsServerSrc;
        this.emsHome = emsHome;
        if (!emsHome.exists()) {
            emsHome.mkdirs();
        }
    }

    // Used by the exec plugin in pom.xml
    public static void main(String[] args) throws Exception {
        String emsServerSrc = args[0];
        String emsHome = args[1];

        new EmsSrcEmbedder(new File(emsServerSrc), new File(emsHome)).start();
    }

    public void start() throws Exception {
        EmsServerConfiguration.defaultEmsHome = some(emsHome);

        server = new Server(PORT);
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/ems");
        webapp.setWar(new File(emsServerSrc, "src/main/webapp").getAbsolutePath());
        server.addHandler(webapp);
        server.start();

        applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(webapp.getServletContext());
    }

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public WebApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public <T> T getBean(Class<T> klass) {

        String[] strings = applicationContext.getBeanNamesForType(klass);

        if(strings.length != 1){
            throw new RuntimeException("Expected to find exactly one instance of bean of type " + klass.getName() + ", found " + strings.length);
        }

        return klass.cast(applicationContext.getBean(strings[0], klass));
    }

    public String getBaseUri() {
        return "http://localhost:" + PORT + "/ems";
    }
}
