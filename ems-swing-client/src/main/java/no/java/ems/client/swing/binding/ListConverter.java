package no.java.ems.client.swing.binding;

import org.jdesktop.beansbinding.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public abstract class ListConverter<T> extends Converter<List<T>, String> {

    private static final String DELIMITER = "\\s*[,;]\\s*";

    public String convertForward(final List<T> elements) {
        StringBuilder builder = new StringBuilder();
        for (T element : elements) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(toString(element));
        }
        return builder.toString();
    }

    public List<T> convertReverse(final String elementsText) {
        ArrayList<T> elements = new ArrayList<T>();
        for (String elementString : elementsText.split(DELIMITER)) {
            T element = fromString(elementString);
            if (element != null && !elements.contains(element)) {
                elements.add(element);
            }
        }
        return elements;
    }

    protected abstract T fromString(final String text);

    protected abstract String toString(final T element);

    public static class StringListConverter extends ListConverter<String> {

        protected String fromString(final String text) {
            return text != null && !text.isEmpty() ? text : null;
        }

        protected String toString(final String element) {
            return element;
        }

    }

}
