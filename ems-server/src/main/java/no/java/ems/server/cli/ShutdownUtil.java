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
