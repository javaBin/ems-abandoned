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

package no.java.swing;

import org.apache.commons.lang.Validate;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskService;
import org.jdesktop.application.ApplicationContext;

import javax.swing.*;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public abstract class DefaultPanel extends JPanel implements InitSequence {

    private final String panelKey;

    public JPanel getComponent() {
        return this;
    }

    protected DefaultPanel() {
        this.panelKey = getClass().getName();
    }

    protected DefaultPanel(final String key) {
        Validate.notNull(key, "Base resource key may not be null");
        this.panelKey = key;
    }

    protected void initialize() {
        SwingHelper.initialize(this);
        getResourceMap().injectComponents(this);
    }

    protected JLabel createLabel(final String key, final JComponent component) {
        return createLabel(key, component, JLabel.LEADING);
    }

    protected JLabel createLabel(final String key, final JComponent component, final int alignment) {
        Validate.notNull(key, "Resource key may not be null");
        JLabel label = new JLabel(key, alignment);
        label.setName(getFullResourceKey(key));
        label.setLabelFor(component);
        return label;
    }

    protected String getFullResourceKey(final String key) {
        Validate.notNull(key, "Resource key may not be null");
        return panelKey + "." + key;
    }

    protected ApplicationContext getContext() {
        return Application.getInstance().getContext();
    }

    protected ResourceMap getResourceMap() {
        return getContext().getResourceMap();
    }

    protected TaskService getTaskService() {
        return getContext().getTaskService();
    }

    protected String getString(final String key, Object... params) {
        Validate.notNull(key, "Resource key may not be null");
        return getResourceMap().getString(getFullResourceKey(key), params);
    }

    protected abstract class DefaultAction extends ConfiguredAction {

        private final String actionKey;

        protected DefaultAction(final String key) {
            super(DefaultPanel.this.getFullResourceKey(key));
            getActionMap().put(key, this);
            actionKey = key;
        }

        protected String getFullResourceKey(final String key) {
            Validate.notNull(key, "Resource key may not be null");
            return panelKey + "." + actionKey + "." + key;
        }

        protected String getString(final String key, Object... params) {
            Validate.notNull(key, "Resource key may not be null");
            return getResourceMap().getString(getFullResourceKey(key), params);
        }
    }

    protected abstract class DefaultTask<T, V> extends ApplicationTask<T, V> {

        protected DefaultTask(final String key) {
            super(Application.getInstance(), DefaultPanel.this.getFullResourceKey(key));
        }

    }
}
