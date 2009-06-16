package no.java.swing;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import javax.swing.*;

/**
 * Creates a configured command. Save some lines of code compared to using {@link no.java.swing.ConfigurationUtil#configureAction(javax.swing.Action,String,org.jdesktop.application.ResourceMap) configureAction()} directly.
 * <pre>
 * Action action = new ConfiguredAction("no.java.ExampleAction") {
 *   public void actionPerformed(final ActionEvent event) { }
 * };
 * </pre>
 *
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>.
 * @see no.java.swing.ConfigurationUtil#configureAction(javax.swing.Action,String,org.jdesktop.application.ResourceMap)
 */
public abstract class ConfiguredAction extends AbstractAction {

    public static final String EXECUTING = "executing";

    /**
     * Creates and configures a new action using the {@link org.jdesktop.application.ApplicationContext#getResourceMap() default resource map}.
     *
     * @param pBaseName base name used for resource map lookups. May not be {@code null}.
     * @see no.java.swing.ConfigurationUtil#configureAction(javax.swing.Action,String,org.jdesktop.application.ResourceMap)
     */
    protected ConfiguredAction(final String pBaseName) {
        this(pBaseName, Application.getInstance().getContext().getResourceMap());
    }

    /**
     * Creates and configures a new action using the provided resource map.
     *
     * @param pBaseName    base name used for resource map lookups. May not be {@code null}.
     * @param pResourceMap resource map used to look up values. May not be {@code null}.
     * @throws IllegalArgumentException if pBaseName or pResourceMap == {@code null}.
     * @see no.java.swing.ConfigurationUtil#configureAction(javax.swing.Action,String,org.jdesktop.application.ResourceMap)
     */
    protected ConfiguredAction(final String pBaseName, final ResourceMap pResourceMap) {
        ConfigurationUtil.configureAction(this, pBaseName, pResourceMap);
    }
}
