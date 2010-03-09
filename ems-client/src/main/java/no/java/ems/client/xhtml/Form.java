package no.java.ems.client.xhtml;

import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.HTTPMethod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: $
*/
public class Form {
    private final String id;
    private final HTTPMethod method;
    private final Map<String, InputElement<?>> form = new LinkedHashMap<String, InputElement<?>>();

    public Form(String id) {
        this(id, HTTPMethod.GET);
    }

    public Form(String id, HTTPMethod method) {
        Validate.notNull(id, "Id may not be null");
        Validate.notNull(method, "Method may not be null");
        this.id = id;
        this.method = method;
    }

    public void add(String name, InputElement element) {
        form.put(name, element);
    }

    public String getId() {
        return id;
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
