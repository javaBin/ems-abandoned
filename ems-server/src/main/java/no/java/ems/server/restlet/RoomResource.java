/* 
 * $Header: $ 
 * 
 * Copyright (C) 2008 Escenic. 
 * All Rights Reserved.  No use, copying or distribution of this 
 * work may be made except in accordance with a valid license 
 * agreement from Escenic.  This notice must be 
 * included on all copies, modifications and derivatives of this 
 * work. 
 */
package no.java.ems.server.restlet;

import no.java.ems.dao.RoomDao;
import no.java.ems.dao.EventDao;
import no.java.ems.domain.Room;
import no.java.ems.domain.Event;
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
import java.util.List;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class RoomResource extends GenericResource<Room> {
    private static final String EVENTID = "eid";
    
    private RoomDao roomDao;
    private EventDao eventDao;

    public RoomResource(Context context, Request request, Response response) {
        super(context, request, response, "rid");
        roomDao = EmsApplication.getService(context, RoomDao.class);
        eventDao = EmsApplication.getService(context, EventDao.class);
    }

    public Representation getRepresentation(Variant variant) {
        Serializable resource;
        String eventId = (String) getRequest().getAttributes().get(EVENTID);
        if (StringUtils.isBlank(getId())) {
            resource = new ArrayList<Room>(roomDao.getRooms());
        } else {
            resource = roomDao.getRoom(getId());
        }
        if (!StringUtils.isBlank(eventId)) {
            Event event = eventDao.getEvent(eventId);
            if (event != null) {
                resource = new ArrayList<Room>(event.getRooms());
            }
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
        Room resource = extractObject(entity);
        Validate.isTrue(id.equals(resource.getId()));
        String eventId = (String) getRequest().getAttributes().get(EVENTID);
        roomDao.save(resource);
        if (eventId != null) {
            Event event = eventDao.getEvent(eventId);
            if (event != null) {
                event.getRooms().add(resource);
                eventDao.saveEvent(event);
            }
        }
        index(resource, false);
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    @Override
    public void post(Representation entity) {
        Room resource = extractObject(entity);
        Validate.isTrue(resource.getId() == null);
        Validate.isTrue(resource.getRevision() == 0);
        String eventId = (String) getRequest().getAttributes().get(EVENTID);
        roomDao.save(resource);
        if (eventId != null) {
            Event event = eventDao.getEvent(eventId);
            if (event != null) {
                List<Room> rooms = new ArrayList<Room>(event.getRooms());
                rooms.add(resource);
                event.setRooms(rooms);
                eventDao.saveEvent(event);
            }
        }
        index(resource, false);
        Reference redirect = new Reference(getRequest().getResourceRef().toString() + resource.getId());
        getResponse().setRedirectRef(redirect);
        getResponse().setStatus(Status.SUCCESS_CREATED);
    }


    @Override
    public boolean allowDelete() {
        return false;
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }
}