package no.java.ems.client.xhtml;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: $
*/
public class Options extends InputElement<List<String>> {
    private final Map<String, String> options;

    public Options(String name, Map<String, String> options, List<String> selections) {
        super(name, selections);
        this.options = options;
    }

    public void addSelection(String value) {
        if (!options.containsKey(value)) {
            throw new IllegalArgumentException("Selection is not in value map");
        }
        this.value.add(value);
    }

    public void removeSelection(String value) {
        if (!options.containsKey(value)) {
            throw new IllegalArgumentException("Selection is not in value map");
        }
        this.value.remove(value);
    }

    @Override
    public InputElement<List<String>> copy() {
        return new Options(getName(), getOptions(), getValue());
    }

    public Map<String, String> getOptions() {
        return Collections.unmodifiableMap(options);
    }
}
