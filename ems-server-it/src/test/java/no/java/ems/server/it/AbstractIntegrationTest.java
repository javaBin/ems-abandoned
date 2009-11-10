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

package no.java.ems.server.it;

import no.java.ems.dao.EventDao;
import no.java.ems.dao.RoomDao;
import no.java.ems.dao.SessionDao;
import no.java.ems.external.v1.RestletEmsV1Client;
import no.java.ems.server.DerbyService;
import no.java.ems.server.EmsSrcEmbedder;
import no.java.ems.server.domain.Room;
import no.java.ems.util.TestHelper;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractIntegrationTest {
    protected final Room room1 = new Room("Room 1");
    protected final Room room2 = new Room("Room 2");
    protected final Room room3 = new Room("Room 3");

    protected final LocalDateTime jz06sep130900 = new LocalDateTime(2006, 9, 13, 9, 0);
    protected final LocalDateTime jz06sep131015 = new LocalDateTime(2006, 9, 13, 10, 15);
    protected final LocalDateTime jz06sep131145 = new LocalDateTime(2006, 9, 13, 11, 45);
    protected final LocalDateTime jz06sep131300 = new LocalDateTime(2006, 9, 13, 13, 0);
    protected final LocalDateTime jz06sep131415 = new LocalDateTime(2006, 9, 13, 14, 15);
    protected final LocalDateTime jz06sep131545 = new LocalDateTime(2006, 9, 13, 15, 45);
    protected final LocalDateTime jz06sep131700 = new LocalDateTime(2006, 9, 13, 17, 0);

    protected final Interval jz06sep13slot1 = new Interval(jz06sep130900.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot2 = new Interval(jz06sep131015.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot3 = new Interval(jz06sep131145.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot4 = new Interval(jz06sep131300.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot5 = new Interval(jz06sep131415.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot6 = new Interval(jz06sep131545.toDateTime(), Minutes.minutes(60));
    protected final Interval jz06sep13slot7 = new Interval(jz06sep131700.toDateTime(), Minutes.minutes(60));

    private static EmsSrcEmbedder embedder;
    protected static String baseUri;
    protected static RestletEmsV1Client ems;

    @BeforeClass
    public static void beforeClass() throws Exception {
        String baseDir = TestHelper.getBaseDir(AbstractIntegrationTest.class).getPath();
        File emsHome = PlexusTestCase.getTestFile(baseDir + "/..", "target/ems-home-" + EventDatoIntegrationTest.class.getName());
        FileUtils.deleteDirectory(emsHome);
        assertTrue(emsHome.mkdirs());
//        assertTrue(new File(emsHome, "database/ems").mkdirs());
//        System.out.println("new File(emsHome, \"database/ems\").getAbsolutePath() = " + new File(emsHome, "database/ems").getAbsolutePath());

        embedder = new EmsSrcEmbedder(new File(baseDir + "/../ems-server/"), emsHome);
        embedder.start();
        embedder.getBean(DerbyService.class).maybeCreateTables(false);
        baseUri = embedder.getBaseUri();

        ems = new RestletEmsV1Client(new InMemoryHttpCache(), baseUri);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (embedder != null) {
            embedder.stop();
        }
    }

    public static EventDao getEventDao() {
        return embedder.getBean(EventDao.class);
    }

    public static DerbyService getDerbyService() {
        return embedder.getBean(DerbyService.class);
    }

    public static RoomDao getRoomDao() {
        return embedder.getBean(RoomDao.class);
    }

    public static SessionDao getSessionDao() {
        return embedder.getBean(SessionDao.class);
    }
}
