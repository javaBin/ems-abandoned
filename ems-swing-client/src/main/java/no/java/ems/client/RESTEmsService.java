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

package no.java.ems.client;

import no.java.ems.domain.search.ObjectType;
import no.java.ems.domain.search.SearchResult;
import no.java.ems.external.v2.*;
import no.java.ems.domain.*;
import no.java.ems.client.f.ExternalV2F;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.net.URI;
import java.io.InputStream;

import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.client.HTTPClientResponseResolver;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Status;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import fj.data.Option;
import fj.P;
import fj.P2;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Id $
 */
public class RESTEmsService {
    private EmsV2Client client;
    private final HTTPCache cache;
    private final String baseURI;

    public RESTEmsService(String baseURI, String username, String password) {
        cache = new HTTPCache(
                new MemoryCacheStorage(),
                new HTTPClientResponseResolver(new HttpClient(new MultiThreadedHttpConnectionManager())));
        this.baseURI = baseURI;
        client = createEmsClient(username, password);
    }

    private RestletEmsV2Client createEmsClient(String username, String password) {
        Option<P2<String,String>> credentials =  Option.none();
        if (username != null && password != null) {
            credentials = Option.some(P.p(username, password));
        }
        return new RestletEmsV2Client(cache, this.baseURI, credentials);
    }

    public RESTEmsService(String baseURI) {
        this(baseURI, null, null);
    }

    public synchronized List<Person> getContacts() {
        PersonListV2 people = client.getPeople();       
        Collection<Person> persons = fj.data.List.iterableList(people.getPerson()).map(ExternalV2F.person).toCollection();
        return new ArrayList<Person>(persons);
    }

    public synchronized Person getContact(String id) {
        Option<Person> person = client.getPerson(id).map(ExternalV2F.person);
        if (person.isSome()) {
            return person.some();
        }
        return null;
    }

    public synchronized Person saveContact(Person person) {
        Option<PersonV2> option = Option.some(person).map(ExternalV2F.personV2);
        if (person.getHandle() == null) {
            ResourceHandle handle = client.addPerson(option.some());                       
            person.setHandle(handle);
        }
        else {
            client.updatePerson(option.some());
        }
        return person;
    }

    public synchronized void deleteContact(ResourceHandle person) {
        //client.removePerson(person.getURI());
        throw new UnsupportedOperationException("Not implemented yet...");
    }

    public synchronized List<Event> getEvents() {
        EventListV2 either = client.getEvents();        
        Collection<Event> events = fj.data.List.iterableList(either.getEvent()).map(ExternalV2F.event).toCollection();
        return new ArrayList<Event>(events);
    }

    public synchronized Event getEvent(ResourceHandle id) {
        String path = getUUID(id.getURI());
        Option<Event> event = client.getEvent(path)
                .map(ExternalV2F.event);

        if (event.isSome()) {
            return event.some();
        }
        return null;
    }

    public synchronized Event saveEvent(Event event) {
        Option<EventV2> eventToSave = Option.some(event).map(ExternalV2F.eventV2);
        throw new UnsupportedOperationException("Not implemented yet...");
        /*if (event.getHandle() == null) {
            ResourceHandle handle = client.addEvent(eventToSave.some());
        }
        else {

        }*/
    }

    public synchronized void deleteEvent(ResourceHandle id) {
        throw new UnsupportedOperationException("Not implemented yet...");
    }

    public synchronized List<Session> getSessions(Event event) {
        SessionListV2 sessions = client.getSessions(event.getDisplayID());
        Collection<Session> list = fj.data.List.iterableList(sessions.getSession()).map(ExternalV2F.session).toCollection();
        return new ArrayList<Session>(list);
    }

    //TODO: throw away this!!!!
    private String getUUID(URI uri) {
        if (uri.toString().contains("/")) {
            String path = uri.getPath();
            int index = path.lastIndexOf("/");
            return path.substring(index + 1, path.length());
        }
        return uri.toString();
    }

    public synchronized Session getSession(String eventId, String sessionId) {
        Option<Session> option = client.getSession(eventId, sessionId).
                map(ExternalV2F.session);
        if (option.isSome()) {
            return option.some();
        }
        return null;
    }

    public synchronized Session saveSession(Session session) {
        Option<SessionV2> option = Option.some(session).map(ExternalV2F.sessionV2);
        if (option.isSome()) {
            if (session.getHandle() == null) {
                ResourceHandle handle = client.addSession(option.some());
                session.setHandle(handle);
                return session;
            }
            else {
                client.updateSession(option.some());
                option = client.getSession(getUUID(session.getEventHandle().getURI()), session.getDisplayID());//TODO: EVIL: go away, should be session.getURI()
                Option<Session> sessionOption = option.map(ExternalV2F.session);
                if (sessionOption.isSome()) {
                    session.sync(sessionOption.some());
                }
                return session;
            }
        }
        throw new IllegalArgumentException("Unable to save session");
    }
    
    public synchronized void deleteSession(ResourceHandle handle) {
        throw new UnsupportedOperationException("Not implemented yet...");
    }

    public synchronized InputStream readBinary(Binary binary) {
        if (binary instanceof URIBinary) {
            URIBinary URIBinary = (URIBinary) binary;
            HTTPRequest request = new HTTPRequest(URIBinary.getURI());
            HTTPResponse response = cache.doCachedRequest(request);
            if (response.getStatus() == Status.OK && response.hasPayload()) {
                return response.getPayload().getInputStream();
            }
        }
        else if (binary instanceof ByteArrayBinary) {
            ByteArrayBinary array = (ByteArrayBinary) binary;
            return array.getDataStream();
        }
        //return binaryClient.getBinary(binaryId);
        throw new IllegalArgumentException("Uknown binary...");
    }

    public synchronized Binary saveBinary(Binary binary) {
        // Always create the binary, no update at this point
        throw new UnsupportedOperationException("Not implemented yet...");
    }

    public synchronized void deleteBinary(URI binaryURI) {
        throw new UnsupportedOperationException("Not implemented yet...");
    }
    
    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------


    public synchronized List<Room> getRooms() {
        throw new UnsupportedOperationException("Not implemented yet...");
    }

    public List<SearchResult> search(String query, ObjectType type) {
        List<SearchResult> list = new ArrayList<SearchResult>();
        for (int i = 0; i < 10; i++) {
            list.add(new SearchResult(URI.create("" + i), "Title" + i, type, "Summary" + i));
        }
        return list;
    }
}
