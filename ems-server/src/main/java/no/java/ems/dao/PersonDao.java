package no.java.ems.dao;

import no.java.ems.domain.Person;

import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public interface PersonDao {

    Person getPerson(final String id);

    List<Person> getPersons();

    void savePerson(final Person person);

    void deletePerson(final String id);

}
