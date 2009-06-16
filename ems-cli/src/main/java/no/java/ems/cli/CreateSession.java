package no.java.ems.cli;

import no.java.ems.client.SessionsClient;
import no.java.ems.domain.Session;
import org.apache.commons.cli.Options;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CreateSession extends AbstractCli {
    private static final String OPTION_EVENT_ID = "event-id";
    private static final String OPTION_DATE = "date";
    private static final String OPTION_TITLE = "title";
    private static final String OPTION_LEAD = "lead";
    private static final String OPTION_BODY = "body";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.shortDateTime();

    public CreateSession() {
        super("create-session");
    }

    public static void main(String[] args) throws Exception {
        new CreateSession().doMain(args);
    }

    protected Options addOptions(Options options) {
        String pattern = DateTimeFormat.patternForStyle("SS", null);

        options.addOption(null, OPTION_EVENT_ID, true, "The event that this session is for");
        options.addOption(null, OPTION_DATE, true, "The date and time for the session. Format: " + pattern);
        options.addOption(null, OPTION_TITLE, true, "");
        options.addOption(null, OPTION_LEAD, true, "");
        options.addOption(null, OPTION_BODY, true, "");

        return options;
    }

    public void work() {
        if(!assertIsPresent(OPTION_EVENT_ID) ||
            !assertIsPresent(OPTION_DATE) ||
            !assertIsPresent(OPTION_TITLE)) {
            usage();
            return;
        }
        
        String eventId = getCommandLine().getOptionValue(OPTION_EVENT_ID);
        String dateString = getCommandLine().getOptionValue(OPTION_DATE);
        String title = getCommandLine().getOptionValue(OPTION_TITLE);
        String lead = getCommandLine().getOptionValue(OPTION_LEAD);
        String body = getCommandLine().getOptionValue(OPTION_BODY);

        LocalDateTime date;

        date = DATE_TIME_FORMATTER.parseDateTime(dateString).toLocalDateTime();

        Interval timeslot = new Interval(date.toDateTime(), Minutes.minutes(60));
        
        Session session = new Session();
        session.setEventId(eventId);
        session.setTimeslot(timeslot);
        session.setState(Session.State.Pending);
        session.setFormat(Session.Format.Presentation);
        session.setLevel(Session.Level.Intermediate);
        session.setTitle(title);
        session.setLead(lead);
        session.setBody(body);
//        Language language;
//        List<String> keywords = new ArrayList<String>();
//        List<Speaker> speakers = new ArrayList<Speaker>();

        SessionsClient sessionsClient = getEms().getSessionsClient();
        sessionsClient.createSession(session);

        System.err.println("Session created, id: " + session.getId());
    }
}
