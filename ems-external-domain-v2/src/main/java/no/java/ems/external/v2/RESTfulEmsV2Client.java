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

package no.java.ems.external.v2;

import fj.data.Option;
import static fj.data.Option.none;
import fj.Unit;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.net.URI;

import no.java.ems.client.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class RESTfulEmsV2Client implements EmsV2Client {

    private static final String CONTEXT_PATH = EventV2.class.getPackage().getName();

    private final Map<String, EndpointParser.Endpoint> endpoints = new HashMap<String, EndpointParser.Endpoint>();
    private final RESTfulClient client;
    private Marshaller marshaller;
    private static final MIMEType SESSION = MIMEType.valueOf(MIMETypes.SESSION_MIME_TYPE);
    private static final MIMEType JSON = MIMEType.valueOf(MIMETypes.ENDPOINT_MIME_TYPE);
    private static final MIMEType EVENT_LIST = MIMEType.valueOf(MIMETypes.EVENT_LIST_MIME_TYPE);
    private static final MIMEType SESSION_LIST = MIMEType.valueOf(MIMETypes.SESSION_LIST_MIME_TYPE);
    private static final MIMEType EVENT = MIMEType.valueOf(MIMETypes.EVENT_MIME_TYPE);
    private static final MIMEType PERSON_LIST = MIMEType.valueOf(MIMETypes.PERSON_LIST_MIME_TYPE);
    private static final MIMEType PERSON = MIMEType.valueOf(MIMETypes.PERSON_MIME_TYPE);
    private static final MIMEType ROOM_LIST = MIMEType.valueOf(MIMETypes.ROOM_LIST_MIME_TYPE);
    private static final MIMEType ROOM = MIMEType.valueOf(MIMETypes.ROOM_MIME_TYPE);
    private static final MIMEType ENDPOINT = MIMEType.valueOf(MIMETypes.ENDPOINT_MIME_TYPE);


    public RESTfulEmsV2Client(HTTPCache cache) {
        this(cache, null, null);
    }

    public RESTfulEmsV2Client(HTTPCache cache, String username, String password) {
        try {
            JAXBContext context = JAXBContext.newInstance(CONTEXT_PATH);
            marshaller = context.createMarshaller();
            client = new MyRESTfulClient(cache, context, username, password);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Login. Get the endpoint and populate the endpioint map.
     *
     * @param endpoint the uri of the "2" webservice, e.g http://localhost:3000/ems/2
     */
    public void login(URI endpoint) {
        Option<Resource> endpoints = client.read(new ResourceHandle(endpoint), Collections.singletonList(ENDPOINT));
        if (endpoints.isSome()) {
            Resource resource = endpoints.some();
            Option<Map> data = resource.getData(Map.class);
            if (data.isSome()) {
                Map<String, EndpointParser.Endpoint> map = data.some();
                this.endpoints.putAll(map);                
            }
        }
    }

    private <A> Payload createJAXBPayload(String tag, Class<A> type, A object, MIMEType mimeType) {
        try {
            StringWriter writer = new StringWriter();
            marshaller.marshal(new JAXBElement<A>(new QName(tag), type, object), writer);
            return new InputStreamPayload(IOUtils.toInputStream(writer.toString(), "UTF-8"), mimeType);
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to marshall", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to marshall", e);
        }
    }


    public Option<EventV2> getEvent(ResourceHandle handle) {
        Option<Resource> resourceOption = client.read(handle, Collections.singletonList(EVENT));
        return extractObject(resourceOption, EventV2.class);
    }

    private <T> Option<T> extractObject(Option<Resource> resourceOption, Class<T> type) {
        if (resourceOption.isSome()) {
            Resource resource = resourceOption.some();
            return resource.getData(type);
        }
        return none();
    }

    public Unit updateEvent(ResourceHandle handle, EventV2 event) {
        return client.update(handle, createJAXBPayload("event", EventV2.class, event, EVENT));
    }

    public ResourceHandle addEvent(EventV2 event) {
        return client.create(endpoints.get("events").getHandle(), createJAXBPayload("event", EventV2.class, event, EVENT));
    }

    public EventListV2 getEvents() {
        Option<Resource> resourceOption = client.read(endpoints.get("events").getHandle(), Collections.singletonList(EVENT_LIST));
        return extractObject(resourceOption, EventListV2.class).some();
    }

    public SessionListV2 getSessions(ResourceHandle handle) {
        Option<Resource> resourceOption = client.read(handle, Collections.singletonList(SESSION_LIST));
        return extractObject(resourceOption, SessionListV2.class).some();
    }

    public Option<SessionV2> getSession(ResourceHandle handle) {
        Option<Resource> resourceOption = client.read(handle, Collections.singletonList(SESSION));
        return extractObject(resourceOption, SessionV2.class);
    }

    public ResourceHandle addSession(ResourceHandle handle, SessionV2 session) {
        return client.create(handle, createJAXBPayload("session", SessionV2.class, session, SESSION));
    }

    public Unit updateSession(ResourceHandle handle, SessionV2 session) {
        return client.update(handle, createJAXBPayload("session", SessionV2.class, session, SESSION));
    }

    public PersonListV2 getPeople() {
        Option<Resource> resourceOption = client.read(endpoints.get("people").getHandle(), Collections.singletonList(PERSON_LIST));
        return extractObject(resourceOption, PersonListV2.class).some();
    }

    public Option<PersonV2> getPerson(ResourceHandle handle) {
        Option<Resource> resourceOption = client.read(handle, Collections.singletonList(PERSON));
        return extractObject(resourceOption, PersonV2.class);
    }

    public ResourceHandle addPerson(PersonV2 personV2) {
        return client.create(endpoints.get("people").getHandle(), createJAXBPayload("person", PersonV2.class, personV2, PERSON));
    }

    public Unit updatePerson(PersonV2 personV2) {
        return client.update(new ResourceHandle(URI.create(personV2.getUri())).toUnconditional(), createJAXBPayload("person", PersonV2.class, personV2, PERSON));
    }

    private static class MyRESTfulClient extends RESTfulClient {
        public MyRESTfulClient(HTTPCache cache, JAXBContext context, String username, String password) throws JAXBException {
            super(cache, username, password);
            registerHandler(JAXBHandler.create(context, PersonV2.class, PERSON));
            registerHandler(JAXBHandler.create(context, EventV2.class, EVENT));
            registerHandler(JAXBHandler.create(context, SessionV2.class, SESSION));
            registerHandler(JAXBHandler.create(context, RoomV2.class, ROOM));
            registerHandler(JAXBHandler.create(context, EventListV2.class, EVENT_LIST));
            registerHandler(JAXBHandler.create(context, SessionListV2.class, SESSION_LIST));
            registerHandler(JAXBHandler.create(context, PersonListV2.class, PERSON_LIST));
            registerHandler(JAXBHandler.create(context, RoomListV2.class, ROOM_LIST));
            registerHandler(new URIListHandler());
            registerHandler(new EndpointParser());
            registerHandler(new DefaultHandler());
        }
    }
}
