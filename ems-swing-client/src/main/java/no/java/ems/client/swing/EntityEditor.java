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

import no.java.ems.client.swing.binding.ListConverter;
import no.java.ems.domain.AbstractEntity;
import no.java.swing.SwingHelper;
import org.apache.commons.lang.Validate;
import org.jdesktop.beansbinding.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public abstract class EntityEditor<T extends AbstractEntity> extends AbstractEditor {

    protected final T entity;
    protected final String titleProperty;
    protected JTextField titleField;
    protected JTextField tagsField;
    protected JTextArea notesTextArea;

    protected EntityEditor(final T entity, final String titleProperty) {
        Validate.notNull(entity, "Entity may not be null");
        Validate.notNull(titleProperty, "Title property may not be null");
        this.entity = entity;
        this.titleProperty = titleProperty;
        if (entity.getHandle() != null) {
            setId(entity.getHandle().getURI());
        }
        else {
            entity.addPropertyChangeListener("handle", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getNewValue() != null) {
                        setId(entity.getHandle().getURI());
                    }
                    entity.removePropertyChangeListener("handle", this);
                }
            });
        }
        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentShown(final ComponentEvent event) {
                        // this solves the focus problem when opening/selecting tabs... we should find a better way
                        titleField.requestFocusInWindow();
                    }
                }
        );
    }

    public void initComponents() {
        titleField = new JTextField(30);
        tagsField = new JTextField(50);
        notesTextArea = new JTextArea(4, 50);
        notesTextArea.setLineWrap(true);
        notesTextArea.setWrapStyleWord(true);
        SwingHelper.setTabFocusTraversalKeys(notesTextArea);
    }

    public void initBindings() {
        getBindingGroup().addBinding(
                Bindings.createAutoBinding(
                        AutoBinding.UpdateStrategy.READ,
                        titleField,
                        BeanProperty.<JTextField, String>create("text"),
                        this,
                        BeanProperty.<AbstractEditor, String>create("title")
                )
        );
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<T, String>create(titleProperty), titleField, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<T, List<String>>create("tags"), tagsField, new ListConverter.StringListConverter()));
    }

    public void initListeners() {
        getBindingGroup().addBindingListener(
                new AbstractBindingListener() {
                    @Override
                    public void syncFailed(final Binding binding, final Binding.SyncFailure failure) {
                        System.err.println(String.format("%s: %s", binding.getName(), failure));
                    }
                }
        );
    }

    protected <V> Binding<T, V, JComboBox, V> createComboBoxBinding(final String property, final JComboBox comboBox) {
        return Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                entity,
                BeanProperty.<T, V>create(property),
                comboBox,
                BeanProperty.<JComboBox, V>create("selectedItem")
        );
    }

    protected <V> Binding<T, V, JTextComponent, String> createTextComponentBinding(final BeanProperty<T, V> property, final JTextComponent textComponent, final Converter<V, String> converter) {
        AutoBinding<T, V, JTextComponent, String> binding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                entity,
                property,
                textComponent,
                BeanProperty.<JTextComponent, String>create("text_ON_ACTION_OR_FOCUS_LOST")
        );
        if (converter != null) {
            binding.setConverter(converter);
        }
        return binding;
    }

    public T getEntity() {
        return entity;
    }
}
