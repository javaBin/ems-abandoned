package no.java.swing;

import org.apache.commons.lang.Validate;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class CycleSelectionAction extends AbstractAction {

    private static final Action UP = new CycleSelectionAction(true);
    private static final Action DOWN = new CycleSelectionAction(false);

    private final boolean up;

    public static void install(final JTable table) {
        Validate.notNull(table, "Table may not be null");
        table.getActionMap().put("selectPreviousRow", CycleSelectionAction.UP);
        table.getActionMap().put("selectNextRow", CycleSelectionAction.DOWN);
    }

    public static void install(final JList list) {
        Validate.notNull(list, "List may not be null");
        list.getActionMap().put("selectPreviousRow", CycleSelectionAction.UP);
        list.getActionMap().put("selectNextRow", CycleSelectionAction.DOWN);
    }

    private CycleSelectionAction(final boolean up) {
        this.up = up;
    }

    public void actionPerformed(final ActionEvent event) {
        ListSelectionModel selectionModel = null;
        int itemCount = 0;
        if (event.getSource() instanceof JTable) {
            JTable table = (JTable)event.getSource();
            selectionModel = table.getSelectionModel();
            itemCount = table.getRowSorter().getViewRowCount();
        }
        if (event.getSource() instanceof JList) {
            JList list = (JList)event.getSource();
            selectionModel = list.getSelectionModel();
            itemCount = list.getModel().getSize();
        }
        if (selectionModel != null && itemCount > 0) {
            int select = selectionModel.getLeadSelectionIndex();
            if (up) {
                if (--select < 0) {
                    select = itemCount - 1;
                }
            } else {
                if (++select >= itemCount) {
                    select = 0;
                }
            }
            selectionModel.setSelectionInterval(select, select);
            if (event.getSource() instanceof JTable) {
                JTable table = (JTable)event.getSource();
                table.scrollRectToVisible(table.getCellRect(select, 0, true));
            }
            if (event.getSource() instanceof JList) {
                JList list = (JList)event.getSource();
                list.scrollRectToVisible(list.getCellBounds(select, select));
            }
        }

    }

}
