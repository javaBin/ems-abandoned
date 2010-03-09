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

package no.java.ems.cli.command;

import fj.Effect;
import fj.F;
import fj.F2;
import fj.Function;
import fj.data.List;
import no.java.ems.client.ResourceHandle;
import no.java.ems.external.v2.*;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.InputStreamPayload;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ImportDirectory implements Runnable {

    private final EmsV2Client ems;
    private final File dir;
    private final ResourceHandle resourceHandle;
    private final F<InputStream, SessionV2> unmarshallSession;

    public ImportDirectory(EmsV2Client ems, ResourceHandle eventResourceHandle, File dir) {
        this.ems = ems;
        this.resourceHandle = eventResourceHandle;
        this.dir = dir;
        try {
            JAXBContext context = JAXBContext.newInstance(EventV2.class.getPackage().getName());
            final MIMEType mimeType = MIMEType.valueOf(MIMETypes.SESSION_MIME_TYPE);
            final JAXBHandler<SessionV2> handler = JAXBHandler.create(context, SessionV2.class, mimeType);
            unmarshallSession = new F<InputStream, SessionV2>() {
                public SessionV2 f(InputStream inputStream) {
                    return handler.handle(new InputStreamPayload(inputStream, mimeType));
                }
            };

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        List<File> files = List.list(dir.listFiles(new FilenameFilter() {
            public boolean accept(File file, String s) {
                return s.endsWith(".xml");
            }
        }));

        System.err.println("Importing " + files.length() + " sessions from " + dir.getAbsolutePath() + "...");

        List<SessionV2> sessions = files.
            map(Function.curry(readSession, resourceHandle));

        System.err.println("Adding " + sessions.length() + " objects...");

        sessions.foreach(addSession);

        System.err.println("Import complete");
    }

    private F2<ResourceHandle, File, SessionV2> readSession = new F2<ResourceHandle, File, SessionV2>() {
        public SessionV2 f(ResourceHandle eventId, File file) {
            try {
                SessionV2 session = unmarshallSession.f(new FileInputStream(file));
                session.setEventUri(eventId.getURI().toString());
                return session;
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found: " + file.getAbsolutePath(), e);
            }
        }
    };

    private Effect<SessionV2> addSession = new Effect<SessionV2>() {
        public void e(SessionV2 session) {
            ResourceHandle uri = ems.addSession(resourceHandle, session);

            System.err.println("URI: " + uri);
        }
    };
}
