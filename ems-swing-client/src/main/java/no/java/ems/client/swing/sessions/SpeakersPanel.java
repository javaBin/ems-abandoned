package no.java.ems.client.swing.sessions;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.swing.EmsClient;
import no.java.ems.client.swing.Entities;
import no.java.ems.domain.Person;
import no.java.ems.domain.Session;
import no.java.ems.domain.Speaker;
import no.java.swing.ComponentList;
import no.java.swing.DefaultPanel;
import no.java.swing.SwingHelper;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class SpeakersPanel extends DefaultPanel implements ComponentList.ComponentFactory {

    private final Session session;
    private Action addAction;
    private Action removeAction;
    private Action moveUpAction;
    private Action moveDownAction;
    private ComponentList speakerList;

    public SpeakersPanel(final Session session) {
        super(SpeakersPanel.class.getName());
        this.session = session;
        initialize();
    }

    public ComponentList getSpeakerList() {
        return speakerList;
    }

    public void initModels() {
    }

    public void initActions() {
        addAction = new AddAction();
        removeAction = new RemoveAction();
        moveUpAction = new MoveUpAction();
        moveDownAction = new MoveDownAction();
        // todo: add open contact action to context menu
        // todo: add drag drop support
    }

    public void initComponents() {
        speakerList = new ComponentList(this);
        speakerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        SwingHelper.setTabFocusTraversalKeys(speakerList);
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(addAction);
        popupMenu.addSeparator();
        popupMenu.add(removeAction);
        popupMenu.addSeparator();
        popupMenu.add(moveUpAction);
        popupMenu.add(moveDownAction);
        setComponentPopupMenu(popupMenu);
        speakerList.setComponentPopupMenu(popupMenu);
    }

    public void initBindings() {
    }

    public void initListeners() {
        speakerList.addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(final ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            removeAction.setEnabled(speakerList.getSelectedIndex() != -1);
                        }
                    }
                }
        );
        // todo: make sure right-clicking selects row first
        SwingHelper.bindAction(addAction, this, (KeyStroke)addAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingHelper.bindAction(removeAction, this, (KeyStroke)removeAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingHelper.bindAction(moveUpAction, this, (KeyStroke)moveUpAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingHelper.bindAction(moveDownAction, this, (KeyStroke)moveDownAction.getValue(Action.ACCELERATOR_KEY), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void initLayout() {
        FormLayout layout = new FormLayout("0:g,3dlu,p,1dlu,p,1dlu,p,1dlu,p", "p");
        layout.setColumnGroups(new int[][]{{3, 5, 7, 9}});
        JPanel buttonBar = new JPanel(layout);
        buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        CellConstraints cc = new CellConstraints();
        buttonBar.add(new JButton(addAction), cc.xy(3, 1));
        buttonBar.add(new JButton(removeAction), cc.xy(5, 1));
        buttonBar.add(new JButton(moveUpAction), cc.xy(7, 1));
        buttonBar.add(new JButton(moveDownAction), cc.xy(9, 1));
        for (Component component : buttonBar.getComponents()) {
            if (component instanceof JButton) {
                ((JButton)component).setText("");
            }
        }
        setLayout(new BorderLayout());
        add(speakerList, BorderLayout.CENTER);
        add(buttonBar, BorderLayout.SOUTH);
    }

    public void initState() {
    }

    public JComponent createComponent(final Object modelObject) {
        Speaker speaker = (Speaker)modelObject;
        speaker.addPropertyChangeListener(
                "modified",
                new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent event) {
                        session.setModified(true);
                    }
                }
        );
        return new SpeakerPanel(speaker);
    }

    private class AddAction extends DefaultAction {

        private ContactSelector contactSelector;

        public AddAction() {
            super("add");
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            if (contactSelector == null) {
                contactSelector = new ContactSelector();
            }
            Person person = contactSelector.showSelectDialog(SpeakersPanel.this);
            if (person != null) {
                if (person.getURI() != null) {
                    Speaker speaker = new Speaker(person.getURI(), person.getName());
                    speaker.setDescription(person.getDescription());
                    speaker.setPhoto(person.getPhoto());
                    session.addSpeaker(speaker);
                } else {
                    EmsClient.getInstance().getContext().getTaskService().execute(new CreateContactTask(person));
                }
            }
        }

    }

    private class RemoveAction extends DefaultAction {

        public RemoveAction() {
            super("remove");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            for (Object speakerObject : speakerList.getSelectedValues()) {
                session.removeSpeaker((Speaker)speakerObject);
            }
        }

    }

    private class MoveUpAction extends DefaultAction {

        public MoveUpAction() {
            super("moveUp");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
        }

    }

    private class MoveDownAction extends DefaultAction {

        public MoveDownAction() {
            super("moveDown");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
        }

    }

    private class CreateContactTask extends DefaultTask<Person, Void> {

        private final Person person;

        public CreateContactTask(final Person person) {
            super("createContactTask");
            this.person = person;
        }

        protected Person doInBackground() throws Exception {
            return EmsClient.getInstance().getClientService().saveContact(person);
        }

        @Override
        protected void succeeded(final Person person) {
            Entities.getInstance().add(person);
            Speaker speaker = new Speaker(person.getURI(), person.getName());
            speaker.setDescription(person.getDescription());
            speaker.setPhoto(person.getPhoto());
            session.addSpeaker(speaker);
            EmsClient.getInstance().edit(person);
            super.succeeded(person);
        }

    }

}
