package no.java.ems.client.swing;

import no.java.ems.domain.Binary;
import no.java.swing.ApplicationTask;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class SaveBinariesTask extends ApplicationTask<Void, Void> {

    private final List<Map.Entry<Binary, File>> binaryAndFileList;

    public SaveBinariesTask(final List<Binary> binaries, final File folder) {
        super("no.java.ems.client.swing.SaveBinariesTask");
        Validate.notNull(binaries, "Binaries may not be null");
        Validate.noNullElements(binaries, "Binaries may not contain any null elements");
        Validate.notNull(folder, "Folder may not be null");
        Validate.isTrue(folder.isDirectory(), "Invalid target folder: " + folder.getAbsolutePath());
        binaryAndFileList = new ArrayList<Map.Entry<Binary, File>>(binaries.size());
        for (Binary binary : binaries) {
            File targetFile = getTargetFile(folder, binary);
            if (targetFile.exists()) {
                // todo: ask for confirmation
                System.err.println("skipped file (target already exists): " + targetFile.getAbsolutePath());
            } else {
                binaryAndFileList.add(new AbstractMap.SimpleEntry<Binary, File>(binary, targetFile));
            }
        }
    }

    protected Void doInBackground() throws Exception {
        for (int n = 0; n < binaryAndFileList.size(); n++) {
            Map.Entry<Binary, File> binaryAndFile = binaryAndFileList.get(n);
            Binary binary = binaryAndFile.getKey();
            File file = binaryAndFile.getValue();
            setProgress(n + 1, 0, binaryAndFileList.size());
            if (binaryAndFileList.size() == 1) {
                message(getFullResourceKey("progress.singular"), binary.getFileName());
            } else {
                message(getFullResourceKey("progress.plural"), binary.getFileName(), n + 1, binaryAndFileList.size());
            }
            InputStream in = null;
            OutputStream out = null;
            try {
                IOUtils.copy(in = binary.getDataStream(), out = new FileOutputStream(file));
                wroteBinaryToFile(binaryAndFile);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        return null;
    }

    protected void wroteBinaryToFile(final Map.Entry<Binary, File> binaryAndFile) {
    }

    protected File getTargetFile(final File folder, final Binary binary) {
        return new File(folder, binary.getFileName());
    }

    @Override
    protected void succeeded(final Void result) {
        if (binaryAndFileList.size() == 1) {
            setMessage(getString("succeeded.singular", binaryAndFileList.get(0).getKey().getFileName(), getExecutionDuration(TimeUnit.MILLISECONDS)));
        } else {
            setMessage(getString("succeeded.plural", binaryAndFileList.size(), getExecutionDuration(TimeUnit.MILLISECONDS)));
        }
    }

}
