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

package no.java.ems.client.swing.sessions;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.swing.Entities;
import no.java.ems.domain.Person;
import no.java.swing.AutoCompleter;
import no.java.swing.DefaultPanel;
import no.java.swing.DocumentAdapter;
import no.java.swing.ExpandablePanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class ContactSelector extends DefaultPanel {

    private JTextField nameField;
    private Person selected;
    private JDialog dialog;
    private JButton addButton;
    private JButton cancelButton;
    private Action addAction;
    private Action cancelAction;
    private boolean confirmed;
    private ExpandablePanel info;

    public ContactSelector() {
        initialize();
        // placed here to ensure injectComponents() has been performed
        setPreferredSize(getPreferredSize());
        info.setExpanded(false, false);
    }

    public Person showSelectDialog(final Component component) {
        // todo: there is bug with repainting (at least on the beta 1.6 jdk on mac) when recycling the dialog
        confirmed = false;
        selected = null;
        info.setExpanded(false, false);
        nameField.setText("");
        if (dialog == null) {
            dialog = new JDialog((Window)(component instanceof Window ? component : SwingUtilities.getWindowAncestor(component)), Dialog.ModalityType.DOCUMENT_MODAL);
            dialog.setTitle(getString("dialog.title"));
            dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            dialog.setContentPane(this);
            dialog.getRootPane().setDefaultButton(addButton);
            dialog.setResizable(false);
            dialog.pack();
        }
        nameField.requestFocusInWindow();
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
        if (confirmed) {
            if (selected != null && selected.getName().equals(nameField.getText())) {
                return selected;
            } else {
                // todo: add to list of contacts and possibly open editor. derive email address if present "<>"
                return new Person(nameField.getText());
            }
        }
        return null;
    }

    public void initModels() {
    }

    public void initActions() {
        addAction = new AddAction();
        cancelAction = new CancelAction();
    }

    public void initComponents() {
        info = new ExpandablePanel(createLabel("newContactWarning", null, JLabel.CENTER), ExpandablePanel.Direction.Down);
        addButton = new JButton(addAction);
        cancelButton = new JButton(cancelAction);
        nameField = new JTextField(30);
        nameField.putClientProperty("JTextField.variant", "search");
        new AutoCompleter<Person>(nameField, Entities.getInstance().getContacts()) {
            @Override
            protected void valueSelected(final Person person) {
                nameField.setText(person.getName());
                setSelected(person);
            }

            @Override
            protected String displayValue(final Person person) {
                if (person.getEmailAddresses().isEmpty()) {
                    return person.getName();
                } else {
                    return String.format("%s <%s>", person.getName(), person.getEmailAddresses().get(0).getEmailAddress());
                }
            }

            @Override
            protected void escapePressed() {
                selected = null;
                nameField.setText("");
                dialog.setVisible(false);
            }
        };
    }

    public void initBindings() {
    }

    public void initListeners() {
        nameField.getDocument().addDocumentListener(
                new DocumentAdapter() {
                    protected void documentChanged(final DocumentEvent event) {
                        addAction.setEnabled(!nameField.getText().trim().isEmpty());
                        selected = null;
                        indicate();
                    }
                }
        );
    }

    public void initLayout() {
        setBorder(Borders.DIALOG_BORDER);
        FormLayout layout = new FormLayout("p,3dlu,f:p:g,2dlu,p,1dlu,p", "p,3dlu,p");
        layout.setColumnGroups(new int[][]{{5, 7}});
        setLayout(layout);
        CellConstraints cc = new CellConstraints();
        add(info, cc.xyw(1, 3, 7));
        add(createLabel("nameLabel", nameField), cc.xy(1, 1));
        add(nameField, cc.xy(3, 1));
        add(addButton, cc.xy(5, 1));
        add(cancelButton, cc.xy(7, 1));
    }

    public void initState() {
    }

    private void setSelected(final Person person) {
        selected = person;
        indicate();
    }

    private void indicate() {
        boolean nameMatches = selected != null && selected.getName().equals(nameField.getText());
        nameField.setForeground(nameMatches ? Color.GREEN.darker() : Color.BLUE.darker());
        info.setExpanded(!nameMatches && addAction.isEnabled());
    }

    private class AddAction extends DefaultAction {

        public AddAction() {
            super("add");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            if (selected == null) {
                if (
                        JOptionPane.showConfirmDialog(
                                dialog,
                                getString("confirmNewContact.text", nameField.getText()),
                                getString("confirmNewContact.title"),
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE
                        ) != JOptionPane.OK_OPTION) {
                    return;
                }
            }
            confirmed = true;
            dialog.setVisible(false);
        }

    }

    private class CancelAction extends DefaultAction {

        public CancelAction() {
            super("cancel");
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            dialog.setVisible(false);
        }

    }

}
