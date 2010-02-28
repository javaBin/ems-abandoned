package no.java.ems.client.swing.search;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.swing.AbstractEditor;
import no.java.swing.InitSequence;
import no.java.swing.SwingHelper;
import org.jdesktop.swingx.JXFindBar;
import org.jdesktop.swingx.JXSearchPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchPanel extends AbstractEditor implements InitSequence {
    private SearchTable searchTable;
    private SearchField searchField;

    public SearchPanel() {
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
        searchTable = new SearchTable(new DefaultTableModel());
        searchField = new SearchField(new AbstractAction("search") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("AAAAAAAAAAAAAAAAA");
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
        
        JScrollPane scrollPane = new JScrollPane(searchTable);
        scrollPane.setOpaque(false);
        add(scrollPane, cc.xy(1, 2));
    }

    @Override
    public void initState() {
    }

    @Override
    public boolean isClosable() {
        return false;
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
