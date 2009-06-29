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

package no.java.ems.server.domain;

import fj.data.Option;
import static fj.data.Option.*;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.sql.DataSource;
import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsServerConfiguration {
    private final File emsHome;
    private final File binariesHome;
    private final File solrHome;

    private final File derbyHome;
    private final Option<Integer> derbyPort;

    public EmsServerConfiguration(File emsHome, Option<Integer> derbyPort) {
        this.emsHome = emsHome.getAbsoluteFile();
        binariesHome = new File(emsHome, "database/binaries");
        solrHome = new File(emsHome, "database/solr");
        derbyHome = new File(emsHome, "database/derby");
        this.derbyPort = derbyPort;
    }

    public File getEmsHome() {
        return emsHome;
    }

    public File getBinaryStorageDirectory() {
        return binariesHome;
    }

    public File getSolrHome() {
        return solrHome;
    }

    public File getDerbyHome() {
        return derbyHome;
    }

    public Option<Integer> getDerbyPort() {
        return derbyPort;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    /**
     * System configurable default ems home. Used for testing purposes.
     */
    public static Option<File> defaultEmsHome;

    public static Option<Integer> defaultDerbyPort = none();

    static {
        String emsHome = System.getProperty("ems.home");

        defaultEmsHome = emsHome != null ? some(new File(emsHome).getAbsoluteFile()) : Option.<File>none();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public final static class ConfigurationFactoryBean implements FactoryBean {
        private Log log = LogFactory.getLog(getClass());

        private Option<File> emsHome = none();
        private Option<Integer> derbyPort = none();

        public void setEmsHome(File emsHome) {
            this.emsHome = fromNull(emsHome);
        }

        public void setDerbyPort(int derbyPort) {
            this.derbyPort = Option.some(derbyPort);
        }

        // -----------------------------------------------------------------------
        // FactoryBean Implementation
        // -----------------------------------------------------------------------

        public Object getObject() throws Exception {
            emsHome = emsHome.orElse(defaultEmsHome);

            if (emsHome.isNone()) {
                throw new Exception("Missing required setting: emsHome");
            }

            File emsHome = this.emsHome.some();

            if (!emsHome.isDirectory()) {
                throw new Exception("EMS Home is not a directory: " + emsHome.getAbsolutePath());
            }

            EmsServerConfiguration configuration = new EmsServerConfiguration(emsHome, derbyPort.orElse(defaultDerbyPort));

            log.info( "EMS Configuration:" );
            log.info( "EMS home: " + configuration.getEmsHome() );
            log.info( "Derby home: " + configuration.getDerbyHome() );

            return configuration;
        }

        public Class getObjectType() {
            return EmsServerConfiguration.class;
        }

        public boolean isSingleton() {
            return true;
        }
    }

    public final static class DataSourceFactoryBean implements FactoryBean {

        private EmsServerConfiguration configuration;

        public void setConfiguration(EmsServerConfiguration configuration) {
            this.configuration = configuration;
        }

        // -----------------------------------------------------------------------
        // FactoryBean Implementation
        // -----------------------------------------------------------------------

        public Object getObject() throws Exception {
            EmbeddedConnectionPoolDataSource dataSource = new EmbeddedConnectionPoolDataSource();
            dataSource.setCreateDatabase("create");
            dataSource.setDatabaseName(configuration.getDerbyHome().getAbsolutePath());
            dataSource.setUser("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        public Class getObjectType() {
            return DataSource.class;
        }

        public boolean isSingleton() {
            return true;
        }
    }
}
