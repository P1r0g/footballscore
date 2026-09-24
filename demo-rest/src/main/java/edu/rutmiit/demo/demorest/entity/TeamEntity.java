package edu.rutmiit.demo.demorest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

@Entity
@Table(name = "teams")
public class TeamEntity {

    @Id
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "coach", nullable = false, length = 200)
    private String coach;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected TeamEntity() {
    }

    public TeamEntity(UUID uuid, Long id, String name, String country, String coach) {
        this.uuid = uuid;
        this.id = id;
        this.name = name;
        this.country = country;
        this.coach = coach;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCoach() {
        return coach;
    }

    public long getVersion() {
        return version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCoach(String coach) {
        this.coach = coach;
    }
}