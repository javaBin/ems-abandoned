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

package no.java.ems.cli;

import fj.P1;
import fj.F;
import fj.data.Either;
import static fj.data.Either.right;
import static fj.data.Either.left;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IO {
    public static F<File, Either<Exception, InputStream>> open = new F<File, Either<Exception, InputStream>>() {
        public Either<Exception, InputStream> f(File file) {
            try {
                return right((InputStream) new FileInputStream(file));
            } catch (Exception e) {
                return left(e);
            }
        }
    };

    public static <A> Either<? extends IOException, P1<A>> runIo(File file, F<InputStream, P1<A>> f) {
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(file);
            return right(f.f(inputStream));
        } catch (IOException e) {
            return left(e);
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
