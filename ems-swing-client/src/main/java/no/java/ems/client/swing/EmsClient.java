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

import no.java.ems.client.RESTEmsService;
import no.java.swing.SwingHelper;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

/**
 * <ul>
 * <li>todo: dashboard (track, approved, missing) find correspodance from this contact
 * <li>todo: undo/redo: editing a speaker must flag the session as modified
 * <li>todo: reopen tabs on launch
 * <li>todo: autocomplete in table cell editors
 * <li>todo: dashboard (summaries for state, format, foreign etc)
 * <li>todo: add support for custom tag columns
 * <li>todo: add support row highlighting (tag watch)
 * <li>todo: context menu on tab title
 * <li>todo: macify
 * <li>todo: better email handling in cell editor (do not commit when unparsable/stay in editor)
 * <li>todo: table improvements: dismiss editor on selection change, filter, sorting, tab change etc.
 * <li>todo: better tag and keyword handling (ensure delimiter may not be part of tag/keyword)
 * <li>todo: about dialog / info (kudos, license etc.)
 * <li>todo: drag attachements out of application
 * <li>todo: help / tutorial
 * <li>todo: add "really attach n files/google/email/xxx to n people/contacts/yyy" sanity checks
 * <li>todo: reorder tabs by dragging
 * <li>todo: double click column edge to resize (prefrerred column width)
 * <li>todo: initial table column widths
 * <li>todo: sessions are left in Entities when event is closed
 * <li>todo: remove save/refresh button: autosave and autorefresh (rss/etags?)
 * <li>todo: (per user) session voting
 * <li>todo: new session (inherit speakers)
 * <li>todo: new speaker that doews not have email: notify
 * </ul>
 *
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class EmsClient extends SingleFrameApplication {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private String connectionURI;
    private static final String EMS_DEFAULT_URI = "http://localhost:3000/ems";
    private EmsRootPanel rootPanel;
    private RESTEmsService emsClient;

    public static void main(final String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
//        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Event Management Suite");
        launch(EmsClient.class, args);
    }

    public static EmsClient getInstance() {
        return getInstance(EmsClient.class);
    }

    public static ResourceMap getResourceMap() {
        return getInstance().getContext().getResourceMap();
    }

    public static String getText(final String key, final Object... args) {
        return getResourceMap().getString(key, args);
    }

    public String getHost() {
        return connectionURI;
    }

    public EmsRootPanel getRootPanel() {
        return rootPanel;
    }

    public RESTEmsService getClientService() {
        return emsClient;
    }

    @Override
    protected void initialize(final String[] args) {
        // UI init
        new CustomUncaughtExceptionHandler(getMainFrame());
        UIManager.put("TextArea.font", UIManager.getFont("TextField.font"));

        // Login
        String configuredURI = System.getProperty("ems-host");
        connectionURI = configuredURI != null ? configuredURI : EMS_DEFAULT_URI;

        logger.info("Ems host address set to " + connectionURI);

        if (configuredURI == null) {
            emsClient = new RESTEmsService(connectionURI);            
        }
        else {
            EmsRootPanel.AuthenticationDialog authenticationDialog = EmsRootPanel.runAuthenticationDialog(null);

            if (authenticationDialog == null) {
                logger.info("User login cancelled.");
                System.exit(0); // TODO: Handle login cancel better
            }

            emsClient = new RESTEmsService(connectionURI, authenticationDialog.getUsername(), authenticationDialog.getPassword());
        }
    }

    protected void startup() {
        rootPanel = new EmsRootPanel(this);
    }

    public boolean isAuthenticated() {
        return rootPanel.getStatusBar().isAuthenticated();
    }

    private static class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler, Runnable {

        private final Component referenceComponent;

        public CustomUncaughtExceptionHandler(final Component referenceComponent) {
            this.referenceComponent = referenceComponent;
            run();
        }

        public void uncaughtException(final Thread thread, final Throwable throwable) {
            throwable.printStackTrace();
            SwingHelper.displayErrorMessage(throwable, referenceComponent);
            EventQueue.invokeLater(this);
        }

        public void run() {
            Thread.currentThread().setUncaughtExceptionHandler(CustomUncaughtExceptionHandler.this);
        }
    }
}
