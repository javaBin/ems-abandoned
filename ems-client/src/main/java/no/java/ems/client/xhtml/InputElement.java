package no.java.ems.client.xhtml;

/**
* @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
* @version $Revision: $
*/
public class InputElement<T> {
    private T value;

    public InputElement(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
