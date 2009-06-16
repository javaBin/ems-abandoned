package no.java.ems.dao.impl;

import no.java.ems.dao.BinaryDao;
import no.java.ems.domain.Binary;
import no.java.ems.domain.UriBinary;
import no.java.ems.server.EmsServices;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;


@SuppressWarnings({"unchecked"})
public class FileBinaryDao implements BinaryDao {

    private Log log = LogFactory.getLog(getClass());

    private File binaryStorageDirectory;

    public FileBinaryDao(File binaryStorageDirectory) {
        this.binaryStorageDirectory = binaryStorageDirectory;
    }

    public Binary getBinary(final String id) {
        try {
            File infoFile = new File(binaryStorageDirectory, id + ".info");
            if(!infoFile.canRead()){
                return null;
            }

            List<String> lines = FileUtils.readLines(infoFile, "UTF-8");
            String fileName = lines.get(0);
            String mimeType = lines.get(1);
            URI binaryURI = URI.create(EmsServices.getBinaryUri().toString() + id);
            File file = new File(binaryStorageDirectory, id);
            if (!file.exists()) {
                return null;
            }
            return new UriBinary(id, fileName, mimeType, file.length(), binaryURI);

        } catch (IOException e) {
            //todo: binary failed to be read... ?? What should happen?
            e.printStackTrace();
        }
        return null;
    }

    public File getBinaryAsFile(final String id) {
        return new File(binaryStorageDirectory, id);
    }

    public boolean deleteBinary(final String id) {
        File file = new File(binaryStorageDirectory, id);
        if (file.exists() && file.canWrite()) {
            File infoFile = new File(file.getAbsolutePath() + ".info");
            file.delete();
            infoFile.delete();
            return true;
        }
        return false;
    }

    public Binary createBinary(InputStream inputStream, String filename, String mimeType) {
        String id = UUID.randomUUID().toString();
        OutputStream out = null;
        File file = new File(binaryStorageDirectory, id);
        try {
            FileUtils.writeStringToFile(new File(binaryStorageDirectory, id + ".info"), String.format("%s\n%s", filename, mimeType), "UTF-8");
            IOUtils.copy(inputStream, out = new FileOutputStream(file));
        } catch (IOException e) {
            throw new RuntimeException("binary could not be saved", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(out);
        }
        URI uri = URI.create(EmsServices.getBinaryUri().toString() + id);
        return new UriBinary(id, filename, mimeType, file.length(), uri);
    }
}
