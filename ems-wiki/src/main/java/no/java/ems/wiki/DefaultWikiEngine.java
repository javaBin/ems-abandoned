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

package no.java.ems.wiki;

import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultWikiEngine<S extends WikiSink> implements WikiEngine {

    private BufferedReader reader;

    private String currentLine;

    private S sink;

    public DefaultWikiEngine(S sink) {
        this.sink = sink;
    }

    // -----------------------------------------------------------------------
    // WikiEngine
    // -----------------------------------------------------------------------

    public void transform(String text) throws IOException {
        reader = new BufferedReader(new StringReader(text));

        sink.startDocument();

        while (getLine()) {
            if (currentLine.startsWith("h1. ")) {
                sink.onHeading1(currentLine.substring(4));
                continue;
            }

            if (currentLine.equals("")) {
                continue;
            }

            if (currentLine.startsWith("*")) {
                doUnorderedList(0);
            }
            else {
                onParagraph();
            }
        }

        sink.endDocument();
    }

    public S getSink() {
        return sink;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private void onParagraph() throws IOException {
        sink.startParagraph();

        do {
            if (currentLine.equals("")) {
                break;
            }

            sink.onText(currentLine);
        } while (getLine());

        sink.endParagraph();
    }

    private void doUnorderedList(int n) throws IOException {
        sink.startUnorderedList();

        String x = StringUtils.repeat("*", n + 1);
        String x2 = StringUtils.repeat("*", n + 2);

        do {
            // Check if we need to recurse deeper
            if(currentLine.startsWith(x2)) {
                doUnorderedList(n + 1);
            }

            // When getting back, make sure we're still on "this" level
            if (!currentLine.startsWith(x)) {
                break;
            }

            // Remove the leading stars
            currentLine = currentLine.substring(n + 1).trim();

            sink.onItem(currentLine);
        } while (getLine());

        sink.endUnorderedList();
    }

    public boolean getLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return false;
        }

        currentLine = line.trim();

        return true;
    }
}
