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

package no.java.ems.server.resources.v2;

import no.java.ems.server.EmsSrcEmbedder;
import no.java.ems.server.DerbyService;
import no.java.ems.util.TestHelper;
import org.apache.commons.lang.SystemUtils;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * 
 */
public class ServerRunner {

    public static void main(String[] args) throws Exception {

        File defaultHome = new File(SystemUtils.getUserHome(), ".ems");
        File emsHome = new File(System.getProperty("ems.home", defaultHome.getAbsolutePath()));
        emsHome.mkdirs();

        EmsSrcEmbedder emsEmbedder = new EmsSrcEmbedder(TestHelper.getBaseDir(ServerRunner.class), emsHome);

        emsEmbedder.start();
        emsEmbedder.getBean(DerbyService.class).maybeCreateTables(false);

        System.in.read();

        emsEmbedder.stop();
        System.exit(0);
    }
}
