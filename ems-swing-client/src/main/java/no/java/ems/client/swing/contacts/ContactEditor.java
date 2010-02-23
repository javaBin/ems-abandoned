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

package no.java.ems.client.swing.contacts;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.swing.AttachmentsPanel;
import no.java.ems.client.swing.Entities;
import no.java.ems.client.swing.EntityEditor;
import no.java.ems.client.swing.PhotoPanel;
import no.java.ems.client.swing.binding.EmailConverter;
import no.java.ems.client.swing.binding.LanguageConverter;
import no.java.ems.domain.Binary;
import no.java.ems.domain.EmailAddress;
import no.java.ems.domain.Language;
import no.java.ems.domain.Person;
import no.java.swing.AutoCompleter;
import no.java.swing.SwingHelper;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import javax.swing.*;
import java.awt.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class ContactEditor extends EntityEditor<Person> {

    private JTextField emailField;
    private JTextArea descriptionTextArea;
    private JComboBox genderComboBox;
    private JCheckBox englishCheckBox;
    private PhotoPanel photoPanel;
    private AttachmentsPanel attachmentsPanel;

    public ContactEditor(final Person person) {
        super(person, "name");
        initialize();
    }

    public void initModels() {
    }

    public void initActions() {
    }

    @Override
    public void initComponents() {
        super.initComponents();
        attachmentsPanel = new AttachmentsPanel(entity);
        attachmentsPanel.setName(entity.getClass().getSimpleName().toLowerCase() + ".attachments");  
        photoPanel = new PhotoPanel(new Dimension(100, 125));
        photoPanel.setBorder(BorderFactory.createEtchedBorder());
        photoPanel.setOpaque(false);
        emailField = new JTextField(50);
        descriptionTextArea = new JTextArea(5, 50);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        genderComboBox = new JComboBox(Person.Gender.values());
        englishCheckBox = new JCheckBox("english");
        englishCheckBox.setName(getFullResourceKey("englishCheckBox"));
        SwingHelper.setTabFocusTraversalKeys(descriptionTextArea);
        SwingHelper.setTabFocusTraversalKeys(notesTextArea);
        new AutoCompleter<String>(tagsField, Entities.getInstance().getTags());
        setTransferHandler(attachmentsPanel.getTransferHandler());
    }

    @Override
    public void initBindings() {
        super.initBindings();
        AutoBinding<Person, Language, AbstractButton, Boolean> foreignBinding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                entity,
                BeanProperty.<Person, Language>create("language"),
                englishCheckBox,
                BeanProperty.<AbstractButton, Boolean>create("selected")
        );
        foreignBinding.setSourceNullValue(Boolean.FALSE);
        foreignBinding.setConverter(new LanguageConverter());
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Person, String>create("description"), descriptionTextArea, null));
        //getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Person, String>create("notes"), notesTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Person, List<EmailAddress>>create("emailAddresses"), emailField, new EmailConverter()));
        getBindingGroup().addBinding(createComboBoxBinding("gender", genderComboBox));
        getBindingGroup().addBinding(foreignBinding);
        getBindingGroup().addBinding(
                Bindings.createAutoBinding(
                        AutoBinding.UpdateStrategy.READ_WRITE,
                        entity,
                        BeanProperty.<Person, Binary>create("photo"),
                        photoPanel,
                        BeanProperty.<PhotoPanel, Binary>create("binary")
                )
        );
    }

    public void initLayout() {
        List<Map.Entry<JLabel, JComponent>> content = new ArrayList<Map.Entry<JLabel, JComponent>>();
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("name", titleField), titleField));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("email", emailField), emailField));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("tags", tagsField), tagsField));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("description", descriptionTextArea), new JScrollPane(descriptionTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("gender", genderComboBox), genderComboBox));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("language", englishCheckBox), englishCheckBox));
        //content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("notes", notesTextArea), new JScrollPane(notesTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("attachements", attachmentsPanel), attachmentsPanel));
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("r:p,3dlu,l:d"));
        for (Map.Entry<JLabel, JComponent> entry : content) {
            builder.appendRow("p");
            builder.add(entry.getKey());
            builder.nextColumn(2);
            builder.add(entry.getValue());
            builder.appendRow("3dlu");
            builder.nextLine(2);
        }
        CellConstraints cc = new CellConstraints();
        JPanel fields = new JPanel();
        fields.setLayout(new FormLayout("p,7dlu,l:d:g", "p,d:g"));
        fields.add(photoPanel, cc.xy(1, 1));
        fields.add(builder.getPanel(), cc.xywh(3, 1, 1, 2));
        setBorder(Borders.DLU4_BORDER);
        setLayout(new BorderLayout());
        add(SwingHelper.borderlessScrollPane(fields), BorderLayout.CENTER);
    }

}
