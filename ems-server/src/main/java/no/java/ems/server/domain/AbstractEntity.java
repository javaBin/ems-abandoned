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

package no.java.ems.server.domain;

import fj.data.Option;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fj.data.Option.none;
import static fj.data.Option.some;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public abstract class AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private int revision;
    private String notes;
    private List<String> tags = new ArrayList<String>();
    private List<Binary> attachments = new ArrayList<Binary>();
    private Option<DateTime> lastModified = some(new DateTime());
    private Option<String> modifiedBy = none();

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
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
        this.revision = revision;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public void addTags(final List<String> tags) {
        List<String> allTags = new ArrayList<String>(this.tags);
        for (String tag : tags) {
            if (!allTags.contains(tag)) {
                allTags.add(tag);
            }
        }
        setTags(allTags);
    }

    public void setTags(final List<String> tags) {
        if (tags != null) {
            this.tags = tags;
        }
    }

    public List<Binary> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public void addAttachment(Binary binary) {
        attachments.add(binary);
    }

    public void setAttachments(final List<Binary> attachments) {
        this.attachments = attachments;
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

    public Option<DateTime> getLastModified() {
        return lastModified;
    }

    public void setLastModified(Option<DateTime> lastModified) {
        this.lastModified = lastModified;
    }

    public Option<String> getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Option<String> modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public final boolean equals(final Object other) {
        // final because we can not use id-based equality without refactoring the Entities class:
        // when new entities are created, they have null ids and are placed in sets
        // when saved, the id assigned by the server is set on the entity and therefore breaking the contract
        return super.equals(other);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }
}
