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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LoggingWikiSink {
    public static <T extends WikiSink> T wrap(T sink) {
        return (T) Proxy.newProxyInstance(WikiSink.class.getClassLoader(),
            new Class<?>[]{WikiSink.class}, new LoggingInvocationHandler(sink));
    }

    private static class LoggingInvocationHandler<T extends WikiSink> implements InvocationHandler {
        private T target;

        public LoggingInvocationHandler(T target) {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == WikiSink.class) {
                System.out.println(method.getName());
            }
            return method.invoke(target, args);
        }
    }
}
