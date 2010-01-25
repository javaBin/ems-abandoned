package no.java.ems.server.resources;

import javax.ws.rs.core.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ResourceUtil {
    public static final String APPLICATION_X_JAVA_SERIALIZED_OBJECT_STRING = "application/x-java-serialized-object";
    public static final MediaType APPLICATION_X_JAVA_SERIALIZED_OBJECT = MediaType.valueOf(APPLICATION_X_JAVA_SERIALIZED_OBJECT_STRING);
}
