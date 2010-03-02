package no.java.ems.client.xhtml;

import no.java.ems.client.xhtml.Form;
import no.java.ems.client.xhtml.Options;
import no.java.ems.client.xhtml.TextElement;
import org.codehaus.httpcache4j.HTTPMethod;
import org.codehaus.httpcache4j.payload.Payload;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class XHTMLFormParser {
    private final Payload payload;
    private final static XMLInputFactory factory = XMLInputFactory.newInstance();

    public XHTMLFormParser(Payload payload) {
        this.payload = payload;
    }

    public Form parse() throws XMLStreamException {
        InputStream stream = payload.getInputStream();
        XMLEventReader reader = factory.createXMLEventReader(stream);
        try {
            while(reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    StartElement start = event.asStartElement();
                    if ("form".equals(start.getName().getLocalPart())) {
                        return parseForm(start, reader);
                    }
                }
            }
        } finally {
            reader.close();            
        }
        throw new XMLStreamException("Payload did not contain a form, unable to parse");
    }

    private Form parseForm(StartElement formElement, XMLEventReader reader) throws XMLStreamException {
        Attribute method = formElement.getAttributeByName(new QName("method"));
        Form form;
        if (method == null) {
            form = new Form();
        }
        else {
            String methodValue = method.getValue().toUpperCase();
            form = new Form(HTTPMethod.valueOf(methodValue));
        }
        while(reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
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
                if ("input".equals(element.getName().getLocalPart())) {
                    if (name != null) {
                        Attribute typeAttribute = element.getAttributeByName(new QName("type"));
                        if ("text".equals(typeAttribute.getValue())) {
                            form.add(name, new TextElement(reader.getElementText()));
                        }
                    }
                }
                else if ("select".equals(element.getName().getLocalPart())) {
                    if (name != null) {
                        form.add(name, parseSelect(reader));
                    }                    
                }
            }
        }
        return form;
    }

    private Options parseSelect(XMLEventReader reader) throws XMLStreamException {
        Map<String, String> options = new LinkedHashMap<String, String>();
        while(reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                if ("option".equals(element.getName().getLocalPart())) {
                    Attribute valueAttribute = element.getAttributeByName(new QName("value"));
                    options.put(valueAttribute.getValue(), reader.getElementText());
                }
            }
            else if (event.isEndElement()) {
                EndElement end = event.asEndElement();
                if ("select".equals(end.getName().getLocalPart())) {
                    break;
                }
            }
        }
        return new Options(options);
    }
}
