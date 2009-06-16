package no.java.ems.client.swing;

import com.jgoodies.forms.factories.Borders;
import no.java.swing.DefaultPanel;
import no.java.swing.TaskServiceMonitor;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class StatusBar extends DefaultPanel {

    private TaskServiceMonitor taskServiceMonitor;

    public StatusBar() {
        initialize();
    }

    public void initModels() {
    }

    public void initActions() {
    }

    public void initComponents() {
        taskServiceMonitor = new TaskServiceMonitor(getTaskService());
    }

    public void initBindings() {
    }

    public void initListeners() {
    }

    public void initLayout() {
        setBorder(
                new CompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                        Borders.createEmptyBorder("3dlu,2dlu,3dlu,3dlu")
                )
        );
        setLayout(new BorderLayout());
        add(taskServiceMonitor, BorderLayout.CENTER);
    }

    public void initState() {
    }

    public void setMessage(final String message) {
        taskServiceMonitor.setMessage(message);
    }

    public void setErrorMessage(String message) {
        taskServiceMonitor.setErrorMessage(message);
    }

    public void setCurrentPrincipal(String message) {
        taskServiceMonitor.setCurrentPrincipal(message);
    }
}
