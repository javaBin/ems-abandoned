package no.java.ems.cli;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import no.java.ems.domain.Session;
import no.java.ems.domain.Speaker;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsXStream extends XStream {
    public EmsXStream() {
        super(new PureJavaReflectionProvider(), new StaxDriver());
        alias("event-data", EventData.class);
        alias("session", Session.class);
        alias("speaker", Speaker.class);
    }
}
