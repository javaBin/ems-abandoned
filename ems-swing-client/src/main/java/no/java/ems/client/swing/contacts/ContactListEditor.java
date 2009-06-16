package no.java.ems.client.swing.contacts;

import no.java.ems.client.swing.Entities;
import no.java.ems.client.swing.EntityListEditor;
import no.java.ems.client.swing.binding.EmailConverter;
import no.java.ems.client.swing.binding.LanguageConverter;
import no.java.ems.client.swing.binding.ListConverter;
import no.java.ems.domain.Person;
import org.jdesktop.application.Task;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class ContactListEditor extends EntityListEditor<Person> {

    private Action googleAction;
    private Action emailAction;

    public ContactListEditor() {
        initialize();
    }

    @Override
    public boolean isClosable() {
        return false;
    }

    @Override
    public void initActions() {
        super.initActions();
        googleAction = new GoogleAction();
        emailAction = new EmailAction();
    }

    @Override
    public void initState() {
        super.initState();
        refreshAction.actionPerformed(null);
    }

    @SuppressWarnings({"unchecked"})
    protected void createColumns(final JTableBinding<Person, List<Person>, JTable> tableBinding) {
        tableBinding
                .addColumnBinding(BeanProperty.<Person, String>create("name"))
                .setColumnName(getString("columns.name"))
                .setEditable(false)
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Person, String>create("emailAddresses"))
                .setColumnName(getString("columns.email"))
                .setColumnClass(String.class)
                .setConverter(new EmailConverter())
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Person, String>create("tags"))
                .setColumnName(getString("columns.tags"))
                .setColumnClass(String.class)
                .setConverter(new ListConverter.StringListConverter())
                ;
        tableBinding
                .addColumnBinding(BeanProperty.<Person, String>create("language"))
                .setColumnName(getString("columns.foreign"))
                .setColumnClass(Boolean.class)
                .setConverter(new LanguageConverter())
                ;
    }

    protected Person createEntity() {
        return new Person();
    }

    protected ObservableList<Person> getEntityList() {
        return Entities.getInstance().getContacts();
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
        popupMenu.add(googleAction);
        popupMenu.add(emailAction);
        popupMenu.addSeparator();
        popupMenu.add(deleteAction);
        popupMenu.add(refreshAction);
        return popupMenu;
    }

    protected Task createRefreshTask() {
        return Entities.getInstance().createRefreshContactsTask();
    }

    @Override
    protected void selectionChanged() {
        boolean hasSelection = table.getSelectedRowCount() > 0;
        googleAction.setEnabled(hasSelection && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE));
        emailAction.setEnabled(hasSelection && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL));
        super.selectionChanged();
    }

    private class GoogleAction extends DefaultAction {

        public GoogleAction() {
            super("google");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    for (Person person : getSelected()) {
                        Desktop.getDesktop().browse(URI.create("http://www.google.com/search?&q=" + URLEncoder.encode(person.getName(), "ISO-8859-1")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class EmailAction extends DefaultAction {

        public EmailAction() {
            super("email");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                try {
                    for (Person person : getSelected()) {
                        if (!person.getEmailAddresses().isEmpty()) {
                            // todo: find a way to add the person name "Jane Doe" <jane@doe.com>
                            Desktop.getDesktop().mail(URI.create("mailto:" + person.getEmailAddresses().get(0).getEmailAddress()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
