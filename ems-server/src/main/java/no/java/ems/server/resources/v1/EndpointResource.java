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

import no.java.ems.server.URIBuilder;
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

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
@Path("2")
@Component
//TODO: Use JaxB for instead of the JSONObject.
public class EndpointResource {
    
    @Produces({"application/json", MIMETypes.ENDPOINT_MIME_TYPE})
    @GET
    public Response get(@Context UriInfo info) {
        URIBuilder uriBuilder = new URIBuilder(info.getBaseUriBuilder());
        JSONObject object = new JSONObject();
        try {
            object.put("events", createMapping(uriBuilder.events().getURI()));
            object.put("people", createMapping(uriBuilder.people().people()));
            object.put("rooms", createMapping(uriBuilder.rooms().rooms()));
            object.put("binaries", createMapping(uriBuilder.binaries().binaries()));
            object.put("search", createMapping(uriBuilder.search().form()));
        } catch (JSONException e) {
            throw new WebApplicationException(500);
        }
        return Response.ok(object.toString()).build();
    }

    private JSONObject createMapping(URI uri) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("uri", uri.toString());
        return object;
    }
}
