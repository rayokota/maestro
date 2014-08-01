package com.yammer.maestro.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.envers.RevisionType;
import org.joda.time.LocalDateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public abstract class AuditedEntity {
    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Transient
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
    private int revisionId;

    @Transient
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private LocalDateTime revisionDate;

    @Transient
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private RevisionType revisionType;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public int getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(int revisionId) {
        this.revisionId = revisionId;
    }

    public LocalDateTime getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(LocalDateTime revisionDate) {
        this.revisionDate = revisionDate;
    }

    public RevisionType getRevisionType() {
        return revisionType;
    }

    public void setRevisionType(RevisionType revisionType) {
        this.revisionType = revisionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditedEntity)) return false;
        AuditedEntity that = (AuditedEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
