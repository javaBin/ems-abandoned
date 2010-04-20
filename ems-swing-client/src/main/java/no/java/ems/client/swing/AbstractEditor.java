package no.java.ems.client.swing;

import no.java.swing.DefaultPanel;
import no.java.ems.service.EmsService;
import org.jdesktop.beansbinding.BindingGroup;

import javax.swing.*;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public abstract class AbstractEditor extends DefaultPanel {

    private final BindingGroup bindingGroup = new BindingGroup();
    private String id;
    private String title;
    private Icon icon;

    protected AbstractEditor() {
    }

    protected AbstractEditor(final String key) {
        super(key);
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        firePropertyChange("id", this.id, this.id = id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        firePropertyChange("title", this.title, this.title = title);
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(final Icon icon) {
        firePropertyChange("icon", this.icon, this.icon = icon);
    }

    public BindingGroup getBindingGroup() {
        return bindingGroup;
    }

    public boolean isClosable() {
        return true;
    }

    public boolean canClose() {
        return true;
    }

    public void willClose() {
    }

    public void initState() {
        setTitle(getString("title"));
        setIcon(getResourceMap().getIcon(getFullResourceKey("icon")));
        getBindingGroup().bind();
    }

    protected EmsService getEmsService() {
        return EmsClient.getInstance().getEmsService();
    }
}
