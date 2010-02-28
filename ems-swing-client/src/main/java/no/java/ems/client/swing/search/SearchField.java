package no.java.ems.client.swing.search;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.swing.InitSequence;
import no.java.swing.SwingHelper;
import org.apache.commons.lang.SystemUtils;
import org.jdesktop.swingx.JXButton;

import javax.swing.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchField implements InitSequence {
    private JButton searchButton;
    private JTextField searchField;
    private final Action searchAction;
    private JPanel root;

    SearchField(Action searchAction) {
        this.searchAction = searchAction;
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
        root = new JPanel();
        searchButton = new JXButton(searchAction);
        decorateButton(searchButton);
        searchField = new JTextField(40);
        searchField.setAction(searchAction);
    }

    @Override
    public void initBindings() {
    }

    @Override
    public void initListeners() {
    }

    @Override
    public void initLayout() {
        ButtonBarBuilder builder = new ButtonBarBuilder(root);
        builder.addFixed(searchField);
        builder.addUnrelatedGap();
        builder.addGridded(searchButton);
        //builder.setBorder(Borders.DLU2_BORDER);
        /*CellConstraints cc = new CellConstraints();
        root.setLayout(new FormLayout("p, p", "f:p"));
        root.add(searchField, cc.xy(1, 1));
        root.add(searchButton, cc.xy(2, 1));
        root.setBorder(Borders.DLU2_BORDER);*/
    }

    @Override
    public void initState() {
    }

    @Override
    public JComponent getComponent() {
        return root;
    }

    private JComponent decorateButton(JButton searchButton) {
        if (SystemUtils.IS_OS_WINDOWS) {
            JToolBar toolbar = new JToolBar();
            toolbar.add(searchButton);
            return toolbar;
        }
        searchButton.setRequestFocusEnabled(false);
        return searchButton;
    }

}
