package no.java.swing;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.Validate;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A TaskServiceMonitor displays a cancel button, a progress bar and a message label for
 * the foreground task of a {@link org.jdesktop.application.TaskService}. It is intended to
 * be displayed on the application's status bar.
 * <p/>
 * When the <em>foreground</em> task completes and there are other task running, the next task
 * is promoted to the foreground task. The <em>foreground</em> task can also be cancelled.
 * When there are mutiple tasks running, only the numer of other tasks is displayed as "(2 more)".
 * <p/>
 * You have to add some configuration to your application {@code .properties} file.
 * Example configuration:
 * <pre>
 * no.java.swing.TaskServiceMonitor.cancel.Action.icon = /cancel.png
 * no.java.swing.TaskServiceMonitor.cancel.Action.shortDescription = Cancel this task
 * no.java.swing.TaskServiceMonitor.count.text = (%s more)
 * no.java.swing.TaskServiceMonitor.messageDuration = 10000
 * </pre>
 *
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class TaskServiceMonitor extends DefaultPanel {

    private final TaskService taskService;
    private TaskListener taskListener;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private JLabel messageLabel;
    private JLabel countLabel;
    private JLabel principalLabel;
    private Timer timer;
    private Task active;

    public TaskServiceMonitor(final TaskService taskService) {
        super("no.java.swing.TaskServiceMonitor");
        Validate.notNull(taskService, "Task service may not be null");
        this.taskService = taskService;
        initialize();
    }

    public void initModels() {
    }

    public void initActions() {
    }

    public void initComponents() {
        cancelButton = new JButton();
        cancelButton.setName(getFullResourceKey("cancelButton"));
        cancelButton.setVisible(false);
        cancelButton.setFocusable(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setBorder(Borders.createEmptyBorder("2,0,0,2dlu"));
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        messageLabel = new MessageLabel();
        messageLabel.setName(getFullResourceKey("messages"));
        countLabel = createLabel("count", null);
        countLabel.setVisible(false);
        principalLabel = createLabel("principal", null);
    }

    public void initBindings() {
    }

    public void initListeners() {
        taskListener = new TaskListener();
        taskService.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent event) {
                        if ("tasks".equals(event.getPropertyName())) {
                            List taskList = (List)event.getNewValue();
                            if (active != null && !taskList.contains(active)) {
                                setActive((Task)(taskList.isEmpty() ? null : taskList.get(0)));
                            }
                            if (active == null && !taskList.isEmpty()) {
                                setActive((Task)taskList.get(0));
                            }
                            countLabel.setText(getString("count.text", taskList.size() - 1));
                            countLabel.setVisible(taskList.size() > 1);
                        }
                    }
                }
        );
        timer = new Timer(
                getResourceMap().getInteger(getFullResourceKey("messageDuration")),
                new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        messageLabel.setText(null);
                    }
                }
        );
        timer.setRepeats(false);
        cancelButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        if (active != null && active.getUserCanCancel()) {
                            active.cancel(true);
                        }
                    }
                }
        );
    }

    public void initLayout() {
        CellConstraints cc = new CellConstraints();
        setLayout(new FormLayout("p,p,5dlu,f:0:g,2dlu,p, 200px", "d"));
        add(cancelButton, cc.xy(1, 1));
        add(progressBar, cc.xy(2, 1));
        add(messageLabel, cc.xy(4, 1));
        add(countLabel, cc.xy(6, 1));
        add(principalLabel, cc.xy(7, 1));
        setPreferredSize(getPreferredSize());
    }

    public void initState() {
    }

    public Task getActive() {
        return active;
    }

    public void setMessage(final String message) {
        messageLabel.setForeground(UIManager.getColor("Label.foreground"));
        messageLabel.setText(message);
    }

    public void setErrorMessage(final String message) {
        messageLabel.setForeground(Color.RED);
        messageLabel.setText(message);
    }

    public void setCurrentPrincipal(final String principal) {
        principalLabel.setText(principal == null ? 
            getString("principal.anonymous") :
            getString("principal.authenticated", principal));
    }

    private void setActive(final Task task) {
        if (active != null) {
            active.removePropertyChangeListener(taskListener);
        }
        active = task;
        progressBar.setValue(active == null ? 0 : active.getProgress());
        progressBar.setIndeterminate(active != null && active.getProgress() == 0);
        progressBar.setVisible(active != null);
        if (active != null) {
            messageLabel.setForeground(UIManager.getColor("Label.foreground"));
            messageLabel.setText(active.getMessage());
        } else {
            if (timer.isRunning()) {
                timer.stop();
            }
            timer.start();
        }
        cancelButton.setVisible(active != null && active.getUserCanCancel());
        if (active != null) {
            active.addPropertyChangeListener(taskListener);
        }
    }

    private class TaskListener implements PropertyChangeListener {

        public void propertyChange(final PropertyChangeEvent event) {
            if ("progress".equals(event.getPropertyName())) {
                progressBar.setIndeterminate(false);
                progressBar.setValue((Integer)event.getNewValue());
            }
            if ("message".equals(event.getPropertyName())) {
                messageLabel.setText((String)event.getNewValue());
            }
            if ("state".equals(event.getPropertyName()) && Task.StateValue.DONE == event.getNewValue()) {
                Task task = (Task)event.getSource();
                try {
                    task.get();
                } catch (ExecutionException e) {
                    messageLabel.setForeground(Color.RED);
                } catch (Exception ignore) {
                    // ignore: task was cancelled or interrupted
                }
            }
        }
    }

    private static class MessageLabel extends JLabel {
        @Override
        public void setText(final String text) {
            super.setText(text == null || text.isEmpty() ? " " : text);
            setToolTipText(text == null || text.isEmpty() ? null : text);
        }
    }
}
