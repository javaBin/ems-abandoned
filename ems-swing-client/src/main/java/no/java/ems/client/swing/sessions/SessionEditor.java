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

package no.java.ems.client.swing.sessions;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.swing.Entities;
import no.java.ems.client.swing.EntityEditor;
import no.java.ems.client.swing.binding.LanguageConverter;
import no.java.ems.client.swing.binding.ListConverter;
import no.java.ems.domain.Event;
import no.java.ems.domain.Language;
import no.java.ems.domain.Room;
import no.java.ems.domain.Session;
import no.java.ems.domain.Speaker;
import no.java.swing.AutoCompleter;
import no.java.swing.SwingHelper;
import no.java.swing.SelectableLabel;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.SwingBindings;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import java.awt.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class SessionEditor extends EntityEditor<Session> {

    private ComboBoxModel roomsModel;
    private ComboBoxModel timeslotsModel;

    private SpeakersPanel speakersPanel;
    private JTextField keywordsField;
    private JTextArea leadTextArea;
    private JTextArea expectedAudienceTextArea;
    private JTextArea feedbackTextArea;
    private JTextArea outLineTextArea;
    private JTextArea equipmentTextArea;
    private JTextArea bodyTextArea;
    private JComboBox stateComboBox;
    private JComboBox formatComboBox;
    private JComboBox roomComboBox;
    private JComboBox timeslotComboBox;
    private JComboBox levelComboBox;
    private JCheckBox englishCheckBox;
    private JCheckBox publishedCheckBox;
    private SelectableLabel idField;

    private DateTimeFormatter dateFormatter = DateTimeFormat.shortDate();
    private DateTimeFormatter timeFormatter = DateTimeFormat.shortTime();

    public SessionEditor(final Session session) {
        super(session, "title");
        initialize();
    }

    public void initModels() {
        Event event = getEmsService().getEvent(entity.getEventHandle());
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (Room room : event.getRooms()) {
            model.addElement(room);
        }
        roomsModel = model;

        model = new DefaultComboBoxModel();
        for (Interval timeslot : event.getTimeslots()) {
            model.addElement(timeslot);
        }
        timeslotsModel = model;
    }

    public void initActions() {
    }

    @Override
    public void initComponents() {
        super.initComponents();
        speakersPanel = new SpeakersPanel(entity);
        keywordsField = new JTextField(50);
        leadTextArea = createMediumTextArea();
        bodyTextArea = createBigTextArea();
        expectedAudienceTextArea = createBigTextArea();
        feedbackTextArea = createMediumTextArea();
        outLineTextArea = createBigTextArea();
        equipmentTextArea = createMediumTextArea();
        stateComboBox = new JComboBox(Session.State.values());
        formatComboBox = new JComboBox(Session.Format.values());
        roomComboBox = new JComboBox(roomsModel);
        timeslotComboBox = new JComboBox(timeslotsModel);
        levelComboBox = new JComboBox(Session.Level.values());
        englishCheckBox = new JCheckBox("english");
        englishCheckBox.setName(getFullResourceKey("englishCheckBox"));
        publishedCheckBox = new JCheckBox("published");
        publishedCheckBox.setName(getFullResourceKey("publishedCheckBox"));
        idField = new SelectableLabel(false);
        SwingHelper.setTabFocusTraversalKeys(leadTextArea);
        SwingHelper.setTabFocusTraversalKeys(bodyTextArea);
        new AutoCompleter<String>(tagsField, Entities.getInstance().getTags());
        new AutoCompleter<String>(keywordsField, Entities.getInstance().getKeywords());
        setTransferHandler(attachementsPanel.getTransferHandler());

        roomComboBox.setRenderer(
                new DefaultListCellRenderer() {
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value == null) {
                            setText("");
                            return this;
                        }
                        Room room = (Room) value;
                        setText(room.getName());
                        return this;
                    }
                }
        );

        roomComboBox.setSelectedIndex(-1);
        if (getEntity().getRoom() != null) {
            String currentRoomName = getEntity().getRoom().getName();
            for (int i = 0; i < roomsModel.getSize(); i++) {
                Room room = (Room) roomsModel.getElementAt(i);

                if (room.getName().equals(currentRoomName)) {
                    roomComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        timeslotComboBox.setRenderer(
                new DefaultListCellRenderer() {
                    // TODO: set a fixed-width font
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value == null) {
                            setText("");
                            return this;
                        }

                        setText(intervalToString((Interval) value));

                        return this;
                    }
                }
        );

        timeslotComboBox.setSelectedIndex(-1);
        Interval currentTimeslot = getEntity().getTimeslot();
        if (currentTimeslot != null) {
            for (int i = 0; i < timeslotsModel.getSize(); i++) {
                Interval interval = (Interval) timeslotsModel.getElementAt(i);

                // TODO: this should compare the entire interval, not only the start.
                // The current behaviour is like this because end is currently equal to start which is a bug - trygve
                if (interval.getStart().equals(currentTimeslot.getStart())) {
                    timeslotComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private JTextArea createMediumTextArea() {
        JTextArea textArea = new JTextArea(4, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private JTextArea createBigTextArea() {
        JTextArea textArea = new JTextArea(8, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    @Override
    public void initBindings() {
        super.initBindings();
        AutoBinding<Session, String, SelectableLabel, String> sessionIdBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, entity, BeanProperty.<Session, String>create("displayID"), idField, BeanProperty.<SelectableLabel, String>create("text"));
        getBindingGroup().addBinding(sessionIdBinding);
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("lead"), leadTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("body"), bodyTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("notes"), notesTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("equipment"), equipmentTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("outline"), outLineTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("expectedAudience"), expectedAudienceTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("feedback"), feedbackTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, List<String>>create("keywords"), keywordsField, new ListConverter.StringListConverter()));
        getBindingGroup().addBinding(createComboBoxBinding("room", roomComboBox));
        getBindingGroup().addBinding(createComboBoxBinding("timeslot", timeslotComboBox));
        getBindingGroup().addBinding(createComboBoxBinding("level", levelComboBox));
        getBindingGroup().addBinding(createComboBoxBinding("format", formatComboBox));
        getBindingGroup().addBinding(createComboBoxBinding("state", stateComboBox));
        AutoBinding<Session, Language, AbstractButton, Boolean> englishBinding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                entity,
                BeanProperty.<Session, Language>create("language"),
                englishCheckBox,
                BeanProperty.<AbstractButton, Boolean>create("selected")
        );
        englishBinding.setSourceNullValue(Boolean.FALSE);
        englishBinding.setConverter(new LanguageConverter());
        getBindingGroup().addBinding(englishBinding);
        getBindingGroup().addBinding(
                SwingBindings.createJListBinding(
                        AutoBinding.UpdateStrategy.READ_WRITE,
                        entity,
                        BeanProperty.<Session, List<Speaker>>create("speakers"),
                        speakersPanel.getSpeakerList()
                )
        );
        getBindingGroup().addBinding(
                Bindings.createAutoBinding(
                        AutoBinding.UpdateStrategy.READ_WRITE,
                        entity,
                        BeanProperty.<Session, Boolean>create("published"),
                        publishedCheckBox,
                        BeanProperty.<JCheckBox, Boolean>create("selected")
                )
        );
    }

    public void initLayout() {
        List<Map.Entry<JLabel, JComponent>> content = new ArrayList<Map.Entry<JLabel, JComponent>>();
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("id", idField), idField));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("published", publishedCheckBox), publishedCheckBox));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("title", titleField), titleField));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("speakers", speakersPanel), speakersPanel));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("language", englishCheckBox), englishCheckBox));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("tags", tagsField), tagsField));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("keywords", keywordsField), keywordsField));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("state", stateComboBox), stateComboBox));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("format", formatComboBox), formatComboBox));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("room", roomComboBox), roomComboBox));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("timeslot", timeslotComboBox), timeslotComboBox));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("level", levelComboBox), levelComboBox));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("lead", leadTextArea), new JScrollPane(leadTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("body", bodyTextArea), new JScrollPane(bodyTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("notes", notesTextArea), new JScrollPane(notesTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("outline", outLineTextArea), new JScrollPane(outLineTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("expectedAudience", expectedAudienceTextArea), new JScrollPane(expectedAudienceTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("equipment", equipmentTextArea), new JScrollPane(equipmentTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("feedback", feedbackTextArea), new JScrollPane(feedbackTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("attachements", attachementsPanel), attachementsPanel));
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("r:p,3dlu,l:d"));
        for (Map.Entry<JLabel, JComponent> entry : content) {
            builder.appendRow("p");
            builder.add(entry.getKey());
            builder.nextColumn(2);
            builder.add(entry.getValue());
            builder.appendRow("3dlu");
            builder.nextLine(2);
        }
        setBorder(Borders.DLU4_BORDER);
        setLayout(new BorderLayout());
        add(SwingHelper.borderlessScrollPane(builder.getPanel()), BorderLayout.CENTER);
    }

    private String intervalToString(Interval timeslot) {
        return dateFormatter.print(timeslot.getStart()) + " " + timeFormatter.print(timeslot.getStart()) + " - " + timeFormatter.print(timeslot.getEnd());
    }

}
