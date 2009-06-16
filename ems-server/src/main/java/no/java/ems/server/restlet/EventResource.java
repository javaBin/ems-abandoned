package no.java.ems.server.restlet;

import no.java.ems.dao.EventDao;
import no.java.ems.domain.Event;
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

import java.io.Serializable;
import java.util.ArrayList;

public class EventResource extends GenericResource<Event> {

    private EventDao eventDao;

    public EventResource(Context context, Request request, Response response) {
        super(context, request, response, "eid");
        getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        eventDao = (EventDao)context.getAttributes().get("eventDao");
    }

    public Representation getRepresentation(Variant variant) {
        Serializable resource;
        if (StringUtils.isBlank(getId())) {
            resource = new ArrayList<Event>(eventDao.getEvents());
        } else {
            resource = eventDao.getEvent(getId());
        }
        if (variant.getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT)) {
            return new ObjectRepresentation(resource);
        } else if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            return new StringRepresentation(ToStringBuilder.reflectionToString(resource, ToStringStyle.MULTI_LINE_STYLE));
        }
        return super.getRepresentation(variant);
    }

    @Override
    public void put(Representation entity) {
        String id = getId();
        Validate.notEmpty(id);
        Event resource = extractObject(entity);
        Validate.isTrue(id.equals(resource.getId()));
        eventDao.saveEvent(resource);
        index(resource, false);
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    @Override
    public void post(Representation entity) {
        Event resource = extractObject(entity);
        Validate.isTrue(resource.getId() == null);
        Validate.isTrue(resource.getRevision() == 0);
        eventDao.saveEvent(resource);
        index(resource, false);
        Reference redirect = new Reference(getRequest().getResourceRef().toString() + resource.getId());
        getResponse().setRedirectRef(redirect);
        getResponse().setStatus(Status.SUCCESS_CREATED);
    }

    @Override
    public void delete() {
        final Event event = eventDao.getEvent(getId());
        if (event != null) {
            eventDao.deleteEvent(getId());
            index(event, true);
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

}
