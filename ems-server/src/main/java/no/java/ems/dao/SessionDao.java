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

package no.java.ems.dao;

import no.java.ems.server.domain.Session;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public interface SessionDao {

    List<Session> getSessions(String eventId);

    List<String> getSessionIdsByEventId(String eventId);

    List<String> findSessionsBySpeakerName(String eventId, String name);

    List<String> findSessionsByTitle(String eventId, String title);

    List<String> findSessionsByDate(String eventId, LocalDate date);

    /**
     * @deprecated
     */
    Session getSession(String id);
    
    Session getSession(String eventId, String id);

    void saveSession(Session session);

    void deleteSession(String id);
}
