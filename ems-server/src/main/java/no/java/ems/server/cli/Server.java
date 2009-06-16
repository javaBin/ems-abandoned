package no.java.ems.server.cli;

import no.java.ems.server.EmsServices;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;

public class Server {

    public static final String EMS_BINARY_URI = "ems.binary.uri";
    private static final String OPTION_JAAS = "jaas";
    private static final String OPTION_BINARY_URI = "binaryuri";

    public static void main(String[] args) throws Exception {
        new Server().work(args);
    }

    private void work(String[] args) throws Exception {
        setupLogging();
        String defaultBinaryURI = System.getProperty(EMS_BINARY_URI);

        EmsCommandLine commandLine = new EmsOptions("ems-server").
            addEmsOption(OPTION_JAAS, true, "JAAS login configuration. Enables security.").
            addEmsOption(OPTION_BINARY_URI, true, "Sets the binary uri for where EMS can retrieve the data files. Default: " + defaultBinaryURI).
            parse(args);

        if (commandLine == null) {
            return;
        }

        String jaas = commandLine.getOptionValue(OPTION_JAAS);
        File emsHome = commandLine.getEmsHome();
        String binaryURI = commandLine.getOptionValue(OPTION_BINARY_URI, defaultBinaryURI);
        if (binaryURI != null) {
            System.setProperty(EMS_BINARY_URI, binaryURI);
        }

        if (jaas != null) {
            System.setProperty("java.security.auth.login.config", jaas);
        }

        EmsServices services = new EmsServices(emsHome, 3000, true, false, 3001, jaas != null);
        services.getDerbyService().maybeCreateTables(System.getProperty("ems.drop-tables") != null);
        ShutdownUtil.waitForShutdown(services);
    }

    public static void setupLogging() {
        try {
            LogManager.getLogManager().readConfiguration(Server.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
