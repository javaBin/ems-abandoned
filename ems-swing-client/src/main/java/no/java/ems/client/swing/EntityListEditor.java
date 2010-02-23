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

package no.java.ems.client.swing;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.swing.binding.ListConverter;
import no.java.ems.domain.AbstractEntity;
import no.java.swing.CycleSelectionAction;
import no.java.swing.DefaultUndoManager;
import no.java.swing.DocumentAdapter;
import no.java.swing.SwingHelper;
import org.apache.commons.lang.SystemUtils;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public abstract class EntityListEditor<T extends AbstractEntity> extends AbstractEditor {

    protected EntityTable table;
    protected JTextField filterTextField;
    protected JLabel filterStatusLabel;
    protected Action newAction;
    protected Action refreshAction;
    protected Action openAction;
    protected Action deleteAction;
    protected Action addTagsAction;
    protected Action replaceTagsAction;
    protected Action clearFilterAction;
    protected Timer timer;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected abstract void createColumns(final JTableBinding<T, List<T>, JTable> tableBinding);

    protected abstract T createEntity();

    protected abstract Task createRefreshTask();

    protected abstract ObservableList<T> getEntityList();

    protected abstract JPopupMenu createPopupMenu();

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected EntityListEditor() {
        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentShown(final ComponentEvent event) {
                        // this solves the focus problem when opening/selecting tabs... we should find a better way
                        filterTextField.requestFocusInWindow();
                    }
                }
        );
    }

    public void initModels() {
    }

    public void initActions() {
        refreshAction = new RefreshAction();
        newAction = new NewAction();
        openAction = new OpenAction();
        deleteAction = new DeleteAction();
        addTagsAction = new SetTagsAction("addTags", false);
        replaceTagsAction = new SetTagsAction("replaceTags", true);
        clearFilterAction = new ClearFilterAction();
    }

    public void initComponents() {
        table = new EntityTable();
        filterTextField = new JTextField(30) {
            @Override
            protected void processKeyEvent(final KeyEvent event) {
                if (!event.isConsumed() && (
                        event.getKeyCode() == KeyEvent.VK_UP
                        || event.getKeyCode() == KeyEvent.VK_DOWN
                        || event.getKeyCode() == KeyEvent.VK_ENTER
                )) {
                    table.processKeyEvent(event);
                }
                super.processKeyEvent(event);
            }
        };
        filterTextField.setName("contacts.filter");
        filterTextField.putClientProperty("JTextField.variant", "search");
        filterTextField.putClientProperty("JTextField.Search.CancelAction", clearFilterAction);
        table.setName("contacts.table");
        JPopupMenu popupMenu = createPopupMenu();
        if (popupMenu != null) {
            if (SystemUtils.IS_OS_MAC) {
                for (MenuElement menuElement : popupMenu.getSubElements()) {
                    Component item = menuElement.getComponent();
                    if (item instanceof JMenuItem) {
                        ((JMenuItem)item).setIcon(null);
                    }
                }
            }
            table.setComponentPopupMenu(popupMenu);
        }
        filterStatusLabel = new JLabel();
    }

    public void initBindings() {
        JTableBinding<T, List<T>, JTable> tableBinding = SwingBindings.createJTableBinding(
                AutoBinding.UpdateStrategy.READ,
                getEntityList(),
                table
        );
        createColumns(tableBinding);
        getBindingGroup().addBinding(tableBinding);
    }

    public void initListeners() {
        SwingHelper.bindAction(
                openAction,
                table,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_FOCUSED
        );
        SwingHelper.bindAction(
                clearFilterAction,
                this,
                (KeyStroke)clearFilterAction.getValue(Action.ACCELERATOR_KEY),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(final ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            selectionChanged();
                        }
                    }
                }
        );
        table.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(final MouseEvent event) {
                        if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2 && event.getModifiersEx() == 0) {
                            int rowIndex = table.rowAtPoint(event.getPoint());
                            int columnIndex = table.columnAtPoint(event.getPoint());
                            if (rowIndex != -1 && columnIndex != -1) {
                                if (!table.isCellEditable(rowIndex, columnIndex)) {
                                    EmsClient.getInstance().getRootPanel().edit(getEntityList().get(table.convertRowIndexToModel(rowIndex)));
                                }
                            }
                        }
                    }
                }
        );
        filterTextField.addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusGained(final FocusEvent event) {
                        filterTextField.selectAll();
                    }
                }
        );
        timer = new Timer(
                150,
                new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        table.setFilter(filterTextField.getText());
                    }
                }
        );
        timer.setRepeats(false);
        filterTextField.getDocument().addDocumentListener(
                new DocumentAdapter() {
                    protected void documentChanged(final DocumentEvent event) {
                        if (timer.isRunning()) {
                            timer.stop();
                        }
                        timer.start();
                    }
                }
        );
        CycleSelectionAction.install(table);
    }

    public void initLayout() {
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addFixedNarrow(createLabel("filter", filterTextField));
        builder.addRelatedGap();
        builder.addFixed(filterTextField);
        builder.addRelatedGap();
        builder.appendColumn("p");
        builder.add(filterStatusLabel);
        builder.nextColumn();
        builder.addGlue();
        builder.addUnrelatedGap();
        builder.addGridded(new JButton(refreshAction));
        builder.addRelatedGap();
        builder.addGridded(new JButton(newAction));
        builder.addRelatedGap();
        builder.addGridded(new JButton(openAction));
        builder.setBorder(Borders.createEmptyBorder("0,0,3dlu,0"));
        JScrollPane scrollPane = SwingHelper.borderlessScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setBorder(Borders.DLU4_BORDER);
        setLayout(new FormLayout("f:d:g", "p,f:d:g,p"));
        CellConstraints cc = new CellConstraints();
        add(builder.getPanel(), cc.xy(1, 1));
        add(scrollPane, cc.xy(1, 2));
        JComponent dashboard = getDashboard();
        if (dashboard != null) {
            add(dashboard, cc.xy(1, 3));
        }
    }

    @Override
    public void initState() {
        super.initState();
        table.setAutoCreateRowSorter(true);
        table.getRowSorter().addRowSorterListener(
                new RowSorterListener() {
                    public void sorterChanged(final RowSorterEvent event) {
                        table.scrollToSelection();
                        updateFilterStatusLabel();
                    }
                }
        );
        table.getModel().addTableModelListener(
                new TableModelListener() {
                    public void tableChanged(final TableModelEvent event) {
                        table.scrollToSelection();
                        updateFilterStatusLabel();
                    }
                }
        );
    }

    protected JComponent getDashboard() {
        return null;
    }

    protected void selectionChanged() {
        boolean hasSelection = table.getSelectedRow() != -1;
        deleteAction.setEnabled(hasSelection);
        openAction.setEnabled(hasSelection);
        addTagsAction.setEnabled(hasSelection);
        replaceTagsAction.setEnabled(hasSelection);
    }

    protected void updateFilterStatusLabel() {
        filterStatusLabel.setText(
                getString(
                        "filterStatusLabel.text",
                        table.getRowSorter().getViewRowCount(),
                        table.getRowSorter().getModelRowCount()
                )
        );
    }

    protected List<T> getSelected() {
        List<T> selected = new ArrayList<T>();
        for (int index : table.getSelectedRows()) {
            selected.add(getEntityList().get(table.getRowSorter().convertRowIndexToModel(index)));
        }
        return selected;
    }

    protected class RefreshAction extends DefaultAction {

        public RefreshAction() {
            super("refresh");
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            Task refreshTask = createRefreshTask();
            refreshTask.setInputBlocker(
                    new Task.InputBlocker(refreshTask, Task.BlockingScope.ACTION, this) {
                        protected void block() {
                            setEnabled(false);
                        }

                        protected void unblock() {
                            setEnabled(true);
                        }
                    }
            );
            getTaskService().execute(refreshTask);
        }
    }

    protected class NewAction extends DefaultAction {

        public NewAction() {
            super("new");
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            final T entity = createEntity();
            if (entity != null) {
                Entities.getInstance().add(entity);
                Application.getInstance(EmsClient.class).getRootPanel().edit(entity);
                DefaultUndoManager.getInstance(null).addEdit(new CreateEntityUndoableEdit(entity));
            }
        }
    }

    protected class OpenAction extends DefaultAction {

        public OpenAction() {
            super("open");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            for (T entity : getSelected()) {
                Application.getInstance(EmsClient.class).getRootPanel().edit(entity);
            }
        }
    }

    protected class DeleteAction extends DefaultAction {

        public DeleteAction() {
            super("delete");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            List<T> selected = getSelected();
            if (!selected.isEmpty()) {
                T first = selected.get(0);
                // todo: externalize text to resource bundle
                int answer = JOptionPane.showConfirmDialog(
                        getRootPane(),
                        String.format("Delete %s %s(s)?\nThis can not be undone", selected.size(), first.getClass().getSimpleName().toLowerCase()),
                        "Confirm delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE

                );
                if (answer == JOptionPane.YES_OPTION) {
                    getEntityList().removeAll(selected);
                    Entities.getInstance().registerForDeletion(selected);
                }
            }
        }
    }

    protected class SetTagsAction extends DefaultAction {

        private final boolean replace;

        public SetTagsAction(final String key, final boolean replace) {
            super(key);
            setEnabled(false);
            this.replace = replace;
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
                List<String> tags = new ListConverter.StringListConverter().convertReverse(input);
                for (int index : table.getSelectedRows()) {
                    T entity = getEntityList().get(table.getRowSorter().convertRowIndexToModel(index));
                    if (replace) {
                        entity.setTags(tags);
                    } else {
                        entity.addTags(tags);
                    }
                }
            }
        }
    }

    protected class ClearFilterAction extends AbstractAction {

        public ClearFilterAction() {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        }

        public void actionPerformed(final ActionEvent event) {
            filterTextField.setText("");
        }
    }
}
