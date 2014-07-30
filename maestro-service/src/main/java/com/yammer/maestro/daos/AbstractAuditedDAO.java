package com.yammer.maestro.daos;

import com.google.common.collect.Lists;
import com.yammer.maestro.models.AuditedEntity;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * A DAO for managing {@link org.hibernate.envers.Audited} objects.
 */
public abstract class AbstractAuditedDAO<E extends AuditedEntity> extends AbstractDAO<E> {

    protected final SessionFactory sessionFactory;

    /**
     * Creates a new DAO with the given session provider.
     *
     * @param provider a session provider
     */
    public AbstractAuditedDAO(SessionFactory provider) {
        super(provider);
        this.sessionFactory = provider;  // save the session factory since it's private in the superclass
    }

    /**
     * Returns the latest version number for the entity with the given ID.
     *
     * @param id the id of the entity
     */
    public Number getLatestRevisionNumber(long id) {
        AuditReader auditReader = AuditReaderFactory.get(currentSession());
        Number number = (Number) auditReader.createQuery()
                .forRevisionsOfEntity(getEntityClass(), true, true)
                .addProjection(AuditEntity.revisionNumber().max())
                .add(AuditEntity.id().eq(id)).getSingleResult();
        return number;
    }

    /**
     * Returns all version numbers for the entity with the given ID.
     *
     * @param id the id of the entity
     */
    public List<Number> getRevisionNumbers(long id) {
        AuditReader auditReader = AuditReaderFactory.get(currentSession());
        return auditReader.getRevisions(getEntityClass(), id);
    }

    /**
     * Returns the revisions of the entity with the given ID.
     *
     * @param id the id of the entity
     */
    public List<E> getRevisions(long id) {
        AuditReader auditReader = AuditReaderFactory.get(currentSession());
        List<Object[]> rows = auditReader.createQuery().forRevisionsOfEntity(getEntityClass(), false, true).getResultList();
        List<E> result = Lists.newArrayListWithExpectedSize(rows.size());
        for (Object[] row : rows) {
            E entity = (E)row[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity)row[1];
            RevisionType revisionType = (RevisionType)row[2];

            entity.setRevisionId(revisionEntity.getId());
            entity.setRevisionDate(new LocalDateTime(revisionEntity.getTimestamp()));
            entity.setRevisionType(revisionType);

            result.add(entity);
        }
        return result;
    }

    /**
     * Returns a revision of the entity with the given ID and revision ID.
     *
     * @param id the id of the entity
     * @param revisionId the revision id of the entity
     */
    public E getRevision(long id, int revisionId) {
        AuditReader auditReader = AuditReaderFactory.get(currentSession());
        return auditReader.find(getEntityClass(), id, revisionId);
    }
}
