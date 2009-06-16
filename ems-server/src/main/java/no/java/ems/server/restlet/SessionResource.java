package no.java.ems.server.restlet;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import no.java.ems.dao.SessionDao;
import no.java.ems.domain.Session;
import no.java.ems.wiki.DefaultHtmlWikiSink;
import no.java.ems.wiki.DefaultWikiEngine;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionResource extends GenericResource<Session> {

    private SessionDao sessionDao;

    public SessionResource(Context context, Request request, Response response) {
        super(context, request, response, "sid");
        getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        sessionDao = EmsApplication.getService(context, SessionDao.class);
    }

    public Representation getRepresentation(Variant variant) {
        Serializable resource;
        if (StringUtils.isBlank(getId()) && onlyIds()) {
            resource = new ArrayList<String>(sessionDao.getSessionIdsByEventId(getEventId()));
        }
        else if (StringUtils.isBlank(getId())) {
            resource = new ArrayList<Session>(sessionDao.getSessions(getEventId()));
        } else {
            resource = sessionDao.getSession(getId());
        }

        if (variant.getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT)) {
            return new ObjectRepresentation(resource);
        } else if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            if (resource instanceof Session) {
                return new StringRepresentation(ToStringBuilder.reflectionToString(resource, ToStringStyle.MULTI_LINE_STYLE));
            }

            //noinspection unchecked
            List<Session> sessions = (List<Session>) resource;
            StringBuilder string = new StringBuilder();
            string.append("Showing ").append(sessions.size()).append(" sessions.").append(EOL);
            string.append(EOL);
            for (Session session : sessions) {
                string.append(session.getId()).append(": ").append(session.getTitle()).append(EOL);
            }
            return new StringRepresentation(string);
        }
        else if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            Configuration cfg = new Configuration();
            cfg.setClassForTemplateLoading(SessionResource.class, "/freemarker");
            cfg.setObjectWrapper(new DefaultObjectWrapper());

            Map<String, Object> rootMap = new HashMap<String, Object>();
            String templateName = null;
            if (!StringUtils.isBlank(getId()) && resource instanceof Session) {
                Session session = (Session)resource;
                rootMap.put("session", resource);
                String bodyText = session.getBody();
                if (!StringUtils.isBlank(bodyText)) {
                    try {
                        DefaultHtmlWikiSink sink = new DefaultHtmlWikiSink();
                        new DefaultWikiEngine<DefaultHtmlWikiSink>(sink).transform(session.getBody());
                        bodyText = sink.toString();
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
                rootMap.put("bodyText", bodyText);
                templateName = "session.ftl";
            } else {
                rootMap.put("sessions", resource);
                templateName = "sessions.ftl";
            }

            return new TemplateRepresentation(templateName, cfg, rootMap, variant.getMediaType());
        }

        return super.getRepresentation(variant);
    }

    private boolean onlyIds() {
        return Boolean.TRUE.equals(getRequest().getAttributes().get("onlyid"));
    }

    private String getEventId() {
        return (String) getRequest().getAttributes().get("eid");
    }

    @Override
    public void put(Representation entity) {
        Session session = extractObject(entity);
        Validate.notNull(session.getId());
        Validate.isTrue(getId().equals(session.getId()));
        sessionDao.saveSession(session);
        index(session, false);
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    public void post(Representation entity) {
        Session session = extractObject(entity);
        Validate.isTrue(session.getId() == null);
        Validate.isTrue(session.getRevision() == 0);
        sessionDao.saveSession(session);
        index(session, false);
        //index(session, false);
        Reference redirect = new Reference(getRequest().getResourceRef().toString() + session.getId());
        getResponse().setRedirectRef(redirect);
        getResponse().setStatus(Status.SUCCESS_CREATED);
    }

    public void delete() {
        final Session session = sessionDao.getSession(getId());
        if (session != null) {
            sessionDao.deleteSession(getId());
            index(session, true);
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    public boolean allowGet() {
        return true;
    }

    public boolean allowPost() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }
}
