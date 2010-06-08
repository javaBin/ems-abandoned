package no.java.ems.client.xhtml;

import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.HTTPMethod;
import org.codehaus.httpcache4j.payload.FormDataPayload;
import org.codehaus.httpcache4j.util.URIBuilder;

import java.net.URI;
import java.util.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class Form implements Iterable<InputElement<?>> {
    private final String id;
    private final HTTPMethod method;
    private final URI action;
    private final Map<String, InputElement<?>> form = new LinkedHashMap<String, InputElement<?>>();

    public Form(String id, URI action) {
        this(id, HTTPMethod.GET, action);
    }

    public Form(String id, HTTPMethod method, URI action) {
        Validate.notNull(id, "Id may not be null");
        Validate.notNull(method, "Method may not be null");
        Validate.notNull(action, "Action may not be null");
        this.id = id;
        this.method = method;
        this.action = action;
    }

    public void add(InputElement element) {
        form.put(element.getName(), element);
    }

    public String getId() {
        return id;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public URI getAction() {
        return action;
    }

    public Map<String, InputElement<?>> getForm() {
        return Collections.unmodifiableMap(form);
    }

    public Form copy() {
        Form form = new Form(id, method, action);
        for (InputElement<?> element : form) {
            form.add(element.copy());
        }
        return form;
    }

    public URI constructURI() {
        URIBuilder builder = URIBuilder.fromURI(getAction());
        for (InputElement element : this) {
            if (element instanceof TextElement) {
                TextElement e = (TextElement) element;
                builder = builder.addParameter(element.getName(), e.getValue());
            }
            else if (element instanceof Options) {
                Options o = (Options) element;
                List<String> selections = o.getValue();
                for (String selection : selections) {
                    builder = builder.addParameter(o.getName(), selection);
                }
            }
        }
        return builder.toURI();
    }

    public FormDataPayload toPayload() {
        List<FormDataPayload.FormParameter> params = new ArrayList<FormDataPayload.FormParameter>();
        for (InputElement element : this) {
            if (element instanceof TextElement) {
                TextElement e = (TextElement) element;
                params.add(new FormDataPayload.FormParameter(element.getName(), e.getValue()));
            }
            else if (element instanceof Options) {
                Options o = (Options) element;
                List<String> selections = o.getValue();
                for (String selection : selections) {
                    params.add(new FormDataPayload.FormParameter(element.getName(), selection));
                }
            }
        }
        return new FormDataPayload(params);
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

    public Iterator<InputElement<?>> iterator() {
        return getForm().values().iterator();
    }
}
