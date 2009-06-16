package no.java.ems.client;

import no.java.ems.domain.Person;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.Client;

import java.util.List;

public class PeopleClient extends AbstractClient<Person> {

    private static final String PEOPLE = "/people/";

    PeopleClient(String baseUri, Client client) {
        super(baseUri, client);
    }

    public List<Person> listPeople() {
        Request request = new Request(Method.GET, baseUri + PEOPLE);
        Response response = doRequest(request);
        //noinspection unchecked
        return deserialize(response, List.class);
    }

    public Person get(String id) {
        return deserialize(doRequest(new Request(Method.GET, baseUri + PEOPLE + id)), Person.class);
    }

    public Person getPerson(String personId) {
        return get(personId);
    }

    public void createPerson(Person person) {
        Request request = new Request(Method.POST, baseUri + PEOPLE);
        request.setEntity(new ObjectRepresentation(person));
        Response response = doRequest(request);
        person.setId(extractId(response));
    }

    public void updatePerson(Person person) {
        Request request = new Request(Method.PUT, baseUri + PEOPLE + person.getId());
        request.setEntity(new ObjectRepresentation(person));
        doRequest(request);
    }

    public void deletePerson(Person person) {
        Request request = new Request(Method.DELETE, baseUri + PEOPLE + person.getId());
        doRequest(request);
    }
}
