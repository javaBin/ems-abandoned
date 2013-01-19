package no.java.ems.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import no.java.ems.domain.*;
import no.java.ems.service.EmsService;
import org.apache.commons.cli.Options;
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
public final class ExportJson extends AbstractCli {
    private static final DateTimeFormatter format = ISODateTimeFormat.basicDateTimeNoMillis();
    private static final ObjectMapper mapper = new ObjectMapper();

    public ExportJson() {
        super("export-json");
    }

    @Override
    protected void work() throws Exception {
        String fileString = getCommandLine().getOptionValue("dir");
        File directory = fileString != null ? new File(fileString) : new File("/tmp/ems");
        directory.mkdirs();
        exportEvents(directory, getEms());
    }

    @Override
    protected Options addOptions(Options defaultOptions) {
        defaultOptions.addOption(null, "dir", true, "Export directory, This defaults to '/tmp/ems' if not set.");
        return defaultOptions;
    }

    @Override
    protected String getDefaultEventId() {
        return null;
    }

    public static void main(String[] args) throws Exception {
        new ExportJson().doMain(args);
    }

    private Map<String, Person> mapify(List<Person> contacts) {
        Map<String, Person> map = new HashMap<String, Person>();
        for (Person contact : contacts) {
            map.put(contact.getId(), contact);
        }
        return map;
    }

    private void exportEvents(File targetDirectory, EmsService service) throws IOException {
        Map<String, Person> contacts = mapify(getEms().getContacts());
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
                        contacts,
                        intervals,
                        rooms,
                        service.getSessions(event.getId())
                );
                System.out.println("Done exporting " + event.getName());
            }
        }

    }

    private void exportSessions(final File targetDirectory,
                                       final Map<String, Person> contacts,
                                       Map<Interval, String> intervals,
                                       Map<String, String> rooms,
                                       List<Session> sessions) throws IOException {
        File file = new File(targetDirectory, "sessions.json");
        ArrayNode arrayNode = mapper.createArrayNode();
        if (sessions.isEmpty()) {
            System.err.println("No sessions found. aborting!");
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
                object.put("format", getFormat(session.getFormat()));
                object.put("state", session.getState().name().toLowerCase().replace("_", "-"));
                object.put("level", session.getLevel().name().toLowerCase().replace("_", "-"));
                object.put("published", session.isPublished());
                object.put("lang", session.getLanguage() != null ? session.getLanguage().getIsoCode() : "no");
                object.put("tags", session.getTagsAsString(","));
                object.put("keywords", session.getKeywordsAsString(","));
                List<Binary> attachements = session.getAttachements();
                for (Binary binary : attachements) {
                    ArrayNode attachments = mapper.createArrayNode();
                    File att = downloadBinary(new File(targetDirectory, session.getId()), binary);
                    if (att != null && att.exists()) {
                        attachments.add(att.getAbsolutePath());
                    }
                    if (attachments.size() > 0) {
                        object.put("attachments", attachments);
                    }
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
                        Person contact = contacts.get(input.getPersonId());
                        ObjectNode n = mapper.createObjectNode();
                        n.put("id", input.getPersonId());
                        n.put("name", input.getName());
                        if (!contact.getEmailAddresses().isEmpty()) {
                            int size = contact.getEmailAddresses().size();
                            n.put("email", contact.getEmailAddresses().get(size - 1).getEmailAddress());
                        }
                        else {
                            System.out.println("No email for speaker: " + input.getName());
                            n.put("email", "dummy@example.com");
                        }
                        n.put("bio", input.getDescription());
                        ArrayNode tags = mapper.createArrayNode();
                        tags.add("import_2013");
                        n.put("tags", tags);
                        File photo = downloadBinary(targetDirectory, input.getPhoto());
                        if (photo != null && photo.exists()) {
                            n.put("photo", photo.getAbsolutePath());
                        }
                        return n;
                    }
                }));
            }
            ObjectNode root = mapper.createObjectNode();
            root.put("sessions", arrayNode);
            mapper.writeValue(Files.newWriter(file, Charsets.UTF_8), root);
            System.out.println(String.format("Wrote file %s", file.getAbsolutePath()));
        }

    }

    private String getFormat(Session.Format format) {
        switch (format) {
            case Presentation:
                return "presentation";
            case BoF:
                return "bof";
            case PanelDebate:
                return "panel";
            case Quickie:
                return "lightning-talk";
            case Course:
            default:
                throw new IllegalArgumentException("No format mapping for this" + format);
        }
    }

    private <A> JsonNode makeArrayFrom(List<A> list, Function<A, JsonNode> f) {
        return mapper.createArrayNode().addAll(Lists.transform(list, f));
    }

    private File downloadBinary(File targetDirectory, Binary binary) {
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new IllegalArgumentException("Failed to create " + targetDirectory);
        }
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
