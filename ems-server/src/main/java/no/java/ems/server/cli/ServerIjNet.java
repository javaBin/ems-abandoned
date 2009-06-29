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

package no.java.ems.server.cli;

import org.apache.derby.tools.ij;

import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ServerIjNet {
    public static void main(String[] args) throws IOException {

        System.setProperty("ij.maximumDisplayWitht", "1000");

        String db = "jdbc:derby://127.0.0.1/ems";

        System.out.println("Opening database: " + db);
        System.setProperty("ij.protocol", "jdbc:derby:");
        System.setProperty("ij.database", db);
        System.setProperty("ij.user", "sa");
        System.setProperty("ij.password", "");
        ij.main(args);
    }
}
