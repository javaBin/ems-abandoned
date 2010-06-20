
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

package no.java.ems.dao;

import fj.data.*;
import no.java.ems.server.domain.Binary;

import java.io.InputStream;
import java.io.File;

public interface BinaryDao {
    
    /**
     * Gets a binary for an entity
     * 
     * @param id the id of the binary
     * @return a binary if found, null if not.
     */
    Either<String, Binary> getBinary(String id);

    File getBinaryAsFile(String id);

    /**
     *
     * @param inputStream
     * @param filename
     * @param mimetype
     * @return
     */
    Binary createBinary(InputStream inputStream, String filename, String mimetype);

    boolean deleteBinary(String id);
}
