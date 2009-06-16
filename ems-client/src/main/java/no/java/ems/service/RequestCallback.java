package no.java.ems.service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RequestCallback {
    private Map<Integer, Runnable> codeRunnables = new HashMap<Integer, Runnable>();

    protected void addCodeCallback(int code, Runnable callable) {
        codeRunnables.put(code, callable);
    }

    public void onStart(String url) {
    }

    public void onComplete(String url, int code) {
        Runnable runnable = codeRunnables.get(code);

        if (runnable != null) {
            runnable.run();
            return;
        }

        /*
         * By default, throw an exception if the return code was outside the 200 range
         */
        if (code < 200 || code >= 300) {
            throw new IllegalStateException(url + " returned " + code + ".");
        }
    }
}
