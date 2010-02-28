package no.java.ems.client.swing.search;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.domain.search.ObjectType;
import no.java.swing.DefaultPanel;
import no.java.swing.InitSequence;
import no.java.swing.SwingHelper;
import org.apache.commons.lang.SystemUtils;
import org.jdesktop.swingx.JXButton;

import javax.swing.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SearchField extends DefaultPanel {
    private JButton searchButton;
    private JTextField searchField;
    private ObjectType type = ObjectType.session;
    private final Action searchAction;

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
        ButtonBarBuilder builder = new ButtonBarBuilder(this);
        builder.addFixed(searchField);
        builder.addUnrelatedGap();
        builder.addGridded(searchButton);
    }

    @Override
    public void initState() {
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

    public String getSearchText() {
        return searchField.getText();
    }

    public ObjectType getType() {
        return type;
    }
}
