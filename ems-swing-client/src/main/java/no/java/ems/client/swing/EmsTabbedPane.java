package no.java.ems.client.swing;

import com.jgoodies.forms.factories.Borders;
import no.java.swing.SwingHelper;
import org.apache.commons.lang.SystemUtils;
import org.jdesktop.application.Application;
import org.jdesktop.application.SessionStorage;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
class EmsTabbedPane extends JTabbedPane {

    public EmsTabbedPane() {
        setName("root.tabs");
        setBorder(Borders.TABBED_DIALOG_BORDER);
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

}
