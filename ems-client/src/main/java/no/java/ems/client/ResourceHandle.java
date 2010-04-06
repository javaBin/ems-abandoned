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

import org.codehaus.httpcache4j.Tag;
import org.apache.commons.lang.Validate;

import java.net.URI;

import fj.data.Option;
import fj.F;

import static fj.data.Option.some;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class ResourceHandle {
    private final URI uri;
    private final Option<Tag> tag;

    ResourceHandle(final URI uri, final Option<Tag> tag) {
        Validate.notNull(uri, "URI may not be null");
        this.uri = uri;
        this.tag = tag;
    }

    public ResourceHandle(final URI pUri) {
        this(pUri, Option.<Tag>none());
    }

    public URI getURI() {
        return uri;
    }

    Option<Tag> getTag() {
        return tag;
    }

    public boolean isTagged() {
        return tag.isSome();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ResourceHandle that = (ResourceHandle) o;

        Boolean equalTag = tag.map(new F<Tag, Boolean>() {
            public Boolean f(Tag tag) {
                return that.tag.isSome() && tag.equals(that.tag.some());
            }
        }).orSome(true);
        if (!equalTag) {
            return false;
        }
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        int hc = tag.map(new F<Tag, Integer>() {
            public Integer f(Tag tag) {
                return tag.hashCode();
            }}).orSome(0);
        result = 31 * result + hc;
        return result;
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    public ResourceHandle toUnconditional() {
        return new ResourceHandle(uri, some(Tag.ALL));
    }
}
