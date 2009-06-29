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

package no.java.ems.client.swing;

import no.java.ems.domain.AbstractEntity;
import no.java.ems.domain.Session;
import org.apache.commons.lang.Validate;

import javax.swing.*;
import java.util.*;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public class Dashboard<T> extends JPanel {

    private final List<Filter<T>> filters;
    private final List<JLabel> labels;

    public Dashboard(final Filter<T>... filters) {
        Validate.noNullElements(filters);
        this.filters = new ArrayList<Filter<T>>();
        labels = new ArrayList<JLabel>();
        for (Filter<T> filter : filters) {
            this.filters.add(filter);
            JLabel label = new JLabel();
            labels.add(label);
            add(label);
        }
        update(Collections.<T>emptyList());
    }

    public void update(final List<T> elements) {
        int[] count = new int[filters.size()];
        for (T element : elements) {
            for (int index = 0; index < filters.size(); index++) {
                if (filters.get(index).count(element)) {
                    count[index]++;
                }
            }
        }
        for (int index = 0; index < filters.size(); index++) {
            labels.get(index).setText(filters.get(index).describe(elements.size(), count[index]));
        }
    }

    public interface Filter<T> {

        String describe(int total, int matching);

        boolean count(final T element);

    }

    public static class TagFilter<U extends AbstractEntity> implements Filter<U> {

        private final Set<String> tags;

        public TagFilter(final String... tags) {
            this.tags = new HashSet<String>(Arrays.asList(tags));
        }

        public String describe(int total, int matching) {
            return String.format(String.format("%s: %s", tags, matching));
        }

        public boolean count(U element) {
            // todo: create hasTag() for performance
            List<String> tags = element.getTags();
            for (String tag : this.tags) {
                if (tags.contains(tag)) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class KeywordFilter<U extends Session> implements Filter<U> {

        private final Set<String> keywords;

        public KeywordFilter(final String... keywords) {
            this.keywords = new HashSet<String>(Arrays.asList(keywords));
        }

        public String describe(int total, int matching) {
            return String.format(String.format("%s: %s", keywords, matching));
        }

        public boolean count(U element) {
            // todo: create hasKeyword() for performance
            List<String> keywords = element.getKeywords();
            for (String keyword : this.keywords) {
                if (keywords.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class LevelFilter<U extends Session> implements Filter<U> {

        private final Set<Session.Level> levels;

        public LevelFilter(final Session.Level... levels) {
            this.levels = new HashSet<Session.Level>(Arrays.asList(levels));
        }

        public String describe(int total, int matching) {
            return String.format(String.format("%s: %s", levels, matching));
        }

        public boolean count(U element) {
            return levels.contains(element.getLevel());
        }

    }

    public static class StateFilter<U extends Session> implements Filter<U> {

        private final Set<Session.State> states;

        public StateFilter(final Session.State... states) {
            this.states = new HashSet<Session.State>(Arrays.asList(states));
        }

        public String describe(int total, int matching) {
            return String.format(String.format("%s: %s", states, matching));
        }

        public boolean count(U element) {
            return states.contains(element.getState());
        }

    }

    public static class AndFilter<U> implements Filter<U> {

        private final List<Filter<U>> filters = new ArrayList<Filter<U>>();
        private final String message;

        public AndFilter(final String message, final Filter<U>... filters) {
            this.filters.addAll(Arrays.asList(filters));
            this.message = message;
        }

        public String describe(int total, int matching) {
            return String.format(String.format(message, matching));
        }

        public boolean count(U element) {
            for (Filter<U> filter : filters) {
                if (!filter.count(element)) {
                    return false;
                }
            }
            return true;
        }

    }

}
