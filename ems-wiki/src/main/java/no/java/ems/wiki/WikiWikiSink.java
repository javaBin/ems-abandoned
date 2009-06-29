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

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WikiWikiSink implements WikiSink {
    private StringBuilder text = new StringBuilder();

    private int listIndent;
    private String listIndentString;

    // -----------------------------------------------------------------------
    // WikiSink Implementation
    // -----------------------------------------------------------------------

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startParagraph() {
    }

    public void endParagraph() {
        addLine("");
    }

    public void startUnorderedList() {
        listIndent++;
        listIndentString = StringUtils.repeat("*", listIndent);
    }

    public void endUnorderedList() {
        listIndent--;

        if (listIndent == 0) {
            addLine("");
        } else {
            listIndentString = StringUtils.repeat("*", listIndent);
        }
    }

    public void onHeading1(String s) {
        addLine("h1. " + s);
        addLine("");
    }

    public void onText(String line) {
        addLine(line);
    }

    public void onItem(String item) {
        addLine(" " + listIndentString + " " + item);
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public String toString() {
        return text.toString();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private void addLine(String s) {
        text.append(s).append(EOL);
    }
}
