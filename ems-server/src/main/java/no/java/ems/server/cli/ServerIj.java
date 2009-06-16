package no.java.ems.server.cli;

import org.apache.derby.tools.ij;

import java.io.IOException;
import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ServerIj {
    public static void main(String[] args) throws IOException {

        System.setProperty("ij.maximumDisplayWitht", "1000");

        File db;

        if (args.length == 0) {
            String b = System.getProperty("basedir");
            db = new File(b, "../database/database/ems").getCanonicalFile();
        } else {
            db = new File(args[0]).getAbsoluteFile();
        }

        System.out.println("Opening database: " + db);
        System.setProperty("ij.protocol", "jdbc:derby:");
        System.setProperty("ij.database", db.getAbsolutePath());
        System.setProperty("ij.user", "sa");
        System.setProperty("ij.password", "");
        ij.main(args);
    }
}
