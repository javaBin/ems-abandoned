package no.java.ems.client.xhtml;

/**
* @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: $
*/
public abstract class InputElement<T> {
    private String name;
    protected T value;

    public InputElement(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public abstract InputElement<T> copy();
}
