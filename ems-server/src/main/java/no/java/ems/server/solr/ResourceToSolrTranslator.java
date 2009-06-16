package no.java.ems.server.solr;

import no.java.ems.domain.AbstractEntity;
import no.java.ems.domain.Event;
import no.java.ems.domain.Person;
import no.java.ems.domain.Session;
import no.java.ems.domain.ValueObject;
import no.java.ems.server.search.SearchService;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class ResourceToSolrTranslator {

    protected static final String UTC_TIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private SolrInputDocument inputDocument = new SolrInputDocument();

    /**
     * This method will always add these fields to the solr index:
     * <ul>
     * <li>id</li>
     * <li>tags</li>
     * <li>type</li>
     * </ul>
     * It will also try to add the keywords list and name properties if they exist.
     *
     * @param resource
     * @param additionalFields
     * @throws Exception
     */
    public void add(final AbstractEntity resource, final Map<String, Object> additionalFields) {
        addField("id", resource.getId());
        addField("tags", resource.getTags());
        addTypeField(resource);
        maybeAddNamedProperty("keywords", resource);
        maybeAddNamedProperty("name", resource);
        if (additionalFields != null) {
            for (Map.Entry<String, Object> entry : additionalFields.entrySet()) {
                addField(entry.getKey(), entry.getValue());
            }
        }
    }

    private void addTypeField(AbstractEntity resource) {
        String type;
        if (resource instanceof Session) {
            type = SearchService.ObjectType.session.name();
        }
        else if (resource instanceof Person) {
            type = SearchService.ObjectType.person.name();
        }
        else if (resource instanceof Event) {
            type = SearchService.ObjectType.event.name();
        }
        else {
            throw new IllegalArgumentException("I cannot index this object type: " + resource.getClass());
        }
        addField("type", type);
    }

    private void maybeAddNamedProperty(final String pName, AbstractEntity resource) {
        try {
            Object object = PropertyUtils.getProperty(resource, pName);
            addField(pName, object);
        } catch (Exception e) {
            //there was not field with that name.
        }
    }

    private void addField(String name, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof LocalDate) { //TODO: is this correct? Shouldnt we use LocalTime here?
            DateTime time = ((LocalDate) value).toDateTimeAtStartOfDay();
            addDateTimeInUTC(name, time);
        } else if (value instanceof Interval) {
            Interval interval = (Interval) value;
            DateTime start = interval.getStart();
            DateTime end = interval.getEnd();
            addDateTimeInUTC(name + "_start", start);
            addDateTimeInUTC(name + "_end", end);
        } else if (value instanceof ValueObject) {
            ValueObject object = (ValueObject) value;
            inputDocument.addField(name, object.getIndexingValue());
        } else if (value instanceof AbstractEntity) {
            AbstractEntity object = (AbstractEntity) value;
            inputDocument.addField(name, object.getId());
        } else if (value instanceof Collection) {
            Collection list = (Collection) value;
            if (!list.isEmpty()) {
                for (Object object : list) {
                    addField(name, object);
                }
            }
        } else {
            inputDocument.addField(name, value.toString());
        }
    }

    private void addDateTimeInUTC(String name, DateTime time) {
        long utctime = time.getZone().convertLocalToUTC(time.getMillis(), true);
        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(utctime);
        DateFormat format = new SimpleDateFormat(UTC_TIMEFORMAT);
        inputDocument.addField(name, format.format(cal.getTime()));
    }

    public SolrInputDocument getInputDocument() {
        return inputDocument;
    }
}
