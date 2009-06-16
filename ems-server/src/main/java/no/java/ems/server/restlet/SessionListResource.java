package no.java.ems.server.restlet;

import no.java.ems.dao.SessionDao;
import no.java.ems.domain.Session;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.util.ArrayList;

public class SessionListResource extends GenericListResource {

    private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyyMMdd");
    private SessionDao sessionDao;

    public SessionListResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        sessionDao = (SessionDao)context.getAttributes().get("sessionDao");
    }

    public Representation getRepresentation(Variant variant) {
        ArrayList resource;
        String eid = (String)getRequest().getAttributes().get("eid");
        String type = (String)getRequest().getAttributes().get("type");
        String param = (String)getRequest().getAttributes().get("param");
        if (isBlank(type)) {
            resource = new ArrayList<Session>(sessionDao.getSessions(eid));
        } else {
            if ("date".equals(type)) {
                if (isBlank(param)) {
                    throw new RuntimeException("Illegal request: missing date parameter");
                }
                LocalDate date = dateFormatter.parseDateTime(param).toLocalDate();
                resource = new ArrayList<String>(sessionDao.findSessionsByDate(eid, date));
            } else if ("title".equals(type)) {
                if (isBlank(param)) {
                    throw new RuntimeException("Illegal request: missing title parameter");
                }
                resource = new ArrayList<String>(sessionDao.findSessionsByTitle(eid, urlDecode(param)));
            } else if ("speaker-name".equals(type)) {
                if (isBlank(param)) {
                    throw new RuntimeException("Illegal request: missing speaker-name parameter");
                }
                resource = new ArrayList<String>(sessionDao.findSessionsBySpeakerName(eid, urlDecode(param)));
            } else {
                throw new RuntimeException("Unknown session projection: " + type);
            }
        }
        if (variant.getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT)) {
            return new ObjectRepresentation(resource);
        } else if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            return new StringRepresentation(ToStringBuilder.reflectionToString(resource, ToStringStyle.MULTI_LINE_STYLE));
        }
        return super.getRepresentation(variant);
    }

    public boolean allowGet() {
        return true;
    }
}
