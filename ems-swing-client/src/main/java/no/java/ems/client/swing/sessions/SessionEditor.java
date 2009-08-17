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
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.SwingBindings;
import org.jdesktop.swingx.JXHyperlink;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.URI;
import java.io.IOException;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class SessionEditor extends EntityEditor<Session> {

    private ComboBoxModel roomsModel;
    private ComboBoxModel timeslotsModel;

    private SpeakersPanel speakersPanel;
    private JTextField keywordsField;
    private JTextArea leadTextArea;
    private JTextArea bodyTextArea;
    private JTextArea expectedAudienceTextArea;
    private JTextArea feedbackTextArea;
    private JTextArea outlineTextArea;
    private JComboBox stateComboBox;
    private JComboBox formatComboBox;
    private JComboBox roomComboBox;
    private JComboBox timeslotComboBox;
    private JComboBox levelComboBox;
    private JCheckBox englishCheckBox;
    private JCheckBox publishedCheckBox;
    private JXHyperlink idField;

    private DateTimeFormatter dateFormatter = DateTimeFormat.shortDate();
    private DateTimeFormatter timeFormatter = DateTimeFormat.shortTime();
    private URI submitItURI;

    public SessionEditor(final Session session) {
        super(session, "title");        
        initialize();
    }

    public void initModels() {
        Event event = getEmsService().getEvent(entity.getEventId());
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
        outlineTextArea = createBigTextArea();
        feedbackTextArea = createMediumTextArea();
        stateComboBox = new JComboBox(Session.State.values());
        formatComboBox = new JComboBox(Session.Format.values());
        roomComboBox = new JComboBox(roomsModel);
        timeslotComboBox = new JComboBox(timeslotsModel);
        levelComboBox = new JComboBox(Session.Level.values());
        englishCheckBox = new JCheckBox("english");
        englishCheckBox.setName(getFullResourceKey("englishCheckBox"));
        publishedCheckBox = new JCheckBox("published");
        publishedCheckBox.setName(getFullResourceKey("publishedCheckBox"));
        idField = new JXHyperlink(new DefaultAction("session.view") {
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(submitItURI);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        idField.setRequestFocusEnabled(false);
        JPopupMenu idFieldMenu = new JPopupMenu();
        idFieldMenu.add(new CopyURIAction());
        idField.setComponentPopupMenu(idFieldMenu);
        SwingHelper.setTabFocusTraversalKeys(leadTextArea);
        SwingHelper.setTabFocusTraversalKeys(bodyTextArea);
        SwingHelper.setTabFocusTraversalKeys(feedbackTextArea);
        SwingHelper.setTabFocusTraversalKeys(outlineTextArea);
        SwingHelper.setTabFocusTraversalKeys(expectedAudienceTextArea);
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
        addPropertyChangeListener("id", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                submitItURI = URI.create(System.getProperty("session.browse.uri") + evt.getNewValue());
            }
        });
        //getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("id"), idField, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("lead"), leadTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("body"), bodyTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("expectedAudience"), expectedAudienceTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("outline"), outlineTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("feedback"), feedbackTextArea, null));
        getBindingGroup().addBinding(createTextComponentBinding(BeanProperty.<Session, String>create("notes"), notesTextArea, null));
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
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("expectedAudience", expectedAudienceTextArea), new JScrollPane(expectedAudienceTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("outline", outlineTextArea), new JScrollPane(outlineTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("feedback", feedbackTextArea), new JScrollPane(feedbackTextArea)));
        content.add(new AbstractMap.SimpleEntry<JLabel, JComponent>(createLabel("notes", notesTextArea), new JScrollPane(notesTextArea)));
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
        return dateFormatter.print(timeslot.getStart()) + " " +
                timeFormatter.print(timeslot.getStart()) + " -> " +
                timeFormatter.print(timeslot.getEnd()) + " " +
                "(" + (timeslot.getEnd().getMinuteOfDay() - timeslot.getStart().getMinuteOfDay()) + " minutes)";
    }

    private class CopyURIAction extends DefaultAction {
        protected CopyURIAction() {
            super("session.id.copy");
        }

        public void actionPerformed(ActionEvent e) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getId()), null);
        }
    }
}
