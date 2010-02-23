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

package no.java.ems.client.swing;

import no.java.ems.client.RESTEmsService;
import no.java.ems.client.ResourceHandle;
import no.java.ems.domain.*;
import no.java.swing.ApplicationTask;
import no.java.swing.BeanPropertyUndoableEdit;
import no.java.swing.DefaultUndoManager;
import org.apache.commons.lang.Validate;
import org.jdesktop.application.Task;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.net.URI;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class Entities extends HashSet<AbstractEntity> {

    private static final Entities INSTANCE = new Entities();
    private final EntityListener entityListener;
    private final ObservableList<Person> contacts;
    private final ObservableList<Event> events;
    private final ObservableList<Session> sessions;
    private final ObservableList<String> tags;
    private final ObservableList<String> keywords;
    private final Set<AbstractEntity> delete = new HashSet<AbstractEntity>();
    private boolean ignorePropertyEvents;

    private Entities() {
        super(1024);
        entityListener = new EntityListener();
        contacts = ObservableCollections.observableList(new ArrayList<Person>(1000));
        events = ObservableCollections.observableList(new ArrayList<Event>(100));
        sessions = ObservableCollections.observableList(new ArrayList<Session>(1000));
        tags = ObservableCollections.observableList(new ArrayList<String>(1000));
        keywords = ObservableCollections.observableList(new ArrayList<String>(1000));
    }

    public static Entities getInstance() {
        return INSTANCE;
    }

    public boolean isIgnorePropertyEvents() {
        return ignorePropertyEvents;
    }

    public void setIgnorePropertyEvents(boolean ignorePropertyEvents) {
        this.ignorePropertyEvents = ignorePropertyEvents;
    }

    @Override
    public boolean add(final AbstractEntity entity) {
        Validate.notNull(entity, "Entity may not be null");
        boolean wasAdded = super.add(entity);
        if (wasAdded) {
            if (entity instanceof Person) {
                contacts.add((Person)entity);
            }
            if (entity instanceof Event) {
                events.add((Event)entity);
            }
            if (entity instanceof Session) {
                Session session = (Session)entity;
                sessions.add(session);
                addKeywords(session.getKeywords());
            }
            entity.addPropertyChangeListener(entityListener);
            addTags(entity.getTags());
        }
        return wasAdded;
    }

    @Override
    public boolean remove(final Object object) {
        if (object instanceof AbstractEntity) {
            AbstractEntity entity = (AbstractEntity)object;
            boolean wasRemoved = super.remove(entity);
            if (wasRemoved) {
                entity.removePropertyChangeListener(entityListener);
                //noinspection SuspiciousMethodCalls
                if (contacts.contains(entity)) {
                    contacts.remove(entity);
                }
                //noinspection SuspiciousMethodCalls
                if (events.contains(entity)) {
                    events.remove(entity);
                }
                //noinspection SuspiciousMethodCalls
                if (sessions.contains(entity)) {
                    sessions.remove(entity);
                }
                EmsClient.getInstance().getRootPanel().close(entity);
            }
            return wasRemoved;
        }
        throw new IllegalArgumentException();
    }

    public ObservableList<Person> getContacts() {
        return contacts;
    }

    public ObservableList<Event> getEvents() {
        return events;
    }

    public ObservableList<Session> getSessions() {
        return sessions;
    }

    public ObservableList<String> getTags() {
        return tags;
    }

    public ObservableList<String> getKeywords() {
        return keywords;
    }

    public Task createRefreshContactsTask() {
        return new RefreshContactsTask();
    }

    public Task createRefreshEventsTask() {
        return new RefreshEventsTask();
    }

    public Task createSaveChangesTask() {
        return new SaveChangesTask();
    }

    public void addTags(final Collection<String> candidates) {
        if (candidates != null) {
            for (String tag : candidates) {
                if (!tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }
    }

    public void addKeywords(final Collection<String> candidates) {
        if (candidates != null) {
            for (String keyword : candidates) {
                if (!keywords.contains(keyword)) {
                    keywords.add(keyword);
                }
            }
        }
    }

    public boolean hasModifications() {
        for (AbstractEntity entity : this) {
            if (entity.getHandle() == null || entity.isModified()) {
                return true;
            }
        }
        return !delete.isEmpty();
    }

    public void registerForDeletion(final List<? extends AbstractEntity> entities) {
        for (AbstractEntity entity : entities) {
            boolean wasRemoved = remove(entity);
            if (wasRemoved && entity.getHandle() != null) {
                delete.add(entity);
            }
        }
    }

    public Person getContact(final ResourceHandle personId) {
        Validate.notNull(personId, "Person ID may not be null");
        for (Person contact : contacts) {
            if (personId.equals(contact.getHandle())) {
                return contact;
            }
        }
        return null;
    }

    public Event getEvent(final ResourceHandle eventId) {
        Validate.notNull(eventId, "Event ID may not be null");
        for (Event event : events) {
            if (eventId.equals(event.getHandle())) {
                return event;
            }
        }
        return null;
    }

    public Session getSession(final ResourceHandle sessionId) {
        Validate.notNull(sessionId, "Session ID may not be null");
        for (Session session : sessions) {
            if (sessionId.equals(session.getHandle())) {
                return session;
            }
        }
        return null;
    }

    public static boolean isLocalBinary(Binary binary) {
        return binary != null && (binary instanceof ByteArrayBinary || (binary instanceof URIBinary && ((URIBinary)binary).getURI().getScheme().equals("file")));
    }

    private class EntityListener implements PropertyChangeListener {

        public void propertyChange(final PropertyChangeEvent event) {
            DefaultUndoManager undoManager = DefaultUndoManager.getInstance(null);
            if (!undoManager.isUndoingOrRedoing() &&
                !ignorePropertyEvents &&
                !"modified".equals(event.getPropertyName())) {
                undoManager.addEdit(new BeanPropertyUndoableEdit(event));
            }
            // todo: consider collection all tags from all entities instead of simply adding any new tags
            if ("tags".equals(event.getPropertyName())) {
                // noinspection unchecked
                addTags((Collection<String>)event.getNewValue());
            }
            if ("keywords".equals(event.getPropertyName())) {
                // noinspection unchecked
                addKeywords((Collection<String>)event.getNewValue());
            }
        }

    }

    private class RefreshContactsTask extends ApplicationTask<List<Person>, Void> {

        public RefreshContactsTask() {
            super("no.java.ems.client.swing.Entities.refreshContactsTask");
        }

        protected List<Person> doInBackground() throws Exception {
            return EmsClient.getInstance().getClientService().getContacts();
        }

        @Override
        protected void succeeded(final List<Person> newContacts) {
            super.succeeded(newContacts);
            Set<Person> keep = new HashSet<Person>();
            for (Person newContact : newContacts) {
                Person existingContact = getContact(newContact.getHandle());
                if (existingContact == null) {
                    add(newContact);
                    keep.add(newContact);
                } else {
                    if (existingContact.isModified()) {
                        // todo: ask automerge/keep/replace
                        System.err.println("ignoring refresh for modified contact: " + existingContact.getName());
                    } else {
                        try {
                            Entities.getInstance().setIgnorePropertyEvents(true);
                            existingContact.sync(newContact);
                            existingContact.setModified(false);
                        } finally {
                            Entities.getInstance().setIgnorePropertyEvents(false);
                        }
                    }
                    keep.add(existingContact);
                }
            }
            HashSet<Person> toBeDeleted = new HashSet<Person>(contacts);
            toBeDeleted.removeAll(keep);
            removeAll(toBeDeleted);
        }

    }

    private class RefreshEventsTask extends ApplicationTask<List<Event>, Void> {

        public RefreshEventsTask() {
            super("no.java.ems.client.swing.Entities.refreshEventsTask");
        }

        protected List<Event> doInBackground() throws Exception {
            return EmsClient.getInstance().getClientService().getEvents();
        }

        @Override
        protected void succeeded(final List<Event> newEvents) {
            super.succeeded(newEvents);
            Set<Event> keep = new HashSet<Event>();
            for (Event newEvent : newEvents) {
                Event existingEvent = getEvent(newEvent.getHandle());
                if (existingEvent == null) {
                    add(newEvent);
                    keep.add(newEvent);
                } else {
                    if (existingEvent.isModified()) {
                        // todo: ask automerge/keep/replace
                        System.err.println("ignoring refresh for event object: " + existingEvent.getName());
                    } else {
                        try {
                            Entities.getInstance().setIgnorePropertyEvents(true);
                            existingEvent.sync(newEvent);
                            existingEvent.setModified(false);
                        } finally {
                            Entities.getInstance().setIgnorePropertyEvents(false);
                        }
                    }
                    keep.add(existingEvent);
                }
            }
            HashSet<Event> toBeDeleted = new HashSet<Event>(events);
            toBeDeleted.removeAll(keep);
            removeAll(toBeDeleted);
        }

    }

    private class SaveChangesTask extends ApplicationTask<Void, Map.Entry<AbstractEntity, AbstractEntity>> {

        private final List<Person> changedContacts = new ArrayList<Person>();
        private final List<Event> changedEvents = new ArrayList<Event>();
        private final List<Session> changedSessions = new ArrayList<Session>();
        private final List<Person> deletedContacts = new ArrayList<Person>();
        private final List<Event> deletedEvents = new ArrayList<Event>();
        private final List<Session> deletedSessions = new ArrayList<Session>();
        private final int count;

        public SaveChangesTask() {
            super("no.java.ems.client.swing.Entities.saveChangesTask");
            for (Person contact : contacts) {
                if (contact.isModified() || contact.getHandle() == null) {
                    changedContacts.add(contact);
                }
            }
            for (Event event : events) {
                if (event.isModified() || event.getHandle() == null) {
                    changedEvents.add(event);
                }
            }
            for (Session session : sessions) {
                if (session.isModified() || session.getHandle() == null) {
                    changedSessions.add(session);
                }
            }
            for (AbstractEntity entity : delete) {
                if (entity instanceof Event) {
                    deletedEvents.add((Event)entity);
                }
                if (entity instanceof Person) {
                    deletedContacts.add((Person)entity);
                }
                if (entity instanceof Session) {
                    deletedSessions.add((Session)entity);
                }
            }
            count = changedContacts.size()
                    + changedEvents.size()
                    + changedSessions.size()
                    + deletedContacts.size()
                    + deletedEvents.size()
                    + deletedSessions.size()
                    ;
        }

        protected Void doInBackground() throws Exception {
            RESTEmsService service = EmsClient.getInstance().getClientService();
            int processed = 0;
            for (Person contact : changedContacts) {
                setMessage(getString("contact", contact.getName()));
                Binary photoBinary = contact.getPhoto();
                if (isLocalBinary(photoBinary)) {
                    contact.setPhoto(service.saveBinary(photoBinary));
                }
                saveAttachments(contact);
                Person savedContact = service.saveContact(contact);
                setProgress(++processed, 0, count + 1);
                publish(new AbstractMap.SimpleEntry<AbstractEntity, AbstractEntity>(contact, savedContact));
            }
            for (Event event : changedEvents) {
                setMessage(getString("event", event.getName()));
                saveAttachments(event);
                Event savedEvent = service.saveEvent(event);
                setProgress(++processed, 0, count + 1);
                publish(new AbstractMap.SimpleEntry<AbstractEntity, AbstractEntity>(event, savedEvent));
            }
            for (Session session : changedSessions) {
                setMessage(getString("session", session.getTitle()));
                for (Speaker speaker : session) {
                    Binary photoBinary = speaker.getPhoto();
                    if (isLocalBinary(photoBinary)) {
                        speaker.setPhoto(service.saveBinary(photoBinary));
                    }
                }
                saveAttachments(session);
                Session savedSession = service.saveSession(session);
                setProgress(++processed, 0, count + 1);
                publish(new AbstractMap.SimpleEntry<AbstractEntity, AbstractEntity>(session, savedSession));
            }
            for (Session session : deletedSessions) {
                setMessage(getString("session.delete", session.getTitle()));
                service.deleteSession(session.getHandle());
                setProgress(++processed, 0, count + 1);
                publish(new AbstractMap.SimpleEntry<AbstractEntity, AbstractEntity>(session, null));
            }
            for (Event event : deletedEvents) {
                setMessage(getString("event.delete", event.getName()));
                service.deleteEvent(event.getHandle());
                setProgress(++processed, 0, count + 1);
                publish(new AbstractMap.SimpleEntry<AbstractEntity, AbstractEntity>(event, null));
            }
            for (Person contact : deletedContacts) {
                setMessage(getString("contact.delete", contact.getName()));
                service.deleteContact(contact.getHandle());
                setProgress(++processed, 0, count + 1);
                publish(new AbstractMap.SimpleEntry<AbstractEntity, AbstractEntity>(contact, null));
            }
            return null;
        }

        @Override
        protected void process(final List<Map.Entry<AbstractEntity, AbstractEntity>> entities) {
            for (Map.Entry<AbstractEntity, AbstractEntity> entity : entities) {
                if (entity.getValue() != null) {
                    try {
                        Entities.getInstance().setIgnorePropertyEvents(true);
                        entity.getKey().sync(entity.getValue());
                    } finally {
                        Entities.getInstance().setIgnorePropertyEvents(false);
                    }
                }
                entity.getKey().setModified(false);
                delete.remove(entity.getKey());
            }
        }

        private void saveAttachments(final AbstractEntity entity) {
            List<Binary> savedAttachments = new ArrayList<Binary>();
            RESTEmsService service = EmsClient.getInstance().getClientService();
            for (Binary attachement : entity.getAttachments()) {
                if (!isLocalBinary(attachement)) {
                    savedAttachments.add(attachement);
                } else {
                    savedAttachments.add(service.saveBinary(attachement));
                }
            }
            entity.setAttachments(savedAttachments);
        }
    }
}
