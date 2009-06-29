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
                            return EmsClient.getInstance().getClientService().saveEvent(new Event((String)name));
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
