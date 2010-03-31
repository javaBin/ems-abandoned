package no.java.ems.client.swing;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.swing.contacts.ContactEditor;
import no.java.ems.client.swing.contacts.ContactListEditor;
import no.java.ems.client.swing.events.EventEditor;
import no.java.ems.client.swing.events.EventListEditor;
import no.java.ems.client.swing.search.SearchPanel;
import no.java.ems.client.swing.sessions.SessionEditor;
import no.java.ems.domain.AbstractEntity;
import no.java.ems.domain.Person;
import no.java.ems.domain.Session;
import no.java.swing.*;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class EmsRootPanel implements InitSequence {
    private EmsTabbedPane tabs;
    private StatusBar statusBar;
    private ContactListEditor contactListEditor;
    private EventListEditor eventListEditor;
    private SearchPanel searchComponent;
    private Action saveAction;
    private Action exitAction;
    private Action undoAction;
    private Action redoAction;
    private Action logInAction;
    private Action logOutAction;
    private boolean authenticated;
    private EmsClient client;

    public EmsRootPanel(EmsClient client) {
        this.client = client;
        SwingHelper.initialize(this);
    }

    public void edit(final AbstractEntity entity) {
        Validate.notNull(entity, "Entity may not be null");
        AbstractEditor editor;
        if (entity.getHandle() != null) {
            editor = tabs.selectEditor(entity.getHandle().getURI());
            if (editor != null) {
                return;
            }
        }

        if (entity instanceof Person) {
            editor = new ContactEditor((Person)entity);
        } else if (entity instanceof no.java.ems.domain.Event) {
            editor = new EventEditor((no.java.ems.domain.Event)entity);
        } else if (entity instanceof Session) {
            editor = new SessionEditor((Session)entity);
        } else {
            throw new IllegalArgumentException("Unable to create an editor for entity type: " + entity.getClass().getName());
        }
        tabs.addSelectedTab(editor);
    }


    public void close(final AbstractEntity entity) {
        for (int index = tabs.getTabCount() - 1; index >= 0; index--) {
            Component component = tabs.getComponentAt(index);
            if (component instanceof EntityEditor) {
                EntityEditor editor = (EntityEditor)component;
                if (editor.getEntity() == entity) {
                    editor.willClose();
                    tabs.remove(index);
                }
            }
            if (component instanceof EventEditor) {
                EventEditor editor = (EventEditor)component;
                if (editor.getEvent() == entity) {
                    editor.willClose();
                    tabs.remove(index);
                }
            }
        }
    }
    public StatusBar getStatusBar() {
        return statusBar;
    }
    
    public void initModels() {
    }

    public void initActions() {
        saveAction = new SaveAction();
        exitAction = new ExitAction();
        undoAction = new DelegatingAction("undo", tabs);
        redoAction = new DelegatingAction("redo", tabs);
        logInAction = new LogInAction();
        logOutAction = new LogOutAction();
        SwingHelper.bindAction(exitAction, client.getMainFrame().getRootPane(), (KeyStroke)exitAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingHelper.bindAction(undoAction, client.getMainFrame().getRootPane(), (KeyStroke)undoAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingHelper.bindAction(redoAction, client.getMainFrame().getRootPane(), (KeyStroke)redoAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void initComponents() {
        contactListEditor = new ContactListEditor();
        eventListEditor = new EventListEditor();
        searchComponent = new SearchPanel();
        statusBar = new StatusBar();
        tabs = new EmsTabbedPane();
        tabs.addTab(searchComponent);
        tabs.addTab(contactListEditor);
        tabs.addTab(eventListEditor);
        tabs.getActionMap().put("undo", DefaultUndoManager.getInstance(null).getUndoAction());
        tabs.getActionMap().put("redo", DefaultUndoManager.getInstance(null).getRedoAction());

        getStatusBar().setCurrentPrincipal(null);
    }

    public void initBindings() {
    }

    public void initListeners() {
        DebugGlassPane.install(tabs);
        SwingHelper.bindAction(
                saveAction,
                tabs,
                (KeyStroke)saveAction.getValue(Action.ACCELERATOR_KEY),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void initLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(new ApplicationHeader(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);
        root.setPreferredSize(new Dimension(1200, 800));
        FrameView view = client.getMainView();
        view.setComponent(root);
        view.setMenuBar(createMenuBar());
        client.show(view);
    }

    public void initState() {
        tabs.transferFocus();
    }

    public EmsTabbedPane getComponent() {
        return tabs;
    }
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu();
        fileMenu.setName("menus.file");
        fileMenu.add(new DelegatingAction("new", tabs));
        fileMenu.addSeparator();
        fileMenu.add(new DelegatingAction("open", tabs));
        fileMenu.addSeparator();
        fileMenu.add(new DelegatingAction("refresh", tabs));
        fileMenu.addSeparator();
        fileMenu.add(new CloseTabAction());
        fileMenu.addSeparator();
        fileMenu.add(saveAction);
        fileMenu.addSeparator();
        fileMenu.add(logInAction);
        fileMenu.add(logOutAction);
        if (!SystemUtils.IS_OS_MAC) {
            fileMenu.addSeparator();
            fileMenu.add(exitAction);
        }
        menuBar.add(fileMenu);
        JMenu editMenu = new JMenu();
        editMenu.setName("menus.edit");
        editMenu.add(undoAction);
        editMenu.add(redoAction);
        editMenu.addSeparator();
        editMenu.add(new DelegatingAction("cut", tabs));
        editMenu.add(new DelegatingAction("copy", tabs));
        editMenu.add(new DelegatingAction("paste", tabs));
        editMenu.add(new DelegatingAction("delete", tabs));
        editMenu.addSeparator();
        editMenu.add(new DelegatingAction("addTags", tabs));
        editMenu.add(new DelegatingAction("replaceTags", tabs));
        editMenu.add(new DelegatingAction("addKeywords", tabs));
        editMenu.add(new DelegatingAction("replaceKeywords", tabs));
        menuBar.add(editMenu);
        if (SystemUtils.IS_OS_MAC) {
            for (int index = 0; index < menuBar.getMenuCount(); index++) {
                removeIcons(menuBar.getMenu(index));
            }
        }
        return menuBar;
    }
    private void removeIcons(final JMenu menu) {
        for (int index = 0; index < menu.getMenuComponentCount(); index++) {
            Component item = menu.getMenuComponent(index);
            if (item instanceof JMenu) {
                removeIcons((JMenu)item);
            }
            if (item instanceof JMenuItem) {
                ((JMenuItem)item).setIcon(null);
            }
        }
    }
    private class CloseTabAction extends ConfiguredAction {

        public CloseTabAction() {
            super("tabs.close");
            tabs.addChangeListener(
                    new ChangeListener() {
                        public void stateChanged(final ChangeEvent event) {
                            setEnabled(tabs.getSelectedIndex() > 1);
                        }
                    }
            );
            setEnabled(false);
        }

        public void actionPerformed(final ActionEvent event) {
            if (tabs.getSelectedIndex() > 1) {
                AbstractEditor editor = (AbstractEditor)tabs.getComponentAt(tabs.getSelectedIndex());
                editor.willClose();
                tabs.remove(editor);
            }
        }
    }

    private class SaveAction extends ConfiguredAction {

        private SaveAction() {
            super("save");
        }

        public void actionPerformed(final ActionEvent event) {
            for (int index = 0; index < tabs.getTabCount(); index++) {
                Component component = tabs.getTabComponentAt(index);
                if (component instanceof TabComponent) {
                    // todo: binding.save() does not appear to work with "text_ON_ACTION_OR_FOCUS_LOST". We force a focus event on binding listeners. Find a better way
                    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
                    for (FocusListener listener : focusOwner.getFocusListeners()) {
                        if (listener.getClass().getName().startsWith("org.jdesktop.swingbinding.adapters")) {
                            listener.focusLost(
                                    new FocusEvent(
                                            focusOwner,
                                            FocusEvent.FOCUS_LOST,
                                            false,
                                            focusOwner
                                    )
                            );
                        }
                    }
                }
            }
            boolean b = Entities.getInstance().hasModifications();
            if (b) {
                Task task = Entities.getInstance().createSaveChangesTask();
                task.setInputBlocker(
                        new Task.InputBlocker(task, Task.BlockingScope.ACTION, this) {
                            protected void block() {
                                setEnabled(false);
                            }

                            protected void unblock() {
                                setEnabled(true);
                            }
                        }
                );
                client.getContext().getTaskService().execute(task);
            }
        }

    }

    private class ExitAction extends ConfiguredAction {

        private ExitAction() {
            super("exit");
        }

        public void actionPerformed(final ActionEvent event) {
            client.exit(event);
        }
    }

    public static AuthenticationDialog runAuthenticationDialog(Component component) {
        AuthenticationDialog message = new AuthenticationDialog(EmsClient.getInstance());

        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(component, message, "Authenticate", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null)) {
            return message;
        }

        return null;
    }

    private class LogInAction extends ConfiguredAction {
        private LogInAction() {
            super("login");
        }

        public void actionPerformed(ActionEvent e) {
            AuthenticationDialog message = runAuthenticationDialog(client.getMainFrame());
            if (message != null) {
                getStatusBar().setCurrentPrincipal(message.getUsername());
                //emsClient.setCredentials(message.getUsername(), message.getPassword());
                authenticated = true;
            }
        }

        public boolean isEnabled() {
            return !authenticated;
        }
    }

    private class LogOutAction extends ConfiguredAction {
        private LogOutAction() {
            super("logout");
        }

        public void actionPerformed(ActionEvent e) {
            //emsClient.setCredentials(null, null);
            authenticated = false;
        }

        public boolean isEnabled() {
            return !logInAction.isEnabled();
        }
    }


    static class AuthenticationDialog extends JPanel {

        private JTextField username = new JTextField(12);
        private JTextField password = new JPasswordField(12);

        public AuthenticationDialog(final EmsClient client) {
            FormLayout layout = new FormLayout("r:d, 2dlu, f:d:g", "p, 4dlu, p, 2dlu, p, 4dlu, p");
            layout.setRowGroups(new int[][] {{1, 7}, {3, 5}});
            CellConstraints cc = new CellConstraints();

            setLayout(layout);

            JLabel serverLabel = new JLabel(client.getText("no.java.ems.client.swing.LoginPanel.headerLabel.text", client.getHost()));
            serverLabel.putClientProperty("JComponent.sizeVariant", "small");
            add(serverLabel, cc.xyw(1, 1, 3));

            add(new JLabel(client.getText("no.java.ems.client.swing.LoginPanel.usernameLabel.text")), cc.xy(1, 3));
            add(username, cc.xy(3, 3));
            add(new JLabel(client.getText("no.java.ems.client.swing.LoginPanel.passwordLabel.text")), cc.xy(1, 5));
            add(password, cc.xy(3, 5));

            add(new CapsLockLabel(client.getText("no.java.ems.client.swing.LoginPanel.capsLockLabel.text")), cc.xyw(1, 7, 3));

            // TODO: Make username input focused
        }

        public String getUsername() {
            return username.getText();
        }

        public String getPassword() {
            return password.getText();
        }

        private static class CapsLockLabel extends JLabel {
            public CapsLockLabel(String pText) {
                super(pText);
                setVisible(false);
                putClientProperty("JComponent.sizeVariant", "small");
                try {
                  Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
                  new Timer(
                      100,
                      new ActionListener() {
                        public void actionPerformed(final ActionEvent pEvent) {
                          setVisible(Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK));
                        }
                      }
                  ).start();
                } catch (UnsupportedOperationException ignore) {
                  // getLockingKeyState() not supported for this platform
                }
            }
        }
    }    
}
