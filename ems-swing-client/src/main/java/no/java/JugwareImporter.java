package no.java;

import no.java.ems.client.RestEmsService;
import no.java.ems.domain.*;
import no.java.ems.service.EmsService;
import nu.xom.*;
import org.apache.commons.lang.SystemUtils;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: Mar 30, 2008
 *
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public class JugwareImporter extends SingleFrameApplication {

    private EmsService emsService;

    public static void main(String[] args) {
        JugwareImporter.launch(JugwareImporter.class, args);
    }

    public static JugwareImporter getInstance() {
        return getInstance(JugwareImporter.class);
    }

    protected void initialize(String[] args) {
        super.initialize(args);
        String importURL = System.getProperty("ems-host", "http://localhost:3000/ems");
        emsService = new RestEmsService(importURL);
    }

    protected void startup() {
        final JPanel panel = new JPanel();
        panel.add(
                new JButton(
                        new AbstractAction("SELECT FILE") {
                            public void actionPerformed(ActionEvent event) {
                                JFileChooser chooser = new JFileChooser(SystemUtils.USER_DIR);
                                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                chooser.setMultiSelectionEnabled(false);
                                int val = chooser.showOpenDialog(getMainFrame());
                                if (val == JFileChooser.APPROVE_OPTION) {
                                    File file = chooser.getSelectedFile();
                                    JugwareImporter.getInstance().getContext().getTaskService().execute(new ImportTask(file));
                                }

                            }
                        }
                )
        );
        show(panel);
    }

    public EmsService getEmsService() {
        return emsService;
    }

    private static class ImportTask extends Task<Void, Void> {

        private File file;
        private Map<String, Person> persons = new HashMap<String, Person>();

        public ImportTask(File file) {
            super(JugwareImporter.getInstance());
            this.file = file;
        }

        protected Void doInBackground() throws Exception {
            Document doc = new Builder().build(file);
            Element root = doc.getRootElement();
            Nodes nodes = root.query("//person");
            EmsService service = JugwareImporter.getInstance().getEmsService();
            for (int i = 0; i < nodes.size(); i++) {
                Element element = (Element)nodes.get(i);
                String name = element.getFirstChildElement("name").getValue();
                String desc = element.getFirstChildElement("description").getValue();
                boolean foreign = Boolean.parseBoolean(element.getAttributeValue("foreign"));

                List<EmailAddress> addresses = new ArrayList<EmailAddress>();

                parseEmailAdresses(element, addresses);

                Person person = new Person();
                person.setName(name);
                person.setDescription(desc);
                person.setLanguage(foreign ? new Language("en") : new Language("no"));
                person.setEmailAddresses(addresses);
                parseTags(element, person);
                service.saveContact(person);
                Thread.sleep(50);
                persons.put(element.getAttributeValue("personId"), person);
            }
            nodes = root.query("//event");
            for (int i = 0; i < nodes.size(); i++) {
                Element element = (Element)nodes.get(i);
                String name = element.getFirstChildElement("name").getValue();
                Event event = new Event(name);
                parseTags(element, event);
                service.saveEvent(event);
                Thread.sleep(50);
                parseSessions(element, event.getId());
            }

            return null;
        }

        private void parseSessions(Element element, String eventId) {
            Elements tagElements = element.getFirstChildElement("talks").getChildElements();
            for (int i = 0; i < tagElements.size(); i++) {
                Element tagElement = tagElements.get(i);
                Session session = new Session();
                session.setEventId(eventId);
                String title = tagElement.getFirstChildElement("title").getValue();
                session.setTitle(title);
                parseTags(tagElement, session);
                Elements speakerElements = tagElement.getFirstChildElement("speakers").getChildElements();
                for (int j = 0; j < speakerElements.size(); j++) {
                    Element speakerElement = speakerElements.get(j);
                    String personId = speakerElement.getAttributeValue("personId");
                    Person person = persons.get(personId);
                    Speaker speaker = new Speaker(person.getId(), person.getName());
                    parseTags(speakerElement, speaker);
                    session.addSpeaker(speaker);
                }
                EmsService service = JugwareImporter.getInstance().getEmsService();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    //ignore
                }
                service.saveSession(session);
            }

        }

        private void parseTags(Element element, AbstractEntity entity) {
            List<String> tags = new ArrayList<String>();
            Elements tagElements = element.getFirstChildElement("tags").getChildElements();
            for (int j = 0; j < tagElements.size(); j++) {
                Element tagElement = tagElements.get(j);
                String tag = tagElement.getValue();
                if (tag != null) {
                    tags.add(tag);
                }
            }
            entity.setTags(tags);
        }

        private void parseEmailAdresses(Element element, List<EmailAddress> addresses) {
            final Element emailAdressesElement = element.getFirstChildElement("email-addresses");
            if (emailAdressesElement != null) {
                Elements emailElements = emailAdressesElement.getChildElements();
                for (int j = 0; j < emailElements.size(); j++) {
                    Element mailElement = emailElements.get(j);
                    if (mailElement.getValue() != null) {
                        addresses.add(new EmailAddress(mailElement.getValue()));
                    }
                }
            }
        }
    }
}
