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

import no.java.swing.ConfiguredAction;
import no.java.swing.InitSequence;
import no.java.swing.SwingHelper;
import org.jdesktop.beansbinding.*;

import javax.swing.*;
import java.awt.event.ActionEvent;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class LoginPanel extends JPanel implements InitSequence {

    public static final String LOGIN_KEY = LoginPanel.class.getName() + ".login";
    public static final String LOGOUT_KEY = LoginPanel.class.getName() + ".logout";

    private final EmsClient emsClient;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton okButton;
    private JButton cancelButton;
    private User user = new User();
    private BindingGroup bindings = new BindingGroup();

    public LoginPanel(EmsClient emsClient) {
        this.emsClient = emsClient;
        SwingHelper.initialize(this);
    }

    public void initModels() {
    }

    public void initActions() {
        getActionMap().put(LOGIN_KEY, new LoginAction());
        getActionMap().put(LOGOUT_KEY, new LogoutAction());
    }

    public void initComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        okButton = new JButton(getActionMap().get(LOGIN_KEY));
        cancelButton = new JButton(getActionMap().get(LOGOUT_KEY));
    }

    public void initBindings() {
        bindings.addBinding(Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                user,
                BeanProperty.<User, String>create("username"),
                usernameField,
                BeanProperty.<JTextField, String>create("text")
        ));
        bindings.addBinding(Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                user,
                BeanProperty.<User, char[]>create("password"),
                passwordField,
                BeanProperty.<JPasswordField, char[]>create("password")
        ));
    }

    public void initListeners() {
    }

    public void initLayout() {
        CellConstraints cc = new CellConstraints();
        setLayout(new FormLayout("p, 2dlu, p", "f:p, 4dlu, f:p, 4dlu, f:p"));
        JLabel usernameLabel = new JLabel();
        usernameLabel.setName(getClass().getName() + ".usernameLabel");
        add(usernameLabel, cc.xy(1, 1));
        add(usernameField, cc.xy(3, 1));
        JLabel passwordLabel = new JLabel();
        passwordLabel.setName(getClass().getName() + ".passwordLabel");
        add(passwordLabel, cc.xy(1, 3));
        add(passwordField, cc.xy(3, 3));
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addGridded(okButton);
        builder.addRelatedGap();
        builder.addGridded(cancelButton);
        add(builder.getPanel(), cc.xy(3, 5));
    }

    public void initState() {
        bindings.bind();
    }

    public JComponent getComponent() {
        return this;
    }

    private class LoginAction extends ConfiguredAction {

        private LoginAction() {
            super("login");
        }

        public void actionPerformed(ActionEvent e) {
            emsClient.getRootPanel().getStatusBar().setCurrentPrincipal(user.getUsername());
            //emsClient.getClientService().setCredentials(user.getUsername(), user.getPassword() == null ? null : new String(user.getPassword()));
        }
    }

    private class LogoutAction extends ConfiguredAction {
        private LogoutAction() {
            super("logout");
        }

        public void actionPerformed(ActionEvent e) {
            //emsClient.getClientService().setCredentials(null, null);
        }

        @Override
        public boolean isEnabled() {
            return !emsClient.isAuthenticated();
        }
    }

    public class User {
        private String username;
        private char[] password;

        public String getUsername() {
            return username;
        }

        public char[] getPassword() {
            if (password != null) {
                return password.clone();
            }
            return null;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(char[] password) {
            this.password = password;
        }
    }
}