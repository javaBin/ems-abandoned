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

package no.java.ems.server.resources.v2;

import no.java.ems.dao.BinaryDao;
import no.java.ems.server.domain.Binary;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Context;
import java.io.InputStream;
import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Id $
 */
@Path("binaries")
@Component
public class BinaryResource {

    private BinaryDao binaryDao;

    @Autowired
    public BinaryResource(final BinaryDao binaryDao) {
        this.binaryDao = binaryDao;
    }

    @GET
    @Path("{binaryId}")
    public Response getBinary(@PathParam("binaryId") String binaryId) {
        Binary binary = binaryDao.getBinary(binaryId);
        Response.ResponseBuilder builder = Response.ok(binary.getDataStream(), MediaType.valueOf(binary.getMimeType()));
        builder.header("Content-Disposition", "inline; filename=" + binary.getFileName());
        return builder.build();
    }

    @POST
    public Response addBinary(@Context HttpHeaders headers,
                              @HeaderParam("Content-Disposition") String dispositionHeader,
                              InputStream stream) {
        String filename = ResourcesF.getFileName(dispositionHeader);
        if (StringUtils.isBlank(filename)) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        MediaType type = headers.getMediaType();
        Binary binary = binaryDao.createBinary(stream, filename, type.toString());
        return Response.created(URI.create(binary.getId())).build();
    }       

    @DELETE
    @Path("{binaryId}")
    public Response deleteBinary(@PathParam("binaryId") String binaryId) {
        boolean deleted = binaryDao.deleteBinary(binaryId);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
