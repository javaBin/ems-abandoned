package no.java.ems.server.domain;

/**
 * @author Trygve Laugstol
 */
public class Room extends AbstractEntity {
    private String name;

    private String description;

    public Room() {
    }

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
