package no.java.swing;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.beansbinding.BeanProperty;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.beans.PropertyChangeEvent;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class BeanPropertyUndoableEdit extends AbstractUndoableEdit {

    private final PropertyChangeEvent event;

    public BeanPropertyUndoableEdit(final PropertyChangeEvent event) {
        Validate.notNull(event, "Event may not be null");
        this.event = event;
    }

    @Override
    public String getPresentationName() {
        // todo: externalize
        return String.format("set \"%s\"", event.getPropertyName());
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        BeanProperty.create(event.getPropertyName()).setValue(event.getSource(), event.getOldValue());
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        BeanProperty.create(event.getPropertyName()).setValue(event.getSource(), event.getNewValue());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("source", event.getSource().getClass().getName())
                .append("property", event.getPropertyName())
//                .append("undoValue", event.getOldValue())
//                .append("redoValue", event.getNewValue())
                .toString();
    }

}
