package no.java.ems.client.xhtml;

import org.codehaus.httpcache4j.HTTPMethod;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class XHTMLFormParser {
    private final URI defaultURI;
    private final InputStream payload;
    private final static XMLInputFactory factory = XMLInputFactory.newInstance();

    public XHTMLFormParser(URI uri, InputStream payload) {
        defaultURI = uri;
        this.payload = payload;
    }

    public List<Form> parse() throws XMLStreamException {
        XMLEventReader reader = factory.createXMLEventReader(payload);
        List<Form> forms = new ArrayList<Form>();
        try {
            while(reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    StartElement start = event.asStartElement();
                    if ("form".equals(start.getName().getLocalPart())) {
                        forms.add(parseForm(start, reader));
                    }
                }
            }
        } finally {
            reader.close();            
        }
        if (forms.isEmpty()) {
            throw new XMLStreamException("Payload did not contain a form, unable to parse");
        }
        return Collections.unmodifiableList(forms);
    }

    private Form parseForm(StartElement formElement, XMLEventReader reader) throws XMLStreamException {
        Attribute method = formElement.getAttributeByName(new QName("method"));
        Attribute action = formElement.getAttributeByName(new QName("action"));
        URI uri = defaultURI;
        if (action != null) {
            uri = URI.create(action.getValue());
        }
        Form form;
        if (method == null) {
            form = new Form("search", uri);
        }
        else {
            String methodValue = method.getValue().toUpperCase();
            form = new Form(findId(formElement), HTTPMethod.valueOf(methodValue), uri);
        }
        while(reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String name = findId(element);
                if ("input".equals(element.getName().getLocalPart())) {
                    if (name != null) {
                        Attribute typeAttribute = element.getAttributeByName(new QName("type"));
                        if ("text".equals(typeAttribute.getValue()) || "search".equals(typeAttribute.getValue())) {
                            form.add(new TextElement(name, reader.getElementText()));
                        }
                    }
                }
                else if ("select".equals(element.getName().getLocalPart())) {
                    if (name != null) {
                        form.add(parseSelect(name, reader));
                    }                    
                }
            }
        }
        return form;
    }

    private String findId(StartElement element) {
        String name = null;
        Attribute nameAttribute = element.getAttributeByName(new QName("name"));
        if (nameAttribute != null) {
            name = nameAttribute.getValue();
        }
        else {
            nameAttribute = element.getAttributeByName(new QName("id"));
            if (nameAttribute != null) {
                name = nameAttribute.getValue();
            }
        }
        return name;
    }

    private Options parseSelect(String name, XMLEventReader reader) throws XMLStreamException {
        Map<String, String> options = new LinkedHashMap<String, String>();
        List<String> selected = new ArrayList<String>();
        while(reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                if ("option".equals(element.getName().getLocalPart())) {
                    Attribute valueAttribute = element.getAttributeByName(new QName("value"));
                    options.put(valueAttribute.getValue(), reader.getElementText());
                    Attribute selectedAttrbute = element.getAttributeByName(new QName("selected"));
                    if ("selected".equals(selectedAttrbute.getValue())) {
                        selected.add(valueAttribute.getValue());
                    }
                }
            }
            else if (event.isEndElement()) {
                EndElement end = event.asEndElement();
                if ("select".equals(end.getName().getLocalPart())) {
                    break;
                }
            }
        }
        return new Options(name, options, selected);
    }
}
