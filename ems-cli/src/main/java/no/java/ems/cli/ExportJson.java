package no.java.ems.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public class ExportJson {
    private static final DateTimeFormatter format = ISODateTimeFormat.basicDateTimeNoMillis();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        EmsService service = new RestEmsService(args[0]);
        if (args.length >= 3) {
            service.setCredentials(args[1], args[2]);
        }
        File directory = args.length >= 4 ? new File(args[3]) : new File("/tmp/ems");
        directory.mkdirs();
        exportContacts(directory, service.getContacts());
        exportEvents(directory, service);
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
                File f = downloadBinary(targetDirectory, photo);
                if (f != null && f.exists()) {
                    object.put("photo", f.getAbsolutePath());
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

    private static void exportEvents(File targetDirectory, EmsService service) throws IOException {
        File file = new File(targetDirectory, "events.json");
        ArrayNode arrayNode = mapper.createArrayNode();
        List<Event> events = service.getEvents();
        final Map<Interval, String> intervals = new HashMap<Interval, String>();
        final Map<String, String> rooms = new HashMap<String, String>();
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
                        String id = UUID.randomUUID().toString();
                        rooms.put(input.getName(), id);
                        n.put("id", id);
                        n.put("name", input.getName());
                        return n;
                    }
                }));
                object.put("slots", makeArrayFrom(event.getTimeslots(), new Function<Interval, JsonNode>() {
                    public JsonNode apply(Interval input) {
                        ObjectNode n = mapper.createObjectNode();
                        String id = UUID.randomUUID().toString();
                        intervals.put(input, id);
                        n.put("id", id);
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

            for (Event event : events) {
                File eventDir = new File(targetDirectory, event.getId());
                eventDir.mkdirs();
                exportSessions(
                        eventDir,
                        intervals,
                        rooms,
                        service.getSessions(event.getId())
                );
            }
        }

    }

    private static void exportSessions(final File targetDirectory,
                                       Map<Interval, String> intervals,
                                       Map<String, String> rooms,
                                       List<Session> sessions) throws IOException {
        File file = new File(targetDirectory, "sessions.json");
        ArrayNode arrayNode = mapper.createArrayNode();
        if (sessions.isEmpty()) {
            System.err.println("No events found. aborting!");
        }
        else {
            System.err.println(String.format("Found %s sessions", sessions.size()));
            for (Session session : sessions) {
                ObjectNode object = arrayNode.addObject();
                object.put("id", session.getId());
                object.put("eventId", session.getEventId());
                object.put("title", session.getTitle());
                if (session.getBody() != null) {
                    object.put("body", session.getBody());
                }
                if (session.getEquipment() != null) {
                    object.put("equipment", session.getEquipment());
                }
                if (session.getExpectedAudience() != null) {
                    object.put("audience", session.getExpectedAudience());
                }
                if (session.getOutline() != null) {
                    object.put("outline", session.getOutline());
                }
                if (session.getLead() != null) {
                    object.put("summary", session.getLead());
                }
                if (session.getTimeslot() != null) {
                    String id = intervals.get(session.getTimeslot());
                    if (id != null) {
                        object.put("slot", id);
                    }
                }
                object.put("format", session.getFormat().name().toLowerCase().replace("_", "-"));
                object.put("state", session.getState().name().toLowerCase().replace("_", "-"));
                object.put("level", session.getLevel().name().toLowerCase().replace("_", "-"));
                object.put("published", session.isPublished());
                object.put("locale", session.getLanguage() != null ? session.getLanguage().getIsoCode() : "no");
                object.put("tags", session.getTagsAsString(","));
                object.put("keywords", session.getKeywordsAsString(","));
                List<Binary> att = session.getAttachements();
                for (Binary binary : att) {
                    downloadBinary(targetDirectory, binary);
                }
                if (session.getRoom() != null) {
                    String room = session.getRoom().getName();
                    String id = rooms.get(room);
                    if (id != null) {
                        object.put("room", id);
                    }
                }
                object.put("speakers", makeArrayFrom(session.getSpeakers(), new Function<Speaker, JsonNode>() {
                    public JsonNode apply(Speaker input) {
                        ObjectNode n = mapper.createObjectNode();
                        n.put("id", input.getPersonId());
                        n.put("name", input.getName());
                        n.put("bio", input.getDescription());
                        File photo = downloadBinary(targetDirectory, input.getPhoto());
                        if (photo != null && photo.exists()) {
                            n.put("photo", input.getPhoto().getFileName());
                        }
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

    private static File downloadBinary(File targetDirectory, Binary binary) {
        if (binary != null) {
            File f = new File(targetDirectory, binary.getFileName());
            if (f.exists()) {
                return f;
            }
            else {
                if (binary instanceof UriBinary) {
                    URI uri = ((UriBinary) binary).getUri();
                    try {
                        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                        conn.connect();
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            System.err.println(String.format("Downloading binary with length %s(%s MB) to %s", binary.getSize(), binary.getSize() / 8 / 1000 / 1000.0, f));
                            InputStream is = conn.getInputStream();
                            try {
                                long copied = ByteStreams.copy(is, Files.newOutputStreamSupplier(f));
                                System.err.println(String.format("Wrote %s bytes", copied));
                                return f;
                            } finally {
                                Closeables.closeQuietly(is);
                                conn.disconnect();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
        return null;
    }
}
