package no.java.swing;

import org.apache.commons.lang.Validate;
import org.jdesktop.application.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * A JFileChooser decorator that remembers the previous location, dialog size and position between sessions.
 *
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class SessionFileChooser extends JFileChooser {

    private final String identifier;

    public SessionFileChooser(final String identifier) {
        Validate.notEmpty(identifier, "Identifier may not be null or empty");
        this.identifier = identifier;
    }

    @Override
    protected JDialog createDialog(final Component parent) throws HeadlessException {
        final JDialog dialog = super.createDialog(parent);
        dialog.setName(identifier);
        try {
            String path = (String)Application.getInstance().getContext().getLocalStorage().load(dialog.getName() + ".path");
            if (path != null) {
                setCurrentDirectory(new File(path));
            }
            Application.getInstance().getContext().getSessionStorage().restore(dialog, dialog.getName() + ".xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        dialog.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosed(final WindowEvent event) {
                        try {
                            Application.getInstance().getContext().getLocalStorage().save(getCurrentDirectory().getAbsolutePath(), dialog.getName() + ".path");
                            Application.getInstance().getContext().getSessionStorage().save(dialog, dialog.getName() + ".xml");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        return dialog;
    }

}
