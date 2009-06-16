package no.java.ems.client.swing.binding;

import org.jdesktop.beansbinding.Converter;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: #5 $ $Date: 2008/09/15 $
*/
public class URIConverter extends Converter<URI, String> {
    @Override
        public String convertForward(URI uri) {
        return uri != null ? uri.toString() : null;
    }

    @Override
        public URI convertReverse(String s) {
        return s != null ? URI.create(s) : null;
    }
}
