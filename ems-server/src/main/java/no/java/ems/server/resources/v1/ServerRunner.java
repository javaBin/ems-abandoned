package no.java.ems.server.resources.v1;

import no.java.ems.server.EmsSrcEmbedder;
import no.java.ems.server.DerbyService;
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

        EmsSrcEmbedder emsEmbedder = new EmsSrcEmbedder(new File("."), emsHome);

        emsEmbedder.start();
        emsEmbedder.getBean(DerbyService.class).maybeCreateTables(false);

        System.in.read();

        emsEmbedder.stop();
        System.exit(0);
    }
}
