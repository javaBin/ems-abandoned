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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jdesktop.application.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class TabComponent extends JComponent implements PropertyChangeListener {

    private final JTabbedPane tabbedPane;
    private final AbstractEditor abstractEditor;
    private final JLabel titleLabel;

    public TabComponent(final AbstractEditor abstractEditor, final JTabbedPane tabbedPane) {
        Validate.notNull(abstractEditor, "AbstractEditor may not be null");
        Validate.notNull(tabbedPane, "Tabbed pane may not be null");
        this.tabbedPane = tabbedPane;
        this.abstractEditor = abstractEditor;
        JButton closeButton = new JButton();
        closeButton.setName("tabs.closeButton");
        closeButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        closeTab(tabbedPane, abstractEditor);
                    }
                }
        );
        closeButton.setFocusable(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        titleLabel = new JLabel(abstractEditor.getTitle(), abstractEditor.getIcon(), JLabel.LEFT) {
            @Override
            public void setText(final String text) {
                super.setText(text == null || text.isEmpty() ? "<Untitled>" : StringUtils.abbreviate(text, 20));
                setToolTipText(text == null || text.isEmpty() ? null : text);
            }
        };
        titleLabel.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(final MouseEvent event) {
                        if (event.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {
                            tabbedPane.setSelectedComponent(abstractEditor);
                        }
                    }

                    @Override
                    public void mouseClicked(final MouseEvent event) {
                        if (SwingUtilities.isLeftMouseButton(event) && event.isShiftDown() && abstractEditor.isClosable()) {
                            closeTab(tabbedPane, abstractEditor);
                        }
                    }
                }
        );
        setLayout(new BorderLayout(4, 0));
        add(titleLabel, BorderLayout.CENTER);
        add(closeButton, BorderLayout.EAST);
        closeButton.setVisible(abstractEditor.isClosable());
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        Application.getInstance().getContext().getResourceMap().injectComponents(this);
        abstractEditor.addPropertyChangeListener("title", this);
        abstractEditor.addPropertyChangeListener("icon", this);
    }

    public AbstractEditor getTab() {
        return abstractEditor;
    }

    public void propertyChange(final PropertyChangeEvent event) {
        int index = tabbedPane.indexOfTabComponent(this);
        if (index != -1) {
            if ("title".equals(event.getPropertyName())) {
                String text = (String)event.getNewValue();
                tabbedPane.setTitleAt(index, text == null || text.isEmpty() ? "<Untitled>" : StringUtils.abbreviate(text, 20));
                titleLabel.setText(text);
            }
            if ("icon".equals(event.getPropertyName())) {
                tabbedPane.setIconAt(index, (Icon)event.getNewValue());
            }
        }
    }

    private void closeTab(final JTabbedPane tabbedPane, final AbstractEditor abstractEditor) {
        tabbedPane.remove(abstractEditor);
        tabbedPane.transferFocus();
        abstractEditor.removePropertyChangeListener("title", this);
        abstractEditor.removePropertyChangeListener("icon", this);
    }

}
