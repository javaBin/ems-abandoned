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

package no.java.ems.domain;

import no.java.ems.client.ResourceHandle;
import no.java.swing.WeakReferencePropertyChangeListener;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.net.URI;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public abstract class AbstractEntity {
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ResourceHandle handle;
    private int revision;
    private transient boolean modified;
    private String notes;
    private String displayID;
    private List<String> tags = new ArrayList<String>();

    public ResourceHandle getHandle() {
        return handle;
    }

    public void setHandle(final ResourceHandle handle) {
        firePropertyChange("handle", this.handle, this.handle = handle);
    }

    public String getDisplayID() {
        return displayID;
    }

    public void setDisplayID(String displayID) {
        firePropertyChange("displayID", this.displayID, this.displayID = displayID);
    }

    public int getRevision() {
        return revision;
    }

    /**
     * @param revision entity revision number &gt;= 0.
     * @throws IllegalArgumentException if revision &lt; 0.
     */
    public void setRevision(final int revision) {
        Validate.isTrue(revision >= 0, "Revision must be >= 0.");
        firePropertyChange("revision", this.revision, this.revision = revision);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        firePropertyChange("modified", this.modified, this.modified = modified);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        firePropertyChange("notes", this.notes, this.notes = notes);
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public void addTags(final List<String> tags) {
        ArrayList<String> allTags = new ArrayList<String>(this.tags);
        for (String tag : tags) {
            if (!allTags.contains(tag)) {
                allTags.add(tag);
            }
        }
        setTags(allTags);
    }

    public void setTags(final List<String> tags) {
        firePropertyChange("tags", getTags(), Collections.unmodifiableList(this.tags = new ArrayList<String>(tags)));
    }

    public String getTagsAsString(final String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String tag : tags) {
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(tag);
        }
        return builder.toString();
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        Validate.notNull(listener, "Listener may not be null");
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(final String property, final PropertyChangeListener listener) {
        Validate.notNull(property, "Property may not be null");
        Validate.notNull(listener, "Listener may not be null");
        propertyChangeSupport.addPropertyChangeListener(property, new WeakReferencePropertyChangeListener(listener));
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        Validate.notNull(listener, "Listener may not be null");
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final String property, final PropertyChangeListener listener) {
        Validate.notNull(property, "Property may not be null");
        Validate.notNull(listener, "Listener may not be null");
        propertyChangeSupport.removePropertyChangeListener(property, listener);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public final boolean equals(final Object other) {
        // final because we can not use uri-based equality without refactoring the Entities class:
        // when new entities are created, they have null ids and are placed in sets
        // when saved, the uri assigned by the server is set on the entity and therefore breaking the contract
        return super.equals(other);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        if (oldValue == null && newValue == null) {
            return;
        }
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
            if (!"modified".equals(propertyName)) {
                setModified(true);
            }
        }
    }

    public void sync(final AbstractEntity other) {
        setRevision(other.getRevision());
        setNotes(other.getNotes());
        setTags(other.getTags());
    }

    protected Object readResolve() {
        propertyChangeSupport = new PropertyChangeSupport(this);

        return this;
    }
}
