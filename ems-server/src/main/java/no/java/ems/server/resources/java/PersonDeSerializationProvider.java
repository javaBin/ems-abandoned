package no.java.ems.server.resources.java;

import no.java.ems.domain.Person;
import no.java.ems.domain.Session;
import no.java.ems.server.resources.ResourceUtil;
import org.apache.commons.lang.SerializationUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
@Provider
@Component
public class PersonDeSerializationProvider implements MessageBodyReader<Person> {
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Person.class == type && mediaType.isCompatible(ResourceUtil.APPLICATION_X_JAVA_SERIALIZED_OBJECT);
    }

    public Person readFrom(Class<Person> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return type.cast(SerializationUtils.deserialize(entityStream));
    }
}