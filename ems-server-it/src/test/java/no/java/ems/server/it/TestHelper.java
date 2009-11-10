package no.java.ems.server.it;

import java.io.File;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
abstract public class TestHelper {

    public static File getBaseDir(Class aClass) {
        String basedir = System.getProperty("basedir");
        if (basedir != null) {
            return new File(basedir);
        } else {
            File file = new File(aClass.getProtectionDomain().getCodeSource().getLocation().getPath());
            if (!file.exists()) {
                throw new RuntimeException("Unable to find basedir");
            }
            while (!new File(file, "pom.xml").exists()) {
                file = file.getParentFile();
                if (file == null) {
                    throw new RuntimeException("Unable to find basedir");
                }
            }
            return file;
        }
    }

}
