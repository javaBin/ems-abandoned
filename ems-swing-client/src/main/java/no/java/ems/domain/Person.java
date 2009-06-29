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

package no.java.ems.domain;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class Person extends AbstractEntity implements Comparable<Person> {

    public enum Gender {

        Male,
        Female,

    }

    private String name;
    private String description;
    private Gender gender = Gender.Male;
    private LocalDate birthdate;
    private Language language;
    private Nationality nationality;
    private List<EmailAddress> emailAddresses = new ArrayList<EmailAddress>();
    private Binary photo;

    public Person() {
    }

    public Person(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        firePropertyChange("name", this.name, this.name = name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        firePropertyChange("description", this.description, this.description = description);
    }

    public Gender getGender() {
        return gender;
    }

    /**
     * @throws IllegalArgumentException if gender is {@code null}.
     */
    public void setGender(final Gender gender) {
        Validate.notNull(gender, "Gender may not be null");
        firePropertyChange("gender", this.gender, this.gender = gender);
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(final LocalDate birthdate) {
        firePropertyChange("birthdate", this.birthdate, this.birthdate = birthdate);
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(final Language language) {
        firePropertyChange("language", this.language, this.language = language);
    }

    public Nationality getNationality() {
        return nationality;
    }

    public void setNationality(final Nationality nationality) {
        firePropertyChange("nationality", this.nationality, this.nationality = nationality);
    }

    public List<EmailAddress> getEmailAddresses() {
        return Collections.unmodifiableList(emailAddresses);
    }

    public void setEmailAddresses(final List<EmailAddress> emailAddresses) {
        firePropertyChange("emailAddresses", getEmailAddresses(), Collections.unmodifiableList(this.emailAddresses = new ArrayList<EmailAddress>(emailAddresses)));
    }

    public Binary getPhoto() {
        return photo;
    }

    public void setPhoto(final Binary photo) {
        firePropertyChange("photo", this.photo, this.photo = photo);
    }

    public String getEmailAddressesAsString(final String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (EmailAddress emailAddress : emailAddresses) {
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(emailAddress.getEmailAddress());
        }
        return builder.toString();
    }

    /**
     * Compares based on the persons name.
     */
    public int compareTo(final Person other) {
        return new CompareToBuilder().append(name, other == null ? null : other.getName()).toComparison();
    }

    public void sync(final Person other) {
        super.sync(other);
        setName(other.getName());
        setDescription(other.getDescription());
        setGender(other.getGender());
        setBirthdate(other.getBirthdate());
        setLanguage(other.getLanguage());
        setNationality(other.getNationality());
        setEmailAddresses(other.getEmailAddresses());
        setPhoto(other.getPhoto());
    }

}
