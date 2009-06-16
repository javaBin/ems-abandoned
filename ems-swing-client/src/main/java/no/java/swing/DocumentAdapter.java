package no.java.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public abstract class DocumentAdapter implements DocumentListener {

    public void insertUpdate(final DocumentEvent event) {
        documentChanged(event);
    }

    public void removeUpdate(final DocumentEvent event) {
        documentChanged(event);
    }

    public void changedUpdate(final DocumentEvent event) {
        documentChanged(event);
    }

    abstract protected void documentChanged(final DocumentEvent event);

}
