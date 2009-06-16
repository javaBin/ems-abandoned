package no.java.ems.server.it;

import no.java.ems.domain.Room;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractIntegrationTest {
    protected final Room room1 = new Room("Room 1");
    protected final Room room2 = new Room("Room 2");
    protected final Room room3 = new Room("Room 3");

    protected final LocalDateTime jz06sep130900 = new LocalDateTime(2006, 9, 13, 9, 0);
    protected final LocalDateTime jz06sep131015 = new LocalDateTime(2006, 9, 13, 10, 15);
    protected final LocalDateTime jz06sep131145 = new LocalDateTime(2006, 9, 13, 11, 45);
    protected final LocalDateTime jz06sep131300 = new LocalDateTime(2006, 9, 13, 13, 0);
    protected final LocalDateTime jz06sep131415 = new LocalDateTime(2006, 9, 13, 14, 15);
    protected final LocalDateTime jz06sep131545 = new LocalDateTime(2006, 9, 13, 15, 45);
    protected final LocalDateTime jz06sep131700 = new LocalDateTime(2006, 9, 13, 17, 0);

    protected final Interval jz06sep13slot1 = new Interval(jz06sep130900.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot2 = new Interval(jz06sep131015.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot3 = new Interval(jz06sep131145.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot4 = new Interval(jz06sep131300.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot5 = new Interval(jz06sep131415.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot6 = new Interval(jz06sep131545.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot7 = new Interval(jz06sep131700.toDateTime(), Minutes.minutes(60));
}
