package no.java.ems.client.xhtml;

import org.codehaus.httpcache4j.HTTPMethod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
* @version $Revision: $
*/
public class Form {
    private final HTTPMethod method;
    private final Map<String, InputElement<?>> form = new LinkedHashMap<String, InputElement<?>>();

    public Form() {
        this(HTTPMethod.GET);
    }

    public Form(HTTPMethod method) {
        this.method = method;
    }

    public void add(String name, InputElement element) {
        form.put(name, element);
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public Map<String, InputElement<?>> getForm() {
        return Collections.unmodifiableMap(form);
    }

    public TextElement getTextElement(String name) {
        InputElement<?> form = this.form.get(name);
        if (form instanceof TextElement) {
            return (TextElement) form;
        }
        return null;
    }

    public Options getOptions(String name) {
        InputElement<?> form = this.form.get(name);
        if (form instanceof Options) {
            return (Options) form;
        }
        return null;
    }
}
