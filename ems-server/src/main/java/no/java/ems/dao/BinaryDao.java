
package no.java.ems.dao;

import no.java.ems.domain.Binary;

import java.io.InputStream;
import java.io.File;

public interface BinaryDao {
    
    /**
     * Gets a binary for an entity
     * 
     * @param id the id of the binary
     * @return a binary if found, null if not.
     */
    Binary getBinary(String id);

    File getBinaryAsFile(String id);

    /**
     *
     * @param inputStream
     * @param filename
     * @param mimetype
     * @return
     */
    Binary createBinary(InputStream inputStream, String filename, String mimetype);

    /**
     * 
     * @param id
     */
    boolean deleteBinary(String id);
}