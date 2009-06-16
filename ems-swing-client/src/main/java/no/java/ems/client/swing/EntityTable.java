package no.java.ems.client.swing;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.Sizes;
import no.java.ems.domain.Session;
import no.java.swing.HighlightingCellRenderer;
import org.apache.commons.lang.SystemUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class EntityTable extends JTable {

    private final Color alternatingRowColor = new Color(0xf0f0f0);

    public EntityTable() {
        TableCellRenderer cellRenderer;
        if (SystemUtils.IS_OS_UNIX && !SystemUtils.IS_OS_MAC) {
            cellRenderer = new DefaultTableCellRenderer();
        } else {
            cellRenderer = new HighlightingCellRenderer(new Color(0xfffff99));
            ((HighlightingCellRenderer)cellRenderer).setBorder(
                    Borders.createEmptyBorder(
                            Sizes.dluY(3),
                            Sizes.dluX(1),
                            Sizes.dluY(3),
                            Sizes.dluX(1)
                    )
            );
            setRowHeight(((HighlightingCellRenderer)cellRenderer).getPreferredRowHeight(this));
        }
        setDefaultRenderer(String.class, cellRenderer);
        setDefaultRenderer(Session.State.class, cellRenderer);
        setDefaultRenderer(Session.Format.class, cellRenderer);
        setDefaultRenderer(Object.class, cellRenderer);
        setDefaultEditor(Session.Level.class, new DefaultCellEditor(new JComboBox(Session.Level.values())));
        setDefaultEditor(Session.State.class, new DefaultCellEditor(new JComboBox(Session.State.values())));
        setDefaultEditor(Session.Format.class, new DefaultCellEditor(new JComboBox(Session.Format.values())));
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSurrendersFocusOnKeystroke(true);
        setFillsViewportHeight(true);
        addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(final MouseEvent event) {
                        int rowIndex = rowAtPoint(event.getPoint());
                        if (SwingUtilities.isRightMouseButton(event) && rowIndex != -1 && !getSelectionModel().isSelectedIndex(rowIndex)) {
                            getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
                        }
                    }
                }
        );
        getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(final ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            scrollToSelection();
                        }
                    }
                }
        );
        getActionMap().put("cut", new DisabledAction());
        getActionMap().put("copy", new DisabledAction());
        getActionMap().put("paste", new DisabledAction());
    }

    public void scrollToSelection() {
        int min = getSelectionModel().getMinSelectionIndex();
        int max = getSelectionModel().getMaxSelectionIndex();
        if (min != -1 && max != -1) {
            int columnIndex = getColumnModel().getSelectionModel().getLeadSelectionIndex();
            columnIndex = Math.max(0, Math.min(columnIndex, getColumnCount() - 1));
            Rectangle visible = getCellRect(min, columnIndex, true);
            visible.add(getCellRect(max, columnIndex, true));
            scrollRectToVisible(visible);
            scrollRectToVisible(getCellRect(getSelectionModel().getLeadSelectionIndex(), columnIndex, true));
        }
    }

    public void setFilter(final String filter) {
        if (filter == null || filter.isEmpty()) {
            putClientProperty(HighlightingCellRenderer.HIGHLIGHT_PATTERN_PROPERTY, null);
            ((TableRowSorter<TableModel>)getRowSorter()).setRowFilter(null);
        } else {
            if (filter.contains(",")) {
                String[] filters = filter.split(",");
                putClientProperty(HighlightingCellRenderer.HIGHLIGHT_PATTERN_PROPERTY, null);
                List<RowFilter<TableModel, Integer>> rowFilters = new ArrayList<RowFilter<TableModel, Integer>>();
                for (String s : filters) {
                    if (!s.isEmpty()) {
                        String regexp = "(?i)" + Pattern.quote(s);
                        rowFilters.add(RowFilter.<TableModel, Integer>regexFilter(regexp));
                    }
                }
                ((TableRowSorter<TableModel>)getRowSorter()).setRowFilter(RowFilter.<TableModel, Integer>orFilter(rowFilters));
            }
            else {
                String regexp = "(?i)" + Pattern.quote(filter);
                putClientProperty(HighlightingCellRenderer.HIGHLIGHT_PATTERN_PROPERTY, filter.length() > 0 ? Pattern.compile(regexp) : null);
                ((TableRowSorter<TableModel>)getRowSorter()).setRowFilter(RowFilter.regexFilter(regexp));                
            }
        }
    }

    @Override
    public boolean editCellAt(final int rowIndex, final int columnIndex, final EventObject event) {
        // prevent modifier key strokes from starting cell editing
        if (event instanceof KeyEvent) {
            KeyEvent keyEvent = (KeyEvent)event;
            if (keyEvent.isAltDown() || keyEvent.isControlDown() || keyEvent.isMetaDown() || keyEvent.isActionKey()) {
                return false;
            }
        }
        return super.editCellAt(rowIndex, columnIndex, event);
    }

    @Override
    public Component prepareRenderer(final TableCellRenderer renderer, final int rowIndex, final int columnIndex) {
        Component renderingComponent = super.prepareRenderer(renderer, rowIndex, columnIndex);
        if (!isCellSelected(rowIndex, columnIndex)) {
            renderingComponent.setBackground(rowIndex % 2 == 0 ? getBackground() : alternatingRowColor);
        }
        return renderingComponent;
    }

    @Override
    public void processKeyEvent(final KeyEvent event) {
        super.processKeyEvent(event);
    }

    private static class DisabledAction extends AbstractAction {

        private DisabledAction() {
            setEnabled(false);
        }

        public void actionPerformed(final ActionEvent event) {
        }

    }

}
