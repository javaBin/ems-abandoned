package no.java.ems.dao.impl;

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
    protected static Timestamp toSqlTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return new Timestamp(dateTime.toDateTime().getMillis());
    }

    protected static Timestamp toSqlTimestamp(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return new Timestamp(dateTime.getMillis());
    }

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

    protected static Interval mapInterval(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("start");
        if(timestamp == null) {
            return null;
        }

        LocalDateTime start = toLocalDateTime(timestamp);
        LocalDateTime end = start.withFieldAdded(DurationFieldType.minutes(), rs.getInt("durationMinutes"));
        return new Interval(start.toDateTime(), end.toDateTime());
    }
}
