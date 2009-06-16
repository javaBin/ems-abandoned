package no.java.ems.server.cli;

import no.java.ems.server.EmsServices;
import org.apache.derby.drda.NetworkServerControl;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ServerNet {
    public static void main(String[] args) throws Exception {
        EmsCommandLine commandLine = new EmsOptions("ems-server-net").
            parse(args);

        if (commandLine == null) {
            return;
        }

        EmsServices emsServices = new EmsServices(commandLine.getEmsHome(), 0, true, false,
            NetworkServerControl.DEFAULT_PORTNUMBER, false);

        ShutdownUtil.waitForShutdown(emsServices);
    }
}
