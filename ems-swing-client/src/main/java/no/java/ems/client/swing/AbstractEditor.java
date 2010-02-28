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

import no.java.swing.DefaultPanel;
import no.java.ems.client.RESTEmsService;
import org.jdesktop.beansbinding.BindingGroup;

import javax.swing.*;
import java.net.URI;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public abstract class AbstractEditor extends DefaultPanel {

    private final BindingGroup bindingGroup = new BindingGroup();
    private URI id;
    private String title;
    private Icon icon;
    private boolean changed;

    protected AbstractEditor() {
    }

    protected AbstractEditor(final String key) {
        super(key);
    }

    public URI getId() {
        return id;
    }

    public void setId(final URI id) {
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

    public boolean isChanged() {
        return changed;
    }

    protected void setChanged(boolean changed) {
        firePropertyChange("changed", this.changed, this.changed = changed);
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

    protected RESTEmsService getEmsService() {
        return EmsClient.getInstance().getClientService();
    }
}
