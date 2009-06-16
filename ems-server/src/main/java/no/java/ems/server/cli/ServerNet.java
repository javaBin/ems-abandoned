package no.java.ems.server.cli;


/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ServerNet {
    public static void main(String[] args) throws Exception {
        EmsCommandLine commandLine = new EmsOptions("ems-server-net").
            parse(args);

        if (commandLine == null) {
            return;
        }

        throw new RuntimeException("Not implemented");
//        EmsServices emsServices = new EmsServices(commandLine.getEmsHome(), 0, true, false,
//            NetworkServerControl.DEFAULT_PORTNUMBER, false);
//
//        ShutdownUtil.waitForShutdown(emsServices);
    }
}
