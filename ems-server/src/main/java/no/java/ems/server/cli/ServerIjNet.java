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
