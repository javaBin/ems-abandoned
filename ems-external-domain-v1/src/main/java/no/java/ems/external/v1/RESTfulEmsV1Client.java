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

package no.java.ems.external.v1;

import fj.data.Option;
import static fj.data.Option.none;
import fj.Unit;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
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
public class RESTfulEmsV1Client implements EmsV1Client2 {

    private static final String CONTEXT_PATH = EventV1.class.getPackage().getName();

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


    public RESTfulEmsV1Client(HTTPCache cache, String username, String password) throws Exception {
        JAXBContext context = JAXBContext.newInstance(CONTEXT_PATH);
        marshaller = context.createMarshaller();
        client = new MyRESTfulClient(cache, context, username, password);
    }


    /**
     * Login. Get the endpoint and populate the endpioint map.
     *
     * @param endpoint the uri of the "1" webservice, e.g http://localhost:3000/ems/1
     */
    public void login(URI endpoint) {
        Map<String, EndpointParser.Endpoint> endpoints = new EndpointParser(client).parse(endpoint);
        this.endpoints.putAll(endpoints);
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


    public Option<EventV1> getEvent(ResourceHandle handle) {
        Option<Resource> resourceOption = client.read(handle, Collections.singletonList(EVENT));
        return extractObject(resourceOption, EventV1.class);
    }

    private <T> Option<T> extractObject(Option<Resource> resourceOption, Class<T> type) {
        if (resourceOption.isSome()) {
            Resource resource = resourceOption.some();
            return resource.getData(type);
        }
        return none();
    }

    public ResourceHandle addEvent(EventV1 event) {
        return client.create(endpoints.get("events").getHandle(), createJAXBPayload("event", EventV1.class, event, EVENT));
    }

    public EventListV1 getEvents() {
        Option<Resource> resourceOption = client.read(endpoints.get("events").getHandle(), Collections.singletonList(EVENT_LIST));
        return extractObject(resourceOption, EventListV1.class).some();
    }

    public SessionListV1 getSessions(ResourceHandle handle) {
        Option<Resource> resourceOption = client.read(handle, Collections.singletonList(SESSION_LIST));
        return extractObject(resourceOption, SessionListV1.class).some();
    }

    public Option<SessionV1> getSession(ResourceHandle handle) {
        Option<Resource> resourceOption = client.read(handle, Collections.singletonList(SESSION));
        return extractObject(resourceOption, SessionV1.class);
    }

    public SessionListV1 findSessions(ResourceHandle handle, String json) {
        Option<Resource> resourceOption = client.process(handle, new InputStreamPayload(IOUtils.toInputStream(json), JSON));
        Option<SessionListV1> option = extractObject(resourceOption, SessionListV1.class);
        if (option.isSome()) {
            return option.some();
        }
        return new SessionListV1(); //TODO: is this is a good idea?
    }

    public ResourceHandle addSession(ResourceHandle handle, SessionV1 session) {
        return client.create(handle, createJAXBPayload("session", SessionV1.class, session, SESSION));
    }

    public Unit updateSession(ResourceHandle handle, SessionV1 session) {
        return client.update(handle, createJAXBPayload("session", SessionV1.class, session, SESSION));
    }

    public PersonListV1 getPeople() {
        Option<Resource> resourceOption = client.read(endpoints.get("people").getHandle(), Collections.singletonList(PERSON_LIST));
        return extractObject(resourceOption, PersonListV1.class).some();
    }

    public Option<PersonV1> getPerson(ResourceHandle handle) {
        Option<Resource> resourceOption = client.read(handle, Collections.singletonList(PERSON));
        return extractObject(resourceOption, PersonV1.class);
    }

    public ResourceHandle addPerson(PersonV1 personV1) {
        return client.create(endpoints.get("people").getHandle(), createJAXBPayload("person", PersonV1.class, personV1, PERSON));
    }

    public Unit updatePerson(PersonV1 personV1) {
        return client.update(new ResourceHandle(URI.create(personV1.getUri())), createJAXBPayload("person", PersonV1.class, personV1, PERSON));
    }

    private static class JAXBHandler implements Handler {
        private final MIMEType mimeType;
        private final Unmarshaller unmarshaller;
        private final Class type;

        public JAXBHandler(JAXBContext context, Class type, MIMEType mimeType) throws JAXBException {
            this.mimeType = mimeType;
            this.type = type;
            unmarshaller = context.createUnmarshaller();
        }

        public boolean supports(MIMEType type) {
            return mimeType.includes(type);
        }

        @SuppressWarnings({"unchecked"})
        public Object handle(Payload payload) {
            try {
                Source source = new StreamSource(payload.getInputStream());
                return unmarshaller.unmarshal(source, type).getValue();
            } catch (JAXBException e) {
                throw new RuntimeException("Unable to unmarshall.", e);
            }
        }
    }

    private static class MyRESTfulClient extends RESTfulClient {
        public MyRESTfulClient(HTTPCache cache, JAXBContext context, String username, String password) throws Exception {
            super(cache, username, password);
            registerHandler(new JAXBHandler(context, EventV1.class, EVENT));
            registerHandler(new JAXBHandler(context, SessionV1.class, SESSION));
            registerHandler(new JAXBHandler(context, RoomV1.class, ROOM));
            registerHandler(new JAXBHandler(context, EventListV1.class, EVENT_LIST));
            registerHandler(new JAXBHandler(context, SessionListV1.class, SESSION_LIST));
            registerHandler(new JAXBHandler(context, RoomListV1.class, ROOM_LIST));
            registerHandler(new DefaultHandler());
            registerHandler(new URIListHandler());
        }
    }
}
