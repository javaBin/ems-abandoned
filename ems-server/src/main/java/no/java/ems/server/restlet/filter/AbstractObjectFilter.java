package no.java.ems.server.restlet.filter;

import no.java.ems.server.restlet.AuthenticationFilter;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Finder;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractObjectFilter<T> extends Filter {

    private Class<T> objectType;

    public AbstractObjectFilter(Context context, Class<? extends Resource> x) {
        super(context, new Finder(context, x));

        objectType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    // -----------------------------------------------------------------------
    // Abstract Methods
    // -----------------------------------------------------------------------

    protected abstract boolean allowAccess(String id);

    protected abstract boolean allowAccess(T t);

    // -----------------------------------------------------------------------
    // Filter Implementation
    // -----------------------------------------------------------------------

    protected void afterHandle(Request request, Response response) {
        Representation representation = response.getEntity();

        if (representation == null || representation.getMediaType() != MediaType.APPLICATION_JAVA_OBJECT) {
            return;
        }

        try {
            if (!(representation instanceof ObjectRepresentation)) {
                return;
            }

            ObjectRepresentation objectRepresentation = (ObjectRepresentation) representation;

            Object o = objectRepresentation.getObject();

            if (o instanceof ArrayList) {
                response.setEntity(new ObjectRepresentation(accessCheckList(request, (ArrayList<T>) o)));
            } else if (objectType.isAssignableFrom(o.getClass()) && !allowAccess(objectType.cast(o))) {
                // TODO: log UNAUTHORIZED

                response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while filtering list.", e);
        }
    }

    private ArrayList<?> accessCheckList(Request request, ArrayList<T> list) {

        if (AuthenticationFilter.isAuthenticated(request) || list.size() == 0) {
            return list;
        }

        ArrayList<Object> filteredList = new ArrayList<Object>(list.size());

        for (Object o : list) {
            if (o instanceof String) {
                String s = (String) o;

                if (allowAccess(s)) {
                    filteredList.add(s);
                }

                continue;
            }

            if (!objectType.isAssignableFrom(o.getClass())) {
                throw new RuntimeException("Unsupported object type: " + o.getClass().getName() + ". " +
                    "This filter can only handle objects of type " + objectType.getName() + ".");
            }

            if (allowAccess(objectType.cast(o))) {
                filteredList.add(o);
            }
        }

        return filteredList;
    }
}
