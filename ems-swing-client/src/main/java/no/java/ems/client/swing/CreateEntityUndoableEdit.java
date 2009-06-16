package no.java.ems.client.swing;

import no.java.ems.domain.AbstractEntity;
import org.apache.commons.lang.Validate;
import org.jdesktop.application.Application;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
class CreateEntityUndoableEdit extends AbstractUndoableEdit {

    private final AbstractEntity entity;

    public CreateEntityUndoableEdit(final AbstractEntity entity) {
        Validate.notNull(entity, "Entity may not be null");
        this.entity = entity;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        Entities.getInstance().remove(entity);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        Entities.getInstance().add(entity);
        Application.getInstance(EmsClient.class).edit(entity);
    }

    @Override
    public String getUndoPresentationName() {
        // todo: externalize
        return "Undo create new " + entity.getClass().getSimpleName();
    }

    @Override
    public String getRedoPresentationName() {
        // todo: externalize
        return "Redo create new " + entity.getClass().getSimpleName();
    }

}
