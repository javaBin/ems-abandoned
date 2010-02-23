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
        EmsClient.getInstance().getRootPanel().edit(entity);
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
