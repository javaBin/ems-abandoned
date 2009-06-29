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
import no.java.swing.DefaultPanel;
import no.java.swing.TaskServiceMonitor;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class StatusBar extends DefaultPanel {

    private TaskServiceMonitor taskServiceMonitor;

    public StatusBar() {
        initialize();
    }

    public void initModels() {
    }

    public void initActions() {
    }

    public void initComponents() {
        taskServiceMonitor = new TaskServiceMonitor(getTaskService());
    }

    public void initBindings() {
    }

    public void initListeners() {
    }

    public void initLayout() {
        setBorder(
                new CompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                        Borders.createEmptyBorder("3dlu,2dlu,3dlu,3dlu")
                )
        );
        setLayout(new BorderLayout());
        add(taskServiceMonitor, BorderLayout.CENTER);
    }

    public void initState() {
    }

    public void setMessage(final String message) {
        taskServiceMonitor.setMessage(message);
    }

    public void setErrorMessage(String message) {
        taskServiceMonitor.setErrorMessage(message);
    }

    public void setCurrentPrincipal(String message) {
        taskServiceMonitor.setCurrentPrincipal(message);
    }
}
