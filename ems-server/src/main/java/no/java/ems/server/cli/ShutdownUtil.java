package no.java.ems.server.cli;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShutdownUtil {
    public static void waitForShutdown(final Closeable closeable) {

        final AtomicBoolean stop = new AtomicBoolean();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.err.println("Got shutdown signal...");

                synchronized (stop) {
                    stop.set(true);
                    stop.notifyAll();
                }
            }
        }));

        synchronized (stop) {
            while (!stop.get()) {
                try {
                    stop.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        try {
            closeable.close();
        } catch (Exception e) {
            System.err.println("Error while shutting down server.");
            e.printStackTrace(System.err);
        }
    }
}
