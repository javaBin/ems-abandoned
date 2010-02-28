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

import com.jgoodies.forms.factories.Borders;
import no.java.swing.SwingHelper;
import org.apache.commons.lang.SystemUtils;
import org.jdesktop.application.Application;
import org.jdesktop.application.SessionStorage;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
class EmsTabbedPane extends JTabbedPane {

    public EmsTabbedPane() {
        setName("root.tabs");
        setBorder(Borders.TABBED_DIALOG_BORDER);
    }

    public void addTab(AbstractEditor editor) {
        addTab(editor, getTabCount());
    }
    
    public void addTab(AbstractEditor editor, int index) {
        insertTab(editor.getTitle(), editor.getIcon(), editor, null, index);
        setTabComponentAt(index, new TabComponent(editor, this));
    }

    public AbstractEditor selectEditor(URI uri) {
        for (int index = 0; index < getTabCount(); index++) {
            Component component = getTabComponentAt(index);
            if (component instanceof TabComponent) {
                TabComponent tabComponent = (TabComponent)component;
                URI id = tabComponent.getTab().getId();
                if (id != null && id.equals(uri)) {
                    setSelectedIndex(index);
                    return tabComponent.getTab();
                }
            }
        }
        return null;
    }

    public void addSelectedTab(AbstractEditor editor) {
        int index = getTabCount();
        addTab(editor, index);
        setSelectedIndex(index);
    }

    public List<AbstractEditor> getEditorsWithChanges() {
        List<AbstractEditor> editors = new ArrayList<AbstractEditor>();
        int tabs = getTabCount();
        for (int i = 0; i < tabs; i++) {
            TabComponent componentAt = getTabComponentAt(i);
            if (componentAt != null) {
                AbstractEditor editor = componentAt.getTab();
                if (editor.isChanged()) {
                    editors.add(editor);
                }
            }
        }
        return editors;
    }

    @Override
    public void insertTab(final String title, final Icon icon, final Component component, final String tip, final int index) {
        if (SystemUtils.IS_OS_MAC_OSX && component instanceof Container) {
            SwingHelper.visitChildren(
                    (Container)component,
                    new SwingHelper.ComponentVisitor() {
                        public void visit(final Component component) {
                            if (component instanceof JPanel && !(component instanceof PhotoPanel)) {
                                ((JPanel)component).setOpaque(false);
                            }
                            if (component instanceof JScrollPane) {
                                JScrollPane scrollPane = (JScrollPane)component;
                                scrollPane.setOpaque(false);
                                scrollPane.getViewport().setOpaque(false);
                            }
                        }
                    }, true
            );

        }
        super.insertTab(title, icon, component, tip, index);
    }

    @Override
    public TabComponent getTabComponentAt(int index) {
        return (TabComponent) super.getTabComponentAt(index);
    }

    @Override
    public void setTabComponentAt(int index, Component component) {
        if (component instanceof TabComponent || component == null) {
            super.setTabComponentAt(index, component);
            return;
        }
        throw new IllegalArgumentException("Component must be a TabComponent");

    }

/*
    static {
        Application.getInstance().getContext().getSessionStorage().putProperty(
                EmsTabbedPane.class,
                new SessionStorage.Property() {
                    public Object getSessionState(final Component component) {
                        if (component instanceof EmsTabbedPane) {
                            EmsTabbedPane tabbedPane = (EmsTabbedPane)component;
                            for (int index = 0; index < tabbedPane.getTabCount(); index++) {
                                Component componentAt = tabbedPane.getComponentAt(index);
                                if (componentAt instanceof EntityEditor) {
                                    EntityEditor entityEditor = (EntityEditor)componentAt;
                                }
                            }
                        }
                        return null;
                    }

                    public void setSessionState(final Component component, final Object state) {
                        if (component instanceof EmsTabbedPane) {
//                            EmsTabbedPane tabbedPane = (EmsTabbedPane)component;
                            System.err.println("Restoring tabs");
                        }
                    }
                }
        );
    }
*/
}
