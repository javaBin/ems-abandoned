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

package no.java.ems.server.it;

import no.java.ems.server.EmsSrcEmbedder;
import no.java.ems.server.DerbyService;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.PlexusTestCase;
import static org.codehaus.plexus.PlexusTestCase.getTestFile;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.File;

/**
 * @author Trygve Laugstol
 */
public class SetupIntegrationTest {

    @Test
    public void testSetupInEmptyDirectory() throws Exception {
        File setupDirectory = PlexusTestCase.getTestFile("target/setup");

        FileUtils.deleteDirectory(setupDirectory);

        long start = System.currentTimeMillis();
        EmsSrcEmbedder embedder = new EmsSrcEmbedder(getTestFile("../ems-server"), setupDirectory);
        embedder.start();
        embedder.stop();
        long end = System.currentTimeMillis();

        System.out.println("Fresh server started in " + (end - start) + "ms");
        assertTrue(new File(setupDirectory, "database/derby").isDirectory());

        start = System.currentTimeMillis();
        embedder = new EmsSrcEmbedder(getTestFile("../ems-server"), setupDirectory);
        embedder.start();
        embedder.stop();
        end = System.currentTimeMillis();
        System.out.println("Old server started in " + (end - start) + "ms");

        for (int i = 0; i < 10; i++) {
            embedder = new EmsSrcEmbedder(getTestFile("../ems-server"), setupDirectory);
            embedder.start();
            embedder.stop();
        }
    }
}
