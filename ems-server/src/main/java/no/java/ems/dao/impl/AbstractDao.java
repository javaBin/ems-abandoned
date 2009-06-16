package no.java.ems.dao.impl;

import fj.F;
import fj.data.Option;
import static fj.data.Option.some;
import static fj.data.Option.none;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Trygve Laugstol
 */
public abstract class AbstractDao {
    protected static F<LocalDateTime, Timestamp> LocalDateTimeToSqlTimestamp = new F<LocalDateTime, Timestamp>() {
        public Timestamp f(LocalDateTime dateTime) {
            return new Timestamp(dateTime.toDateTime().getMillis());
        }
    };

    protected static F<DateTime, Timestamp> dateTimeToSqlTimestamp = new F<DateTime, Timestamp>() {
        public Timestamp f(DateTime dateTime) {
            return new Timestamp(dateTime.getMillis());
        }
    };

    protected static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return new LocalDateTime(timestamp.getTime());
    }

    protected static Date toSqlDate(LocalDate date) {
        if (date == null) {
            return null;
        }

        return new Date(date.toDateTime(LocalTime.MIDNIGHT).getMillis());
    }

    protected static Option<Interval> mapInterval(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("start");
        if(timestamp == null) {
            return none();
        }

        LocalDateTime start = toLocalDateTime(timestamp);
        LocalDateTime end = start.withFieldAdded(DurationFieldType.minutes(), rs.getInt("durationMinutes"));
        return some(new Interval(start.toDateTime(), end.toDateTime()));
    }
}
