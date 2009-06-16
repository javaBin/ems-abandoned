package no.java.ems.service;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Unauthorized");
    }
}
