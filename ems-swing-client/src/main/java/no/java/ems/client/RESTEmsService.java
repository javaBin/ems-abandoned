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

import fj.*;
import fj.data.*;
import static fj.data.List.*;
import no.java.ems.client.f.*;
import no.java.ems.client.xhtml.Form;
import no.java.ems.client.xhtml.Options;
import no.java.ems.client.xhtml.TextElement;
import no.java.ems.domain.*;
import no.java.ems.domain.search.*;
import no.java.ems.external.v2.*;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.httpclient.*;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.cache.*;
import org.codehaus.httpcache4j.client.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Id $
 */
public class RESTEmsService {
    private EmsV2Client client;
    private final HTTPCache cache;

    public RESTEmsService(String baseURI, String username, String password) {
        cache = new HTTPCache(
                new MemoryCacheStorage(),
                new HTTPClientResponseResolver(new HttpClient(new MultiThreadedHttpConnectionManager())));
        client = createEmsClient(username, password);
        client.login(URI.create(baseURI));
    }

    private RESTfulEmsV2Client createEmsClient(String username, String password) {
        return new RESTfulEmsV2Client(cache, username, password);
    }

    public RESTEmsService(String baseURI) {
        this(baseURI, null, null);
    }

    public Either<Exception, List<Person>> getContacts() {
        return client.getPeople().right().map(new F<PersonListV2, List<Person>>() {
            @Override
            public List<Person> f(PersonListV2 personListV2) {
                return new ArrayList<Person>(iterableList(personListV2.getPerson()).map(ExternalV2F.person).toCollection());
            }
        });
    }

    public Person getContact(ResourceHandle id) {
        return Option.join(client.getPerson(id).right().toOption()).map(ExternalV2F.person).orSome((Person)null);
    }

    public Person saveContact(Person person) {
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

    public List<Event> getEvents() throws Exception {
        return new ArrayList<Event>(iterableList(throwLeft(client.getEvents()).getEvent()).map(ExternalV2F.event).toCollection());
    }

    public Event getEvent(ResourceHandle id) {
        return Option.join(client.getEvent(id).right().toOption()).
            map(ExternalV2F.event).
            orSome((Event)null);
    }

    public Event saveEvent(final Event event) {
        EventV2 eventToSave = ExternalV2F.eventV2.f(event);
        ResourceHandle handle = event.getHandle();
        if (handle == null) {
            handle = client.addEvent(eventToSave);
        }
        else {
            client.updateEvent(event.getHandle(), eventToSave);
        }

        Option<Event> updatedEvent = Option.join(client.getEvent(handle).right().toOption()).
            map(ExternalV2F.event);

        updatedEvent.foreach(new Effect<Event>() {
            @Override
            public void e(Event e) {
                event.sync(e);
            }
        });

        return updatedEvent.orSome((Event) null);
    }

    public List<Session> getSessions(Event event) throws Exception {
        SessionListV2 sessions = throwLeft(client.getSessions(new ResourceHandle(event.getSessionURI())));
        Collection<Session> list = iterableList(sessions.getSession()).map(ExternalV2F.session).toCollection();
        return new ArrayList<Session>(list);
    }

    public static <T> T throwLeft(Either<Exception, T> either) throws Exception {
        if(either.isLeft()) {
            throw either.left().value();
        }

        return either.right().value();
    }

    public Session getSession(ResourceHandle uri) throws Exception {
        return throwLeft(client.getSession(uri)).map(ExternalV2F.session).orSome((Session)null);
    }

    public Session saveSession(Session session) throws Exception {
        SessionV2 sessionV2 = ExternalV2F.sessionV2.f(session);
        if (session.getHandle() == null) {
            ResourceHandle handle = client.addSession(session.getEventHandle(), sessionV2);
            session.setHandle(handle);
            return session;
        }
        else {
            client.updateSession(session.getHandle(), sessionV2);
            Option<Session> sessionOption = throwLeft(client.getSession(session.getHandle().toUnconditional())).map(ExternalV2F.session);
            if (sessionOption.isSome()) {
                session.sync(sessionOption.some());
            }
            return session;
        }
    }

    public void delete(ResourceHandle handle) {
        HTTPResponse response = cache.doCachedRequest(new HTTPRequest(handle.getURI(), HTTPMethod.DELETE));
        if (response.getStatus() != Status.NO_CONTENT) {
            throw new HttpException(handle.getURI(), response.getStatus());
        }
    }

    public InputStream readBinary(Binary binary) {
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
        throw new IllegalArgumentException("Uknown binary...");
    }

    public Binary saveBinary(Binary binary) {
        // Always create the binary, no update at this point
        throw new UnsupportedOperationException("Not implemented yet...");
    }

    public void deleteBinary(URI binaryURI) {
        throw new UnsupportedOperationException("Not implemented yet...");
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------


    public List<Room> getRooms() {
        throw new UnsupportedOperationException("Not implemented yet...");
    }

    public List<SearchResult> search(String query, ObjectType type) {
        List<SearchResult> result = new ArrayList<SearchResult>();
        Form form = client.searchForm();
        TextElement search = form.getTextElement("q");
        Options options = form.getOptions("type");
        search.setValue(query);
        options.addSelection(type.name());
        Feed feed = client.search(form);
        List<Entry> entries = feed.getEntries();
        fj.data.List<SearchResult> list = fj.data.List.iterableList(entries).map(toSearchResult());
        result.addAll(list.toCollection());
        return result;
    }

    private F<Entry, SearchResult> toSearchResult() {
        return new F<Entry, SearchResult>() {
            @Override
            public SearchResult f(Entry entry) {
                try {
                    return new SearchResult(entry.getEditLink().getHref().toURI(), entry.getTitle(), entry.getSummary());
                } catch (URISyntaxException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
