/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package no.java.ems.dao.impl;

import no.java.ems.dao.BinaryDao;
import no.java.ems.server.domain.Binary;
import no.java.ems.server.domain.UriBinary;
import no.java.ems.server.domain.EmsServerConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unchecked"})
@Component
public class FileBinaryDao implements BinaryDao {

    private File binaryStorageDirectory;

    @Autowired
    public FileBinaryDao(EmsServerConfiguration configuration) {
        this.binaryStorageDirectory = configuration.getBinaryStorageDirectory();
    }

    public Binary getBinary(final String id) {
        try {
            File infoFile = new File(binaryStorageDirectory, id + ".info");
            if (!infoFile.canRead()) {
                return null;
            }

            List<String> lines = FileUtils.readLines(infoFile, "UTF-8");
            String fileName = lines.get(0);
            String mimeType = lines.get(1);
            File file = new File(binaryStorageDirectory, id);
            if (!file.exists()) {
                return null;
            }
            return new UriBinary(id, fileName, mimeType, file.length(), file.toURI());

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
        return new UriBinary(id, filename, mimeType, file.length(), file.toURI());
    }
}
