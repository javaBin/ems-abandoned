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

import fj.*;
import fj.data.*;
import static fj.data.Either.*;
import no.java.ems.client.*;
import no.java.ems.client.xhtml.Form;
import no.java.ems.client.xhtml.XHTMLFormParser;
import org.apache.abdera.model.Feed;
import org.apache.commons.io.*;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.cache.*;
import org.codehaus.httpcache4j.payload.*;

import javax.xml.bind.*;
import javax.xml.namespace.*;
import java.io.*;
import java.lang.Class;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.util.HashMap;
import java.util.List;

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
    private static final MIMEType EVENT_LIST = MIMEType.valueOf(MIMETypes.EVENT_LIST_MIME_TYPE);
    public static final MIMEType SESSION_LIST = MIMEType.valueOf(MIMETypes.SESSION_LIST_MIME_TYPE);
    private static final MIMEType EVENT = MIMEType.valueOf(MIMETypes.EVENT_MIME_TYPE);
    private static final MIMEType PERSON_LIST = MIMEType.valueOf(MIMETypes.PERSON_LIST_MIME_TYPE);
    private static final MIMEType PERSON = MIMEType.valueOf(MIMETypes.PERSON_MIME_TYPE);
    private static final MIMEType ROOM_LIST = MIMEType.valueOf(MIMETypes.ROOM_LIST_MIME_TYPE);
    private static final MIMEType ROOM = MIMEType.valueOf(MIMETypes.ROOM_MIME_TYPE);
    private static final MIMEType ENDPOINT = MIMEType.valueOf(MIMETypes.ENDPOINT_MIME_TYPE);
    private static final MIMEType ATOM = new MIMEType("application/atom+xml");
    private static final MIMEType XHTML = MIMEType.valueOf("application/xhtml+xml");

    private P1<EventListV2> eventListV2P1 = new P1<EventListV2>() {
        @Override
        public EventListV2 _1() {
            return new EventListV2();
        }
    };
    private P1<SessionListV2> sessionListV2P1 = new P1<SessionListV2>() {
        @Override
        public SessionListV2 _1() {
            return new SessionListV2();
        }
    };
    private P1<PersonListV2> personListV2P1 = new P1<PersonListV2>() {
        @Override
        public PersonListV2 _1() {
            return new PersonListV2();
        }
    };

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
        Either<Exception, Option<Resource>> endpoints = client.read(new ResourceHandle(endpoint), Collections.singletonList(ENDPOINT));

        Option.join(endpoints.right().toOption()).foreach(new Effect<Resource>() {
            public void e(Resource resource) {
                Option<Map> data = resource.getData(Map.class);
                if (data.isSome()) {
                    Map<String, EndpointParser.Endpoint> map = data.some();
                    RESTfulEmsV2Client.this.endpoints.putAll(map);
                }
            }
        });
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

    public final F<ResourceHandle, Either<Exception, Option<EventV2>>> getEvent_ = new F<ResourceHandle, Either<Exception, Option<EventV2>>>() {
        public Either<Exception, Option<EventV2>> f(ResourceHandle resourceHandle) {
            return getEvent(resourceHandle);
        }
    };

    public Either<Exception, Option<EventV2>> getEvent(ResourceHandle handle) {
        return bindRight(client.read(handle, Collections.singletonList(EVENT)), this.<EventV2>getData().f(EventV2.class));
    }

    private <T> F<Class<T>, F<Resource, Option<T>>> getData() {
        return new F<Class<T>, F<Resource, Option<T>>>() {
            public F<Resource, Option<T>> f(final Class<T> klass) {
                return new F<Resource, Option<T>>() {
                    public Option<T> f(Resource resource) {
                        return resource.getData(klass);
                    }
                };
            }
        };
    }

    public Unit updateEvent(ResourceHandle handle, EventV2 event) {
        return client.update(handle, createJAXBPayload("event", EventV2.class, event, EVENT));
    }

    public ResourceHandle addEvent(EventV2 event) {
        return client.create(endpoints.get("events").getHandle(), createJAXBPayload("event", EventV2.class, event, EVENT));
    }

    public Either<Exception, EventListV2> getEvents() {
        return bindRightOrValue(client.read(endpoints.get("events").getHandle(), Collections.singletonList(EVENT_LIST)), this.<EventListV2>getData().f(EventListV2.class), eventListV2P1);
    }

    public Either<Exception, SessionListV2> getSessions(ResourceHandle handle) {
        return bindRightOrValue(client.read(handle, Collections.singletonList(SESSION_LIST)), this.<SessionListV2>getData().f(SessionListV2.class), sessionListV2P1);
    }

    public F<ResourceHandle, Either<Exception, SessionListV2>> getSessions_ = new F<ResourceHandle, Either<Exception, SessionListV2>>() {
        public Either<Exception, SessionListV2> f(ResourceHandle resourceHandle) {
            return getSessions(resourceHandle);
        }
    };

    public Either<Exception, Option<SessionV2>> getSession(ResourceHandle handle) {
        return bindRight(client.read(handle, Collections.singletonList(SESSION)), this.<SessionV2>getData().f(SessionV2.class));
    }

    public final F<ResourceHandle, Either<Exception, Option<SessionV2>>> getSession_ = new F<ResourceHandle, Either<Exception, Option<SessionV2>>>() {
        public Either<Exception, Option<SessionV2>> f(ResourceHandle resourceHandle) {
            return getSession(resourceHandle);
        }
    };

    public ResourceHandle addSession(ResourceHandle eventHandle, SessionV2 session) {
      Option<Headers> headers = client.inspect(eventHandle);
      if (headers.isSome()) {
        Headers h = headers.some();
        Header header = h.getFirstHeader("Link");
        if (header != null) {
          List<LinkDirective> links = HeaderUtils.toLinkDirectives(header);
          LinkDirective link = links.get(0);
          return client.create(new ResourceHandle(link.getURI()), createJAXBPayload("session", SessionV2.class, session, SESSION));
        }
      }
      throw new IllegalArgumentException("Not possible to inspect Event found at " + session.getEventUri());
    }

    public Unit updateSession(ResourceHandle handle, SessionV2 session) {
        return client.update(handle, createJAXBPayload("session", SessionV2.class, session, SESSION));
    }

    public Either<Exception, PersonListV2> getPeople() {
        return bindRightOrValue(client.read(endpoints.get("people").getHandle(), Collections.singletonList(PERSON_LIST)), this.<PersonListV2>getData().f(PersonListV2.class), personListV2P1);
    }

    public Either<Exception, Option<PersonV2>> getPerson(ResourceHandle handle) {
        return bindRight(client.read(handle, Collections.singletonList(PERSON)), this.<PersonV2>getData().f(PersonV2.class));
    }

    public ResourceHandle addPerson(PersonV2 personV2) {
        return client.create(endpoints.get("people").getHandle(), createJAXBPayload("person", PersonV2.class, personV2, PERSON));
    }

    public Unit updatePerson(PersonV2 personV2) {
        return client.update(new ResourceHandle(URI.create(personV2.getUri())).toUnconditional(), createJAXBPayload("person", PersonV2.class, personV2, PERSON));
    }

    public Either<Exception, Option<Form>> getSearchForm() {
        final EndpointParser.Endpoint endpoint = endpoints.get("search");
        Either<Exception, Option<InputStream>> result = bindRight(client.read(endpoint.getHandle(), Collections.singletonList(XHTML)), this.<InputStream>getData().f(InputStream.class));
        if (result.isRight()) {
            return result.right().map(flatMap(parseForm(endpoint)));
        }
        throw new IllegalStateException("Unable to get search form");
    }

    public Either<Exception, Option<Feed>> search(Form form) {
        F<Resource, Option<Feed>> feedF = this.<Feed>getData().f(Feed.class);
        if (form.getMethod() == HTTPMethod.GET) {
            return bindRight(client.read(new ResourceHandle(form.constructURI()), Collections.singletonList(ATOM)), feedF);
        }
        else {
            return bindRight(client.process(new ResourceHandle(form.getAction()), form.toPayload()), feedF);
        }
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
            registerHandler(new AbderaHandler());
            registerHandler(new DefaultHandler());
        }
    }
    private <T> Either<Exception, T> bindRightOrValue(Either<Exception, Option<Resource>> either, F<Resource, Option<T>> f, P1<T> p1) {
        if(either.isLeft()) {
            return left(either.left().value());
        }

        return right(either.right().value().bind(f).orSome(p1));
    }

    private <T> Either<Exception, Option<T>> bindRight(Either<Exception, Option<Resource>> either, F<Resource, Option<T>> f) {
        if(either.isLeft()) {
            return left(either.left().value());
        }

        return right(either.right().value().bind(f));
    }

    private F<InputStream, Form> parseForm(final EndpointParser.Endpoint endpoint) {
        return new F<InputStream, Form>() {
            public Form f(InputStream inputStream) {
                XHTMLFormParser parser = new XHTMLFormParser(endpoint.getURI(), inputStream);
                return parser.parse().get(0);
            }
        };
    }

    private static <A, B> F<Option<A>, Option<B>> flatMap(final F<A,B> f) {
        return new F<Option<A>, Option<B>>() {
            public Option<B> f(Option<A> aOption) {
                return aOption.map(f);
            }
        };
    }
}
