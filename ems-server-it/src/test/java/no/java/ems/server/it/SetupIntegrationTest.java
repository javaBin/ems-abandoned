package no.java.ems.server.it;

import no.java.ems.server.EmsServices;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.PlexusTestCase;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.File;

/**
 * @author Trygve Laugstol
 */
public class SetupIntegrationTest {

    @Test
    public void testSetupInEmptyDirectory() throws Exception {
/*
        File setupDirectory = PlexusTestCase.getTestFile("target/setup");

        FileUtils.deleteDirectory(setupDirectory);

        long start = System.currentTimeMillis();
        EmsServices emsServices = new EmsServices(setupDirectory, 3001, 3002);
        emsServices.getDerbyService().maybeCreateTables(false);

        emsServices.stop();
        long end = System.currentTimeMillis();

        System.out.println("Fresh server started in " + (end - start) + "ms");
        assertTrue(new File(setupDirectory, "database/ems").isDirectory());

        start = System.currentTimeMillis();
        emsServices = new EmsServices(setupDirectory, 3001, 3002);

        emsServices.stop();
        end = System.currentTimeMillis();
        System.out.println("Old server started in " + (end - start) + "ms");

        for(int i = 0; i < 10; i++){
            emsServices = new EmsServices(setupDirectory, 3001, 3002);
            emsServices.stop();
        }
*/
    }
}
