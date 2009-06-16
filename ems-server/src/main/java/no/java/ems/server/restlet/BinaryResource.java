package no.java.ems.server.restlet;

import no.java.ems.dao.BinaryDao;
import no.java.ems.domain.Binary;
import no.java.ems.server.EmsServices;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.*;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class BinaryResource extends Resource {

    private BinaryDao binaryDao;

    public BinaryResource(Context context, Request request, Response response) {
        super(context, request, response);
        binaryDao = (BinaryDao) context.getAttributes().get("binaryDao");

        getVariants().add(new Variant(MediaType.ALL));
        getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
    }

    public Representation getRepresentation(Variant variant) {
        Binary binary = binaryDao.getBinary(getBinaryId());
        if (binary == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        if (variant.getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT, true)) {
            return new ObjectRepresentation(binary);
        } else {
            return new FileRepresentation(binaryDao.getBinaryAsFile(binary.getId()), MediaType.IMAGE_JPEG, 1000);
        }
    }

    private String getBinaryId() {
        return (String)getRequest().getAttributes().get("bid");
    }

    public void delete() {
        if (binaryDao.deleteBinary(getBinaryId())) {
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    public void post(Representation entity) {
        try {
            Form form = (Form)getRequest().getAttributes().get("org.restlet.http.headers");
            String fileName = form.getValues("X-binary-fileName");
            String mimeType = form.getValues("X-binary-mimeType");
            Binary binary = binaryDao.createBinary(entity.getStream(), fileName, mimeType);
            Reference redirect = new Reference(EmsServices.getBinaryUri().toString() + binary.getId());
            getResponse().setRedirectRef(redirect);
            getResponse().setStatus(Status.SUCCESS_CREATED);
        } catch (RuntimeException ex) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            getLogger().log(Level.WARNING, "Unable to post binary", ex);
        } catch (IOException e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            getLogger().log(Level.WARNING, "Unable to post binary", e);
        }
    }

    public boolean allowPost() {
        return true;
    }

    public boolean allowGet() {
        return true;
    }

    public boolean allowDelete() {
        return true;
    }
}
