package no.java.ems.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import no.java.ems.client.RestEmsService;
import no.java.ems.domain.*;
import no.java.ems.service.EmsService;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public class ExportJson {
    private static final DateTimeFormatter format = ISODateTimeFormat.basicDateTimeNoMillis();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        EmsService service = new RestEmsService(args[0]);
        File tempDir = new File("/tmp/ems");
        tempDir.mkdirs();
        exportContacts(tempDir, service.getContacts());
        exportEvents(tempDir, service.getEvents());
    }

    private static void exportContacts(File targetDirectory, List<Person> contacts) throws IOException {
        File file = new File(targetDirectory, "contacts.json");
        ArrayNode arrayNode = mapper.createArrayNode();
        if (contacts.isEmpty()) {
            System.err.println("No contacts found. aborting!");
        }
        else {
            System.err.println(String.format("Found %s contacts", contacts.size()));
            for (Person contact : contacts) {
                ObjectNode object = arrayNode.addObject();
                object.put("id", contact.getId());
                object.put("name", contact.getName());
                object.put("bio", contact.getDescription());
                String language = contact.getLanguage() != null ? contact.getLanguage().getIsoCode() : "no";
                object.put("language", language);
                Binary photo = contact.getPhoto();
                if (photo != null) {
                    File f = new File(targetDirectory, photo.getFileName());
                    if (f.exists()) {
                        object.put("photo", f.getAbsolutePath());
                    }
                    else {
                        ByteStreams.copy(photo.getDataStream(), Files.newOutputStreamSupplier(f));
                        while (!f.exists());
                        object.put("photo", f.getAbsolutePath());
                    }
                }
                List<EmailAddress> emails = contact.getEmailAddresses();
                ArrayNode emailAddresses = mapper.createArrayNode();
                for (EmailAddress email : emails) {
                    emailAddresses.add(email.getEmailAddress());
                }
                object.put("emails", emailAddresses);
            }
            ObjectNode root = mapper.createObjectNode();
            root.put("contacts", arrayNode);
            mapper.writeValue(Files.newWriter(file, Charsets.UTF_8), root);
            System.out.println(String.format("Wrote file %s", file.getAbsolutePath()));
        }

    }

    private static void exportEvents(File targetDirectory, List<Event> events) throws IOException {
        File file = new File(targetDirectory, "events.json");
        ArrayNode arrayNode = mapper.createArrayNode();
        if (events.isEmpty()) {
            System.err.println("No events found. aborting!");
        }
        else {
            System.err.println(String.format("Found %s events", events.size()));
            for (Event event : events) {
                ObjectNode object = arrayNode.addObject();
                object.put("id", event.getId());
                object.put("name", event.getName());
                object.put("venue", event.getName().toLowerCase().contains("javazone") ? "Oslo Spektrum" : "Unknown");
                if (event.getDate() != null) {
                    object.put("start", event.getDate().toString(format));
                    object.put("end", event.getDate().toString(format));
                }
                object.put("rooms", makeArrayFrom(event.getRooms(), new Function<Room, JsonNode>() {
                    public JsonNode apply(Room input) {
                        ObjectNode n = mapper.createObjectNode();
                        n.put("name", input.getName());
                        return n;
                    }
                }));
                object.put("slots", makeArrayFrom(event.getTimeslots(), new Function<Interval, JsonNode>() {
                    public JsonNode apply(Interval input) {
                        ObjectNode n = mapper.createObjectNode();
                        n.put("start", input.getStart().toString(format));
                        n.put("end", input.getEnd().toString(format));
                        return n;
                    }
                }));
            }
            ObjectNode root = mapper.createObjectNode();
            root.put("events", arrayNode);
            mapper.writeValue(Files.newWriter(file, Charsets.UTF_8), root);
            System.out.println(String.format("Wrote file %s", file.getAbsolutePath()));
        }

    }

    private static <A> JsonNode makeArrayFrom(List<A> list, Function<A, JsonNode> f) {
        return mapper.createArrayNode().addAll(Lists.transform(list, f));
    }
}
