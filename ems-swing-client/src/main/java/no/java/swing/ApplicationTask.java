package no.java.swing;

import org.apache.commons.lang.Validate;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public abstract class ApplicationTask<T, V> extends Task<T, V> {

    private final String resourcePrefix;

    protected ApplicationTask(final String key) {
        this(Application.getInstance(), key);
    }

    protected ApplicationTask(final Application application, final String key) {
        super(application);
        Validate.notNull(key, "Key may not be null");
        resourcePrefix = key;
        String message = getString("initial", getTaskTitle());
        if (message == null) {
            message = getResourceMap().getString("ApplicationTask.defaults.initial", getTaskTitle(), getExecutionDuration(TimeUnit.MILLISECONDS));
        }
        setMessage(message);
    }

    public String getTaskTitle() {
        String name = getString("title");
        return name == null ? getClass().getSimpleName() : name;
    }

    protected String getFullResourceKey(final String key) {
        Validate.notNull(key, "Resource key may not be null");
        return resourcePrefix + "." + key;
    }

    protected String getString(final String key, Object... params) {
        Validate.notNull(key, "Resource key may not be null");
        return getResourceMap().getString(getFullResourceKey(key), params);
    }

    @Override
    protected void cancelled() {
        String message = getString("cancelled", getTaskTitle(), getExecutionDuration(TimeUnit.MILLISECONDS));
        if (message == null) {
            message = getResourceMap().getString("ApplicationTask.defaults.cancelled", getTaskTitle(), getExecutionDuration(TimeUnit.MILLISECONDS));
        }
        setMessage(message);
    }

    @Override
    protected void succeeded(final T result) {
        String message = getString("succeeded", getTaskTitle(), getExecutionDuration(TimeUnit.MILLISECONDS));
        if (message == null) {
            message = getResourceMap().getString("ApplicationTask.defaults.succeeded", getTaskTitle(), getExecutionDuration(TimeUnit.MILLISECONDS));
        }
        setMessage(message);
    }

    @Override
    protected void failed(final Throwable cause) {
        System.out.println("ApplicationTask.failed");
        String message = getString("failed", getTaskTitle(), cause.getLocalizedMessage(), getExecutionDuration(TimeUnit.MILLISECONDS));
        if (message == null) {
            message = getResourceMap().getString("ApplicationTask.defaults.failed", getTaskTitle(), cause.getLocalizedMessage(), getExecutionDuration(TimeUnit.MILLISECONDS));
        }
        setMessage(message);
        super.failed(cause);
    }
}
