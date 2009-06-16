package no.java.ems.server.solr;

import no.java.ems.domain.Event;
import no.java.ems.domain.Nationality;
import no.java.ems.domain.Person;
import no.java.ems.domain.Session;
import org.joda.time.LocalDate;
import org.joda.time.Interval;
import org.joda.time.DateTime;
import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.solr.common.SolrInputDocument;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 *
 * TODO: FIXME!
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class ResourceSolrTranslatorTestCase {

    @Test
    public void testIndexSession() throws Exception {
        Event event = new Event("some event");
        event.setId(UUID.randomUUID().toString());
        event.setDate(new LocalDate());
        Session session = new Session("Some session");        
        session.setId("id");
        String startDate = "2008-08-25T15:09:50Z";
        String endDate = "2008-08-25T15:10:50Z";
        SimpleDateFormat formatter = new SimpleDateFormat(ResourceToSolrTranslator.UTC_TIMEFORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        session.setTimeslot(new Interval(new DateTime(formatter.parse(startDate).getTime()), new DateTime(formatter.parse(endDate).getTime())));
        SolrInputDocument document = createIndexDocument(session);
        assertEquals("Wrong number of fields", 9, document.getFieldNames().size());
        assertEquals("Wrong date format", startDate, document.getField("slot_start").getValue());

    }

    private SolrInputDocument createIndexDocument(Session resource) {
        Map<String, Object> additionalIndexedFields = new HashMap<String, Object>();
        additionalIndexedFields.put("eventid", resource.getEventId());
        additionalIndexedFields.put("slot", resource.getTimeslot());
        additionalIndexedFields.put("state", resource.getState());
        additionalIndexedFields.put("format", resource.getFormat());
        additionalIndexedFields.put("room", resource.getRoom());
        additionalIndexedFields.put("level", resource.getLevel());
        additionalIndexedFields.put("title", resource.getTitle());
        additionalIndexedFields.put("lead_text", resource.getLead());
        additionalIndexedFields.put("body_text", resource.getBody());
        additionalIndexedFields.put("language", resource.getLanguage());
        additionalIndexedFields.put("speakers", resource.getSpeakers());
        additionalIndexedFields.put("published", resource.isPublished());
        ResourceToSolrTranslator translator = new ResourceToSolrTranslator();
        translator.add(resource, additionalIndexedFields);
        return translator.getInputDocument();
    }

    public void testTwoResourceXML() throws Exception {
        Person person = new Person("Ola nordmann");
        person.setId(UUID.randomUUID().toString());
        person.setDescription("Some description");
        person.setGender(Person.Gender.Male);
        person.setNationality(new Nationality("NO"));
        person.setTags(Arrays.asList("Super tag"));
        ResourceToSolrTranslator translator = new ResourceToSolrTranslator();
        //translator.add(person, Arrays.asList("id", "name", "description", "gender"));

      //  assertNotNull(translator.getXML());

        person = new Person("Kari Nordmann");
        person.setId(UUID.randomUUID().toString());
        person.setDescription("Some description");
        person.setGender(Person.Gender.Female);
        person.setNationality(new Nationality("NO"));

        //translator.add(person, Arrays.asList("id", "name", "description", "gender"));
        //assertNotNull(translator.getXML());
        //System.out.println("translator: " + translator.getXML());
    }
}
