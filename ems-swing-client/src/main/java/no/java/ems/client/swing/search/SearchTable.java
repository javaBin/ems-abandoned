package no.java.ems.client.swing.search;

import no.java.ems.client.swing.binding.EmailConverter;
import no.java.ems.client.swing.binding.LanguageConverter;
import no.java.ems.client.swing.binding.ListConverter;
import no.java.ems.domain.Person;
import no.java.ems.domain.search.SearchResult;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchTable {
    private JTable table;
    private final ObservableList<SearchResult> model;

    public SearchTable() {
        this.model = ObservableCollections.observableList(new ArrayList<SearchResult>());
        table = new JTable();
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSurrendersFocusOnKeystroke(true);
        table.setFillsViewportHeight(true);

        JTableBinding<SearchResult, List<SearchResult>, JTable> binding = SwingBindings.
                createJTableBinding(AutoBinding.UpdateStrategy.READ, model, table);
        binding.setEditable(false);
        createColumns(binding);
        binding.bind();
    }

    @SuppressWarnings({"unchecked"})
    protected void createColumns(final JTableBinding<SearchResult, List<SearchResult>, JTable> tableBinding) {
        tableBinding
                .addColumnBinding(BeanProperty.<SearchResult, String>create("title"))
                .setColumnName("Title")
                .setEditable(false)
                ;
    }

    public void setModel(List<SearchResult> dataModel) {
        this.model.clear();
        this.model.addAll(dataModel);
    }

    public JTable getTable() {
        return table;
    }
}
