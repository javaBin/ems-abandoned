package no.java.ems.server.restlet.filter;

import no.java.ems.domain.Person;
import no.java.ems.server.restlet.PersonResource;
import org.restlet.Context;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PersonFilter extends AbstractObjectFilter<Person> {
    public PersonFilter(Context context, Class<PersonResource> resourceType) {
        super(context, resourceType);
    }

    protected boolean allowAccess(String id) {
        return true;
    }

    protected boolean allowAccess(Person person) {
        /*
         * Always returns true as the only requirent is that the user is logged in which is handled by the super class
         */

        return true;
    }
}
