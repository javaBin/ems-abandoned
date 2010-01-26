package no.java.ems.domain;

import org.apache.commons.lang.Validate;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Speaker extends AbstractEntity {

    private final String personId;
    private final String name;
    private String description;
    private Binary photo;

    /**
     * @param personId person identifer. May not be {@code null}.
     * @param name the name of the person. May not be null.
     * @throws IllegalArgumentException if the person identifier is {@code null}.
     */
    public Speaker(final String personId, final String name) {
        Validate.notNull(personId, "Person identifier may not be null.");
        Validate.notNull(name, "Person name may not be null.");
        this.name = name;
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        firePropertyChange("description", this.description, this.description = description);
    }

    public Binary getPhoto() {
        return photo;
    }

    public void setPhoto(final Binary photo) {
        firePropertyChange("photo", this.photo, this.photo = photo);
    }

    public String getName() {
        return name;
    }
}
