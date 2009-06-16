package no.java.ems.client.swing.events;

import no.java.ems.client.swing.*;
import no.java.ems.client.swing.binding.LanguageConverter;
import no.java.ems.client.swing.binding.ListConverter;
import no.java.ems.client.swing.binding.SpeakersConverter;
import no.java.ems.domain.Event;
import no.java.ems.domain.Session;
import org.apache.commons.lang.Validate;
import org.jdesktop.application.Task;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class EventEditor extends EntityListEditor<Session> {

    private final Event event;
    private Action addKeywordsAction;
    private Action replaceKeywordsAction;
    private JMenu statesMenu;
    private JMenu formatsMenu;
    private ObservableList<Session> sessions;
    private Dashboard<Session> dashboard;

    public EventEditor(final Event event) {
        Validate.notNull(event, "Event may not be null");
        this.event = event;
        initialize();
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public void initModels() {
        super.initModels();
        sessions = ObservableCollections.observableList(new ArrayList<Session>());
    }

    @Override
    public void initActions() {
        super.initActions();
        addKeywordsAction = new SetKeywordsAction("addKeywords");
        replaceKeywordsAction = new SetKeywordsAction("replaceKeywords");
    }

    @Override
    public void initComponents() {
        statesMenu = new JMenu("setStateMenu");
        statesMenu.setEnabled(false);
        statesMenu.setName(getFullResourceKey("setStateMenu"));
        for (Session.State state : Session.State.values()) {
            statesMenu.add(new SetSateAction(state));
        }
        formatsMenu = new JMenu("setFormatMenu");
        formatsMenu.setEnabled(false);
        formatsMenu.setName(getFullResourceKey("setFormatMenu"));
        for (Session.Format format : Session.Format.values()) {
            formatsMenu.add(new SetFormatAction(format));
        }
        getResourceMap().injectComponent(statesMenu);
        getResourceMap().injectComponent(formatsMenu);
        dashboard = new Dashboard<Session>(
                new Dashboard.LevelFilter(Session.Level.Advanced),
                new Dashboard.LevelFilter(Session.Level.Intermediate),
                new Dashboard.LevelFilter(Session.Level.Introductory),
                new Dashboard.StateFilter<Session>(Session.State.Approved),
                new Dashboard.StateFilter<Session>(Session.State.Pending),
                new Dashboard.StateFilter<Session>(Session.State.Rejected),
                createKeywordFilter("Core"),
                createKeywordFilter("Emerging"),
                createKeywordFilter("EMG"),
                createKeywordFilter("Enterprise"),
                createKeywordFilter("Experience"),
                createKeywordFilter("Method"),
                createKeywordFilter("Web")
        );

        super.initComponents();
    }

    private Dashboard.AndFilter<Session> createKeywordFilter(final String keyword) {
        return new Dashboard.AndFilter<Session>(
                keyword + ": %s",
                new Dashboard.KeywordFilter(keyword),
                new Dashboard.StateFilter<Session>(Session.State.Approved)
        );
    }

    @Override
    public void initBindings() {
        super.initBindings();
        getBindingGroup().addBinding(
                Bindings.createAutoBinding(
                        AutoBinding.UpdateStrategy.READ,
                        event,
                        BeanProperty.<Event, String>create("id"),
                        this,
                        BeanProperty.<AbstractEditor, String>create("id")
                )
        );
        getBindingGroup().addBinding(
                Bindings.createAutoBinding(
                        AutoBinding.UpdateStrategy.READ,
                        event,
                        BeanProperty.<Event, String>create("name"),
                        this,
                        BeanProperty.<AbstractEditor, String>create("title")
                )
        );
    }

    @Override
    public void initState() {
        super.initState();
        table.getModel().addTableModelListener(
                new TableModelListener() {
                    public void tableChanged(TableModelEvent e) {
                        dashboard.update(getEntityList());
                    }
                }
        );
        refreshAction.actionPerformed(null);
    }

    @Override
    protected JComponent getDashboard() {
        return dashboard;
    }

    @SuppressWarnings({"unchecked"})
    protected void createColumns(final JTableBinding<Session, List<Session>, JTable> tableBinding) {
        tableBinding
                .addColumnBinding(BeanProperty.<Session, String>create("title"))
                .setColumnName(getString("columns.title"))
                .setEditable(false)
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Session, String>create("speakers"))
                .setColumnName(getString("columns.speakers"))
                .setColumnClass(String.class)
                .setEditable(false)
                .setConverter(new SpeakersConverter())
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Session, String>create("tags"))
                .setColumnName(getString("columns.tags"))
                .setColumnClass(String.class)
                .setConverter(new ListConverter.StringListConverter())
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Session, String>create("keywords"))
                .setColumnName(getString("columns.keywords"))
                .setColumnClass(String.class)
                .setConverter(new ListConverter.StringListConverter())
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Session, String>create("level"))
                .setColumnName(getString("columns.level"))
                .setColumnClass(Session.Level.class)
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Session, String>create("format"))
                .setColumnName(getString("columns.format"))
                .setColumnClass(Session.Format.class)
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Session, String>create("state"))
                .setColumnName(getString("columns.state"))
                .setColumnClass(Session.State.class)
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Session, String>create("language"))
                .setColumnName(getString("columns.english"))
                .setColumnClass(Boolean.class)
                .setConverter(new LanguageConverter())
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Session, Boolean>create("published"))
                .setColumnName(getString("columns.published"))
                .setColumnClass(String.class)
                .setEditable(false)
                .setConverter(new PublishedConverter())
                ;
    }

    protected Session createEntity() {
        Session session = new Session();
        session.setEventURI(getEvent().getURI());
        sessions.add(session);
        return session;
    }

    protected Task createRefreshTask() {
        return new RefreshSessionsTask();
    }

    protected ObservableList<Session> getEntityList() {
        return sessions;
    }

    protected JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(openAction);
        popupMenu.addSeparator();
        popupMenu.add(newAction);
        popupMenu.addSeparator();
        popupMenu.add(addTagsAction);
        popupMenu.add(replaceTagsAction);
        popupMenu.add(addKeywordsAction);
        popupMenu.add(replaceKeywordsAction);
        popupMenu.addSeparator();
        popupMenu.add(statesMenu);
        popupMenu.add(formatsMenu);
        popupMenu.addSeparator();
        popupMenu.add(deleteAction);
        popupMenu.addSeparator();
        popupMenu.add(refreshAction);
        return popupMenu;
    }

    @Override
    protected void selectionChanged() {
        boolean hasSelection = table.getSelectedRowCount() > 0;
        addKeywordsAction.setEnabled(hasSelection);
        replaceKeywordsAction.setEnabled(hasSelection);
        statesMenu.setEnabled(hasSelection);
        formatsMenu.setEnabled(hasSelection);
        super.selectionChanged();
    }

    private class RefreshSessionsTask extends DefaultTask<List<Session>, Void> {

        public RefreshSessionsTask() {
            super("refresh");
        }

        protected List<Session> doInBackground() throws Exception {
            return EmsClient.getInstance().getClientService().getSessions(event);
        }

        @Override
        protected void succeeded(final List<Session> newSessions) {
            if (newSessions.size() == 1) {
                setMessage(getString("succeeded.singular", newSessions.size(), getExecutionDuration(TimeUnit.MILLISECONDS)));
            } else {
                setMessage(getString("succeeded.plural", newSessions.size(), getExecutionDuration(TimeUnit.MILLISECONDS)));
            }
            Set<Session> keep = new HashSet<Session>();
            for (Session newSession : newSessions) {
                Session existingSession = Entities.getInstance().getSession(newSession.getURI());
                if (existingSession == null) {
                    sessions.add(newSession);
                    keep.add(newSession);
                } else {
                    if (!sessions.contains(existingSession)) {
                        sessions.add(existingSession);
                    }
                    if (existingSession.isModified()) {
                        // todo: ask automerge/keep/replace
                        System.err.println("ignoring refresh for modified session: " + existingSession.getTitle());
                    } else {
                        try {
                            Entities.getInstance().setIgnorePropertyEvents(true);
                            existingSession.sync(newSession);
                            existingSession.setModified(false);
                        } finally {
                            Entities.getInstance().setIgnorePropertyEvents(false);
                        }
                    }
                    keep.add(existingSession);
                }
            }
            HashSet<Session> toBeDeleted = new HashSet<Session>(sessions);
            toBeDeleted.removeAll(keep);
            sessions.removeAll(toBeDeleted);
            Entities.getInstance().removeAll(toBeDeleted);
            Entities.getInstance().addAll(sessions);
        }
    }

    private class SetSateAction extends AbstractAction {

        private final Session.State state;

        public SetSateAction(final Session.State state) {
            super(state.name());
            this.state = state;
        }

        public void actionPerformed(final ActionEvent event) {
            for (int index : table.getSelectedRows()) {
                Session session = sessions.get(table.getRowSorter().convertRowIndexToModel(index));
                session.setState(state);
            }
        }

    }

    private class SetFormatAction extends AbstractAction {

        private final Session.Format format;

        public SetFormatAction(final Session.Format format) {
            super(format.name());
            this.format = format;
        }

        public void actionPerformed(final ActionEvent event) {
            for (int index : table.getSelectedRows()) {
                Session session = sessions.get(table.getRowSorter().convertRowIndexToModel(index));
                session.setFormat(format);
            }
        }

    }

    private class SetKeywordsAction extends DefaultAction {

        private final boolean replace;

        public SetKeywordsAction(final String key) {
            super(key);
            setEnabled(false);
            this.replace = false;
        }

        public void actionPerformed(final ActionEvent event) {
            String input = (String)JOptionPane.showInputDialog(
                    getRootPane(),
                    getString("label"),
                    getString("title", table.getSelectedRowCount()),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    getString("example")

            );
            if (input != null) {
                List<String> keywords = new ListConverter.StringListConverter().convertReverse(input);
                for (int index : table.getSelectedRows()) {
                    Session session = sessions.get(table.getRowSorter().convertRowIndexToModel(index));
                    if (replace) {
                        session.addKeywords(keywords);
                    } else {
                        session.addKeywords(keywords);
                    }
                }
            }
        }

    }

    private class PublishedConverter extends Converter {
        public Object convertForward(Object value) {
            if (Boolean.TRUE.equals(value)) {
                return getString("published.true");
            }
            return getString("published.false");
        }

        public Object convertReverse(Object value) {
            return null;
        }
    }
}
