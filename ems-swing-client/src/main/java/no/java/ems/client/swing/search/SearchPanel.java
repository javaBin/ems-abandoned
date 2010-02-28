package no.java.ems.client.swing.search;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.RESTEmsService;
import no.java.ems.client.swing.AbstractEditor;
import no.java.ems.client.swing.EmsClient;
import no.java.ems.domain.search.ObjectType;
import no.java.ems.domain.search.SearchResult;
import no.java.swing.InitSequence;
import no.java.swing.SwingHelper;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.SwingBindings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchPanel extends AbstractEditor implements InitSequence {
    private SearchTable searchTable;
    private SearchField searchField;

    public SearchPanel() {
        setTitle(getString("title"));
        setIcon(getResourceMap().getIcon(getFullResourceKey("icon")));
        SwingHelper.initialize(this);
    }

    @Override
    public void initModels() {
    }

    @Override
    public void initActions() {
    }

    @Override
    public void initComponents() {
        searchTable = new SearchTable();
        searchField = new SearchField(new DefaultAction("search") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("AAAAAAAAAAAAAAAAA");
                SearchTask task = new SearchTask(
                        EmsClient.getInstance().getClientService(),
                        searchField.getSearchText(),
                        searchField.getType());
                task.addTaskListener(new TaskListener.Adapter<List<SearchResult>, Void>() {
                    @Override
                    public void succeeded(TaskEvent<List<SearchResult>> event) {
                        List<SearchResult> value = event.getValue();
                        searchTable.setModel(value == null ? Collections.<SearchResult>emptyList() : value);
                    }
                });
                getTaskService().execute(task);
            }
        });

    }

    @Override
    public void initBindings() {
    }

    @Override
    public void initListeners() {
    }

    @Override
    public void initLayout() {
        setLayout(new FormLayout("f:d:g", "p,f:d:g,p"));
        CellConstraints cc = new CellConstraints();
        setBorder(Borders.DLU4_BORDER);        
        add(searchField.getComponent(), cc.xy(1, 1));
        
        JScrollPane scrollPane = new JScrollPane(searchTable.getTable());
        scrollPane.setOpaque(false);
        add(scrollPane, cc.xy(1, 2));
    }

    @Override
    public boolean isClosable() {
        return false;
    }

    private static class SearchTask extends Task<java.util.List<SearchResult>, Void> {
        private final String query;
        private final ObjectType type;
        private RESTEmsService clientService;

        public SearchTask(final RESTEmsService clientService, String query, ObjectType type) {
            super(EmsClient.getInstance());
            this.query = query;
            this.type = type;
            this.clientService = clientService;
        }

        @Override
        protected java.util.List<SearchResult> doInBackground() throws Exception {
            return clientService.search(query, type);
        }
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame jFrame = new JFrame("foo");
                JComponent contentPane = new SearchPanel().getComponent();
                jFrame.setContentPane(contentPane);
                jFrame.setLocationRelativeTo(null);
                jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jFrame.setSize(contentPane.getPreferredSize());
                jFrame.setVisible(true);
            }
        });
    }
}
