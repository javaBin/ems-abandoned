package no.java.ems.client.swing.search;

import no.java.ems.client.RESTEmsService;
import no.java.ems.client.ResourceHandle;
import no.java.ems.client.swing.EmsClient;
import no.java.ems.domain.AbstractEntity;
import no.java.ems.domain.search.SearchResult;
import no.java.swing.InitSequence;
import no.java.swing.SwingHelper;
import org.jdesktop.application.Task;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchTable implements InitSequence {
    private JTable table;
    private ObservableList<SearchResult> model;
    private JTableBinding<SearchResult, List<SearchResult>, JTable> binding;
    private final EmsClient client;

    public SearchTable(EmsClient client) {
        this.client = client;   
        SwingHelper.initialize(this);
    }

    @Override
    public void initModels() {
        this.model = ObservableCollections.observableList(new ArrayList<SearchResult>());
    }

    @Override
    public void initActions() {
    }

    @Override
    public void initComponents() {
        table = new JTable();
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSurrendersFocusOnKeystroke(true);
        table.setFillsViewportHeight(true);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public void initBindings() {
        binding = SwingBindings.
                createJTableBinding(AutoBinding.UpdateStrategy.READ, model, table);
        binding.setEditable(false);
    }

    @Override
    public void initListeners() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRowCount() == 1) {
                    SearchResult result = model.get(table.getSelectedRow());
                    client.getContext().getTaskService().execute(new LoadSearchResultTask(client, result));
                }
            }
        });
    }

    @Override
    public void initLayout() {
        createColumns(binding);
    }

    @Override
    public void initState() {
        binding.bind();
    }

    @Override
    public JComponent getComponent() {
        return table;
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

    private static class LoadSearchResultTask extends Task<Void, AbstractEntity> {
        private final RESTEmsService service;
        private final EmsClient client;
        private final SearchResult result;

        public LoadSearchResultTask(EmsClient client, SearchResult result) {
            super(client);
            this.client = client;
            this.result = result;
            this.service = client.getClientService();
        }

        @Override
        protected Void doInBackground() throws Exception {
            AbstractEntity entity = null;
            switch (result.getType()) {
                case session:
                    entity = service.getSession(result.getHandle());
                    break;
                case event:
                    entity = service.getEvent(result.getHandle());
                    break;
                case person:
                    entity = service.getContact(result.getHandle());
                    break;
            }
            if (entity == null) {
                throw new IllegalArgumentException("Unknown type");
            }
            publish(entity);
            return null;
        }

        @Override
        protected void process(List<AbstractEntity> values) {
            for (AbstractEntity value : values) {
                client.getRootPanel().edit(value);
            }
        }
    }
}
