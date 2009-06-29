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

import org.codehaus.httpcache4j.Status;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
* @version $Revision: #5 $ $Date: 2008/09/15 $
*/
public class HttpException extends RuntimeException {
    public HttpException(final URI pRequestURI, Status status) {
        this(pRequestURI, status, "");
    }

    public HttpException(final URI requestURI, Status status, final String message) {
        super(message + " " + requestURI + " " + status);
    }
}
