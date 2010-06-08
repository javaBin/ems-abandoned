package no.java.ems.client.xhtml;

/**
* @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: $
*/
public class TextElement extends InputElement<String> {
    public TextElement(String name, String value) {
        super(name, value);
    }

    @Override
    public InputElement<String> copy() {
        return new TextElement(getName(), getValue());
    }
}
