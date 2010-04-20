package no.java.ems.client.swing.events;

import no.java.ems.client.swing.EmsClient;
import no.java.ems.client.swing.Entities;
import no.java.ems.client.swing.EntityListEditor;
import no.java.ems.client.swing.binding.ListConverter;
import no.java.ems.domain.Event;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;
import org.joda.time.LocalDate;

import javax.swing.*;
import java.util.List;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class EventListEditor extends EntityListEditor<Event> {

    public EventListEditor() {
        initialize();
    }

    @Override
    public boolean isClosable() {
        return false;
    }

    @Override
    public void initState() {
        super.initState();
        refreshAction.actionPerformed(null);
    }

    @SuppressWarnings({"unchecked"})
    protected void createColumns(final JTableBinding<Event, List<Event>, JTable> tableBinding) {
        tableBinding
                .addColumnBinding(BeanProperty.<Event, String>create("name"))
                .setColumnName(getString("columns.name"))
                .setEditable(false)
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Event, LocalDate>create("date"))
                .setColumnName(getString("columns.date"))
                .setEditable(false)
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Event, String>create("tags"))
                .setColumnName(getString("columns.tags"))
                .setColumnClass(String.class)
                .setConverter(new ListConverter.StringListConverter())
                ;
    }

    protected Event createEntity() {
        final Object name = JOptionPane.showInputDialog(
                getRootPane(),
                getString("createEvent.eventNameLabel"),
                null,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null
        );
        if (name != null) {
            Application.getInstance().getContext().getTaskService().execute(
                    new DefaultTask<Event, Void>("createEventTask") {
                        protected Event doInBackground() throws Exception {
                            return EmsClient.getInstance().getEmsService().saveEvent(new Event((String)name));
                        }

                        @Override
                        protected void succeeded(final Event event) {
                            super.succeeded(event);
                            Entities.getInstance().add(event);
                            EmsClient.getInstance().edit(event);
                        }
                    }
            );
        }
        return null;
    }

    protected Task createRefreshTask() {
        return Entities.getInstance().createRefreshEventsTask();
    }

    protected ObservableList<Event> getEntityList() {
        return Entities.getInstance().getEvents();
    }

    protected JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(openAction);
        popupMenu.addSeparator();
        popupMenu.add(newAction);
        popupMenu.addSeparator();
        popupMenu.add(addTagsAction);
        popupMenu.add(replaceTagsAction);
        popupMenu.addSeparator();
        popupMenu.add(deleteAction);
        popupMenu.addSeparator();
        popupMenu.add(refreshAction);
        return popupMenu;
    }

}
