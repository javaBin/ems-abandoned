package no.java.ems.client.swing;

import no.java.ems.client.RestEmsService;
import no.java.ems.client.swing.contacts.ContactEditor;
import no.java.ems.client.swing.contacts.ContactListEditor;
import no.java.ems.client.swing.events.EventEditor;
import no.java.ems.client.swing.events.EventListEditor;
import no.java.ems.client.swing.sessions.SessionEditor;
import no.java.ems.domain.AbstractEntity;
import no.java.ems.domain.Event;
import no.java.ems.domain.Person;
import no.java.ems.domain.Session;
import no.java.ems.service.EmsService;
import no.java.ems.service.ForbiddenException;
import no.java.ems.service.RequestCallback;
import no.java.ems.service.UnauthorizedException;
import no.java.swing.ConfiguredAction;
import no.java.swing.DebugGlassPane;
import no.java.swing.DefaultUndoManager;
import no.java.swing.DelegatingAction;
import no.java.swing.InitSequence;
import no.java.swing.SwingHelper;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.logging.Logger;

/**
 * <ul>
 * <li>todo: dashboard (track, approved, missing) find correspodance from this contact
 * <li>todo: undo/redo: editing a speaker must flag the session as modified
 * <li>todo: reopen tabs on launch
 * <li>todo: autocomplete in table cell editors
 * <li>todo: dashboard (summaries for state, format, foreign etc)
 * <li>todo: add support for custom tag columns
 * <li>todo: add support row highlighting (tag watch)
 * <li>todo: context menu on tab title
 * <li>todo: macify
 * <li>todo: better email handling in cell editor (do not commit when unparsable/stay in editor)
 * <li>todo: table improvements: dismiss editor on selection change, filter, sorting, tab change etc.
 * <li>todo: better tag and keyword handling (ensure delimiter may not be part of tag/keyword)
 * <li>todo: about dialog / info (kudos, license etc.)
 * <li>todo: drag attachements out of application
 * <li>todo: help / tutorial
 * <li>todo: add "really attach n files/google/email/xxx to n people/contacts/yyy" sanity checks
 * <li>todo: reorder tabs by dragging
 * <li>todo: double click column edge to resize (prefrerred column width)
 * <li>todo: initial table column widths
 * <li>todo: sessions are left in Entities when event is closed
 * <li>todo: remove save/refresh button: autosave and autorefresh (rss/etags?)
 * <li>todo: (per user) session voting
 * <li>todo: new session (inherit speakers)
 * <li>todo: new speaker that doews not have email: notify
 * </ul>
 *
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class EmsClient extends SingleFrameApplication implements InitSequence {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private String connectionURI;
    private JTabbedPane tabs;
    private StatusBar statusBar;
    private EmsService emsService;
    private ContactListEditor contactListEditor;
    private EventListEditor eventListEditor;
    private Action saveAction;
    private Action exitAction;
    private Action undoAction;
    private Action redoAction;
    private Action logInAction;
    private Action logOutAction;
    private boolean authenticated;

    public static void main(final String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
//        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Event Management Suite");
        launch(EmsClient.class, args);
    }

    public static EmsClient getInstance() {
        return getInstance(EmsClient.class);
    }

    public static ResourceMap getResourceMap() {
        return getInstance().getContext().getResourceMap();
    }

    public static String getText(final String key, final Object... args) {
        return getResourceMap().getString(key, args);
    }

    public void edit(final AbstractEntity entity) {
        Validate.notNull(entity, "Entity may not be null");
        AbstractEditor editor;
        for (int index = 0; index < tabs.getTabCount(); index++) {
            Component component = tabs.getTabComponentAt(index);
            if (component instanceof TabComponent) {
                TabComponent tabComponent = (TabComponent)component;
                String id = tabComponent.getTab().getId();
                if (id != null && id.equals(entity.getId())) {
                    tabs.setSelectedIndex(index);
                    return;
                }
            }
        }
        if (entity instanceof Person) {
            editor = new ContactEditor((Person)entity);
        } else if (entity instanceof Event) {
            editor = new EventEditor((Event)entity);
        } else if (entity instanceof Session) {
            editor = new SessionEditor((Session)entity);
        } else {
            throw new IllegalArgumentException("Unable to create an editor for entity type: " + entity.getClass().getName());
        }
        int index = tabs.getTabCount();
        tabs.insertTab(editor.getTitle() == null || editor.getTitle().isEmpty() ? "<Untitled>" : editor.getTitle(), editor.getIcon(), editor, null, index);
        tabs.setTabComponentAt(index, new TabComponent(editor, tabs));
        tabs.setSelectedIndex(index);
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

    public String getHost() {
        return connectionURI;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public ContactListEditor getContactsPanel() {
        return contactListEditor;
    }

    public EventListEditor getSessionsPanel() {
        return eventListEditor;
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
        SwingHelper.bindAction(exitAction, this.getMainFrame().getRootPane(), (KeyStroke)exitAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingHelper.bindAction(undoAction, this.getMainFrame().getRootPane(), (KeyStroke)undoAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingHelper.bindAction(redoAction, this.getMainFrame().getRootPane(), (KeyStroke)redoAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void initComponents() {
        contactListEditor = new ContactListEditor();
        eventListEditor = new EventListEditor();
        statusBar = new StatusBar();
        tabs = new EmsTabbedPane();
        tabs.insertTab(contactListEditor.getTitle(), contactListEditor.getIcon(), contactListEditor, null, 0);
        tabs.insertTab(eventListEditor.getTitle(), eventListEditor.getIcon(), eventListEditor, null, 1);
        tabs.setTabComponentAt(0, new TabComponent(contactListEditor, tabs));
        tabs.setTabComponentAt(1, new TabComponent(eventListEditor, tabs));
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
        FrameView view = getMainView();
        view.setComponent(root);
        view.setMenuBar(createMenuBar());
        show(view);
    }

    public void initState() {
        tabs.transferFocus();
    }

    public JTabbedPane getComponent() {
        return tabs;
    }

    public EmsService getEmsService() {
        return emsService;
    }

    @Override
    protected void initialize(final String[] args) {
        new CustomUncaughtExceptionHandler(getMainFrame());
        UIManager.put("TextArea.font", UIManager.getFont("TextField.font"));
        connectionURI = System.getProperty("ems-host", "http://localhost:3000/ems");
        emsService = new RestEmsService(connectionURI);
        emsService.setRequestCallback(new EmsClientRequestCallback());
        logger.info("Ems host address set to " + connectionURI);

        AuthenticationDialog authenticationDialog = runAuthenticationDialog(null);
        emsService.setCredentials(authenticationDialog.getUsername(), authenticationDialog.getPassword());
    }

    protected void startup() {
        SwingHelper.initialize(this);
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

    public boolean isAuthenticated() {
        return authenticated;
    }

    private static class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler, Runnable {

        private final Component referenceComponent;

        public CustomUncaughtExceptionHandler(final Component referenceComponent) {
            this.referenceComponent = referenceComponent;
            run();
        }

        public void uncaughtException(final Thread thread, final Throwable throwable) {
            throwable.printStackTrace();
            SwingHelper.displayErrorMessage(throwable, referenceComponent);
            EventQueue.invokeLater(this);
        }

        public void run() {
            Thread.currentThread().setUncaughtExceptionHandler(CustomUncaughtExceptionHandler.this);
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
//                    // todo: this is kinda naive validation... use isValid() or something...
//                    BindingGroup bindingGroup = ((TabComponent)component).getTab().getBindingGroup();
//                    for (Binding binding : bindingGroup.getBindings()) {
//                        Binding.SyncFailure syncFailure = binding.save();
//                        if (syncFailure != null && !(binding.getSourceObject() instanceof ObservableList)) {
//                            getStatusBar().setErrorMessage(syncFailure.toString());
//                            tabs.setSelectedIndex(index);
//                            return;
//                        }
//                    }
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
                getContext().getTaskService().execute(task);
            }
        }

    }

    private class ExitAction extends ConfiguredAction {

        private ExitAction() {
            super("exit");
        }

        public void actionPerformed(final ActionEvent event) {
            exit(event);
        }
    }

    public static AuthenticationDialog runAuthenticationDialog(Component component) {
        AuthenticationDialog message = new AuthenticationDialog();
        JOptionPane.showOptionDialog(component, message, "Authenticate", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
        return message;
    }

    private class LogInAction extends ConfiguredAction {
        private LogInAction() {
            super("login");
        }

        public void actionPerformed(ActionEvent e) {
            AuthenticationDialog message = runAuthenticationDialog(getMainFrame());
            getStatusBar().setCurrentPrincipal(message.getUsername());
            emsService.setCredentials(message.getUsername(), message.getPassword());
            authenticated = true;
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
            emsService.setCredentials(null, null);
            authenticated = false;
        }

        public boolean isEnabled() {
            return !logInAction.isEnabled();
        }
    }

    private static class AuthenticationDialog extends JPanel {

        private JTextField username = new JTextField();
        private JTextField password = new JPasswordField();

        public AuthenticationDialog() {
            setLayout(new GridLayout(2, 2));
            add(new JLabel("Username"));
            add(username);
            add(new JLabel("Password"));
            add(password);
        }

        public String getUsername() {
            return username.getText();
        }

        public String getPassword() {
            return password.getText();
        }
    }

    private class EmsClientRequestCallback extends RequestCallback {

        private EmsClientRequestCallback() {
            addCodeCallback(401, new Runnable() {
                public void run()  {
                    // TODO: Do fancy stuff here. Login and then re-try the call
                    throw new UnauthorizedException();
                }
            });
            addCodeCallback(403, new Runnable() {
                public void run() {
                    throw new ForbiddenException();
                }
            });
        }
    }
}
