package no.java.ems.server.restlet;

import no.java.ems.dao.PersonDao;
import no.java.ems.domain.Person;
import no.java.ems.server.solr.ResourceToSolrTranslator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.springframework.dao.DataAccessException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class PersonResource extends GenericResource<Person> {

    private final PersonDao personDao;

    public PersonResource(Context context, Request request, Response response) {
        super(context, request, response, "pid");
        getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        personDao = (PersonDao)context.getAttributes().get("personDao");
    }

    public Representation getRepresentation(Variant variant) {
        Serializable resource;
        if (StringUtils.isBlank(getId())) {
            resource = new ArrayList<Person>(personDao.getPersons());
        } else {
            resource = personDao.getPerson(getId());
        }
        if (variant.getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT)) {
            return new ObjectRepresentation(resource);
        } else if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            return new StringRepresentation(ToStringBuilder.reflectionToString(resource, ToStringStyle.MULTI_LINE_STYLE));
        }
        return super.getRepresentation(variant);
    }

    public void put(Representation entity) {
        try {
            String id = getId();
            Validate.notEmpty(id);
            Person resource = extractObject(entity);
            Validate.isTrue(id.equals(resource.getId()));
            personDao.savePerson(resource);
            index(resource, false);
            getResponse().setStatus(Status.SUCCESS_OK);
        } catch (Exception e) {
            log.debug("Putting resource", e);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

    public void post(Representation entity) {
        try {
            Person person = extractObject(entity);
            Validate.isTrue(person.getId() == null);
            Validate.isTrue(person.getRevision() == 0);
            personDao.savePerson(person);
            Reference redirect = new Reference(getRequest().getResourceRef().toString() + person.getId());
            getResponse().setRedirectRef(redirect);
            index(person, false);
            getResponse().setStatus(Status.SUCCESS_CREATED);
        } catch (Exception e) {
            log.debug("Failed creation of person", e);
            getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
        }
    }

    public void delete() {
        try {
            Person person = personDao.getPerson(getId());
            personDao.deletePerson(getId());
            log.debug(String.format("Resource with id %s removed", getId()));
            index(person, true);
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        } catch (DataAccessException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }

    }

    public boolean allowDelete() {
        return true;
    }

    public boolean allowPut() {
        return true;
    }

    public boolean allowPost() {
        return true;
    }

}
