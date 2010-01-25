package no.java.ems.server.resources;

import org.springframework.stereotype.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;
import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Provider
@Component
public class ArrayListProvider implements MessageBodyWriter<Object> {

    public long getSize(final Object t, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(final Class<?> type, final Type genericType,
                               final Annotation[] annotations, final MediaType mediaType) {
        return mediaType.isCompatible(ResourceUtil.APPLICATION_X_JAVA_SERIALIZED_OBJECT);
    }

    public void writeTo(final Object t, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        ObjectOutputStream out = new ObjectOutputStream(entityStream);
        out.writeObject(t);
        out.flush();
        out.close();
    }
}
