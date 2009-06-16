package no.java.ems.client.swing.sessions;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.client.swing.EmsClient;
import no.java.ems.client.swing.Entities;
import no.java.ems.client.swing.PhotoPanel;
import no.java.ems.domain.Binary;
import no.java.ems.domain.Person;
import no.java.ems.domain.Speaker;
import no.java.swing.DefaultPanel;
import no.java.swing.SwingHelper;
import org.jdesktop.beansbinding.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class SpeakerPanel extends DefaultPanel {

    private final Speaker speaker;
    private JLabel nameField;
    private JTextArea descriptionField;
    private PhotoPanel photoPanel;

    public SpeakerPanel(final Speaker speaker) {
        this.speaker = speaker;
        initialize();
    }

    public void initModels() {
    }

    public void initActions() {
    }

    public void initComponents() {
        photoPanel = new PhotoPanel(new Dimension(75, 100));
        photoPanel.setBackground(Color.WHITE);
        nameField = new JLabel();
        descriptionField = new JTextArea(1, 20);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        SwingHelper.setTabFocusTraversalKeys(descriptionField);
    }

    public void initBindings() {
    }

    public void initListeners() {
        nameField.addMouseListener(new OpenPersonHandler());
        speaker.addPropertyChangeListener(
                "photo",
                new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent event) {
                        Binary binary = (Binary)event.getNewValue();
                        if (Entities.isLocalBinary(binary)) {
                            SwingUtilities.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            // todo: consider the same behaviour when saving a changed description and no description exist for the contact
                                            Person contact = Entities.getInstance().getContact(speaker.getPersonURI());
                                            if (event.getNewValue() != null) {
                                                if (contact.getPhoto() != null) {
                                                    int answer = JOptionPane.showConfirmDialog(
                                                            getRootPane(),
                                                            getString("updateContactPhoto"),
                                                            null,
                                                            JOptionPane.YES_NO_OPTION,
                                                            JOptionPane.PLAIN_MESSAGE
                                                    );
                                                    if (answer == JOptionPane.YES_OPTION) {
                                                        contact.setPhoto(speaker.getPhoto());
                                                    }
                                                } else {
                                                    contact.setPhoto(speaker.getPhoto());
                                                }
                                            } else {
                                                if (contact.getPhoto() != null) {
                                                    int answer = JOptionPane.showConfirmDialog(
                                                            getRootPane(),
                                                            getString("removeContactPhoto"),
                                                            null,
                                                            JOptionPane.YES_NO_OPTION,
                                                            JOptionPane.PLAIN_MESSAGE
                                                    );
                                                    if (answer == JOptionPane.YES_OPTION) {
                                                        contact.setPhoto(speaker.getPhoto());
                                                    }
                                                }
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    public void initLayout() {
        CellConstraints cc = new CellConstraints();
        setBorder(Borders.DLU4_BORDER);
        setLayout(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new FormLayout("f:d:g", "p,3dlu,f:d:g"));
        fields.add(nameField, cc.xy(1, 1));
        fields.add(new JScrollPane(descriptionField), cc.xy(1, 3));
        add(fields, BorderLayout.CENTER);
        add(photoPanel, BorderLayout.WEST);
        setOpaque(false);
        fields.setOpaque(false);
    }

    public void initState() {
        AutoBinding<Speaker, String, JLabel, String> nameBinding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ,
                speaker,
                BeanProperty.<Speaker, String>create("name"),
                nameField,
                BeanProperty.<JLabel, String>create("text")
        );
        AutoBinding<Speaker, String, JTextComponent, String> descriptionBinding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                speaker,
                BeanProperty.<Speaker, String>create("description"),
                descriptionField,
                BeanProperty.<JTextComponent, String>create("text")
        );
        BindingGroup bindingGroup = new BindingGroup();
        bindingGroup.addBinding(nameBinding);
        bindingGroup.addBinding(descriptionBinding);
        bindingGroup.addBindingListener(
                new AbstractBindingListener() {
                    @Override
                    public void syncFailed(final Binding binding, final Binding.SyncFailure failure) {
                        System.err.println(String.format("%s: %s", binding.getName(), failure));
                    }
                }
        );
        bindingGroup.addBinding(
                Bindings.createAutoBinding(
                        AutoBinding.UpdateStrategy.READ_WRITE,
                        speaker,
                        BeanProperty.<Speaker, Binary>create("photo"),
                        photoPanel,
                        BeanProperty.<PhotoPanel, Binary>create("binary")
                )
        );
        bindingGroup.bind();
        SwingHelper.visitChildren(
                this,
                new SwingHelper.ComponentVisitor() {
                    public void visit(final Component component) {
                        if (component instanceof JComponent) {
                            ((JComponent)component).setInheritsPopupMenu(true);
                        }
                    }
                },
                true
        );
    }

    private class OpenPersonHandler extends MouseAdapter {

        @Override
        public void mouseClicked(final MouseEvent event) {
            if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2 && event.getModifiersEx() == 0) {
                EmsClient.getInstance().edit(Entities.getInstance().getContact(speaker.getPersonURI()));
            }
        }

    }

}
