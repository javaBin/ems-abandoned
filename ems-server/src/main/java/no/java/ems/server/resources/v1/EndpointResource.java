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

package no.java.ems.server.resources.v1;

import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.GET;

import no.java.ems.external.v1.MIMETypes;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
@Path("1")
@Component
//TODO: Use JaxB for instead of the JSONObject.
public class EndpointResource {
    
    private UriInfo uriInfo;

    @Produces(MIMETypes.ENDPOINT_MIME_TYPE)
    @GET
    public Response get() {
        JSONObject object = new JSONObject();
        try {
            object.put("events", createMapping("/1/events"));
            object.put("people", createMapping("/1/people"));
            object.put("rooms", createMapping("/1/rooms"));
            object.put("binaries", createMapping("/binaries"));
        } catch (JSONException e) {
            throw new WebApplicationException(500);
        }
        return Response.ok(object.toString()).build();
    }

    private JSONObject createMapping(String path) throws JSONException {
        JSONObject object = new JSONObject();
        String uri = uriInfo.getBaseUriBuilder().path(path).build().toString();
        object.put("uri", uri);
        return object;
    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
}
