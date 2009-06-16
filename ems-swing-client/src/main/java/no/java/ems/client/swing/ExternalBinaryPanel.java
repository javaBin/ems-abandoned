package no.java.ems.client.swing;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import no.java.ems.domain.URIBinary;
import no.java.swing.DefaultPanel;
import no.java.swing.SwingHelper;
import org.jdesktop.beansbinding.*;

import javax.swing.*;
import java.net.URI;

public class ExternalBinaryPanel extends DefaultPanel {

    private MutableURIBinary binary;
    private JTextField URIField;
    private JTextField filenameField;
    private JTextField mediaTypeField;
    private BindingGroup bindingGroup;

    public ExternalBinaryPanel() {
        super("no.java.ems.client.swing.ExternalBinaryPanel.DetailsPanel");
        bindingGroup = new BindingGroup();
        SwingHelper.initialize(this);
    }

    public void initModels() {
        binary = new MutableURIBinary();
    }

    public void initActions() {
    }

    public void initComponents() {
        URIField = new JTextField(20);
        filenameField = new JTextField(10);
        mediaTypeField = new JTextField(10);
    }

    public void initBindings() {
        AutoBinding<MutableURIBinary, URI, JTextField, String> uriBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, binary, BeanProperty.<MutableURIBinary, URI>create("uri"), URIField, BeanProperty.<JTextField, String>create("text"));
        uriBinding.setConverter(new URIConverter());
        bindingGroup.addBinding(uriBinding);
        bindingGroup.addBinding(Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, binary, BeanProperty.<MutableURIBinary, String>create("filename"), filenameField, BeanProperty.create("text")));
        bindingGroup.addBinding(Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, binary, BeanProperty.<MutableURIBinary, String>create("mediaType"), mediaTypeField, BeanProperty.create("text")));
    }

    public void initListeners() {
    }

    public void initLayout() {
        setLayout(new FormLayout("p, 5dlu, p", "p, 3dlu, p, 3dlu, p, 3dlu, p"));
        CellConstraints cc = new CellConstraints();
        add(SwingHelper.createSAFLabel("no.java.ems.client.swing.ExternalBinaryPanel.DetailsPanel.uriLabel", URIField), cc.xy(1, 1));
        add(URIField, cc.xy(3, 1));
        add(SwingHelper.createSAFLabel("no.java.ems.client.swing.ExternalBinaryPanel.DetailsPanel.filenameLabel", filenameField), cc.xy(1, 3));
        add(filenameField, cc.xy(3, 3));
        add(SwingHelper.createSAFLabel("no.java.ems.client.swing.ExternalBinaryPanel.DetailsPanel.mediaTypeLabel", mediaTypeField), cc.xy(1, 5));
        add(mediaTypeField, cc.xy(3, 5));
    }

    public void initState() {
        bindingGroup.bind();
    }

    URIBinary write() {
        for (Binding binding : bindingGroup.getBindings()) {
            Binding.SyncFailure failure = binding.save();
            if (failure != null) {
                throw new IllegalStateException(failure.toString());
            }
        }
        return binary.toUriBinary();
    }

    private static class URIConverter extends Converter<URI, String> {
        public String convertForward(URI value) {
            if (value != null) {
                return value.toString();
            }
            return "";
        }

        public URI convertReverse(String value) {
            if (value == null) {
                return null;
            }
            return URI.create(value);
        }
    }

    public static class MutableURIBinary {
        private URI uri;
        private String mediaType;
        private String filename;

        public URI getUri() {
            return uri;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public URIBinary toUriBinary() {
            return new URIBinary(filename, mediaType, -1, uri);
        }
    }

}
