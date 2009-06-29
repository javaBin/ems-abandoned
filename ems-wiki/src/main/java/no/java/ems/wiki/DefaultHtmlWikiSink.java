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

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultHtmlWikiSink implements WikiSink {
    private StringBuilder html = new StringBuilder();

    private boolean liStarted;
    private int listDepth;
    private int lastLiDepth;

    // -----------------------------------------------------------------------
    // WikiSink Implementation
    // -----------------------------------------------------------------------

    public void startDocument() {
        appendLine("<div class=\"wiki\">");
    }

    public void endDocument() {
        appendLine("</div>");
    }

    public void startParagraph() {
        appendLine("<div class=\"paragraph\">");
    }

    public void endParagraph() {
        appendLine("</div>");
    }

    public void startUnorderedList() {
        if(liStarted) {
            appendLine("");
        }
        appendLine("<ul>");
        listDepth++;
        liStarted = false;
    }

    public void endUnorderedList() {
        endLi();
        listDepth--;
        appendLine("</ul>");
    }

    public void onHeading1(String heading) {
        addDiv("heading-1", heading);
    }

    public void onText(String line) {
        appendLine(line);
    }

    public void onItem(String item) {
        if(liStarted || lastLiDepth > listDepth) {
            endLi();
        }
        append("<li>");
        liStarted = true;
        append(item);
        lastLiDepth = listDepth;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public String toString() {
        return html.toString();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private void addDiv(String cssClass, String text) {
        html.append("<div class=\"").append(cssClass).append("\">").append(text).append("</div>").append(EOL);
    }

    private DefaultHtmlWikiSink append(String line) {
        html.append(line);

        return this;
    }

    private void appendLine(String line) {
        html.append(line).append(EOL);
    }

    private void endLi() {
        html.append("</li>").append(EOL);
        liStarted = false;
    }
}
