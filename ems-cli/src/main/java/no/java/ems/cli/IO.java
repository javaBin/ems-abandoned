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
