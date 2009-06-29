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

import fj.*;
import fj.data.List;
import fj.data.Option;

import javax.ws.rs.core.*;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import javax.xml.bind.JAXBElement;

import no.java.ems.server.domain.AbstractEntity;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.HashMap;


/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ResourcesF {
    public static P1<Response.ResponseBuilder> notFound = P.p(status(NOT_FOUND));

    public static <A> F<A, Response.ResponseBuilder> toOk() {
        return new F<A, Response.ResponseBuilder>() {
            public Response.ResponseBuilder f(A a) {
                return ok(a);
            }
        };
    }

    public static <A> F3<Option<? extends AbstractEntity>, Request, JAXBElement<A>, Response.ResponseBuilder> singleResponseBuilderWithTagChecking() {
        return new F3<Option<? extends AbstractEntity>, Request, JAXBElement<A>, Response.ResponseBuilder>() {
            public Response.ResponseBuilder f(Option<? extends AbstractEntity> option, Request request, JAXBElement<A> element) {
                if (option.isSome()) {
                    String tag = Integer.toHexString(option.some().getRevision());
                    Response.ResponseBuilder builder = request.evaluatePreconditions(new EntityTag(tag));
                    if (builder != null) {
                        return builder;
                    }
                    builder = ok(getEntity(element));
                    builder.tag(tag);
                    return builder;
                }
                else {
                    return status(NOT_FOUND);
                }
            }
        };
    }

    public static <A, B extends AbstractEntity> F2<List<B>, JAXBElement<A>, Response.ResponseBuilder> multipleOkResponseBuilder() {
        return new F2<List<B>, JAXBElement<A>, Response.ResponseBuilder>() {
            public Response.ResponseBuilder f(List<B> entityList, JAXBElement<A> element) {
                Response.ResponseBuilder builder = ok(getEntity(element));
                int tag = 0;
                for (AbstractEntity entity : entityList) {
                    tag += entity.getRevision();
                }
                builder.tag(Integer.toHexString(tag));
                return builder;
            }
        };
    }


    static <A> GenericEntity<JAXBElement<A>> getEntity(JAXBElement<A> thing) {
        return new GenericEntity<JAXBElement<A>>(thing, JAXBElement.class);
    }

    static boolean matches(AbstractEntity original, HttpHeaders headers) {
        java.util.List<String> IfMatch = headers.getRequestHeader("If-Match");
        if (IfMatch != null && !IfMatch.isEmpty()) {
            EntityTag match = EntityTag.valueOf(IfMatch.get(0));
            if (new EntityTag(Integer.toHexString(original.getRevision())).equals(match) || EntityTag.valueOf("*").equals(match)) {
                return true;
            }
        }
        return false;
    }

    public static F<StringBuilder, Response.ResponseBuilder> uriListOkResponseBuilder() {
        return new F<StringBuilder, Response.ResponseBuilder>() {
            public Response.ResponseBuilder f(StringBuilder builder) {
                return ok(builder.toString());
            }
        };
    }

    static String getFileName(String contentDispositionHeader) {
        Map<String, String> directives = new HashMap<String, String>();
        if (StringUtils.isNotBlank(contentDispositionHeader)) {
            String[] parts = contentDispositionHeader.split(";");
            for (String part : parts) {
                part = part.trim();
                if (!StringUtils.isBlank(part)) {
                    String[] directiveParts = part.split("=", 2);
                    directives.put(directiveParts[0], directiveParts.length > 1 ? directiveParts[1] : null);
                }
            }
            return directives.get("filename");
        }
        return null;
    }
}
