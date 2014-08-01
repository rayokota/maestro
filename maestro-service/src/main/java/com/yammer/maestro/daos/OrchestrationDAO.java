package com.yammer.maestro.daos;

import com.google.common.base.Optional;
import com.yammer.maestro.models.Orchestration;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;

import java.util.List;

/**
 * A DAO for managing {@link Orchestration} objects.
 */
public class OrchestrationDAO extends AbstractAuditedDAO<Orchestration> {

    /**
     * Creates a new DAO with the given session provider.
     *
     * @param provider a session provider
     */
    public OrchestrationDAO(SessionFactory provider) {
        super(provider);
    }

    /**
     * Returns the {@link Orchestration} with the given ID.
     *
     * @param id the entity ID
     * @return the entity with the given ID
     */
    public Optional<Orchestration> find(long id) {
        return Optional.fromNullable(get(id));
    }

    /**
     * Returns the {@link Orchestration} with the given contextPath.
     *
     * @param contextPath the context path
     * @return the entity with the given ID
     */
    public Optional<Orchestration> findByContextPath(String contextPath) {
        Orchestration orchestration = null;
        boolean ownsSession = false;
        Session session = null;
        try {
            session = currentSession();
        } catch (HibernateException ex) {
            session = sessionFactory.openSession();
            ownsSession = true;
        }
        try {
            orchestration = (Orchestration)session.bySimpleNaturalId(Orchestration.class).load(contextPath);
        } finally {
            if (ownsSession) session.close();
        }
        return Optional.fromNullable(orchestration);
    }

    /**
    /**
     * Returns all {@link Orchestration} entities, ordered by name.
     *
     * @return the list of entities
     */
    public List<Orchestration> findAll() {
        List<Orchestration> orchestrations = null;
        boolean ownsSession = false;
        Session session = null;
        try {
            session = currentSession();
        } catch (HibernateException ex) {
            session = sessionFactory.openSession();
            ownsSession = true;
        }
        try {
            orchestrations = (List<Orchestration>) session.createCriteria(getEntityClass()).addOrder(Order.asc("name")).list();
        } finally {
            if (ownsSession) session.close();
        }
        return orchestrations;
    }

    /**
     * Saves the given {@link Orchestration}.
     *
     * @param entity the entity to save
     * @return the persistent entity
     */
    public Orchestration save(Orchestration entity) throws HibernateException {
        return persist(entity);
    }

    /**
     * Merges the given {@link Orchestration}.
     *
     * @param entity the entity to merge
     * @return the persistent entity
     */
    public Orchestration merge(Orchestration entity) throws HibernateException {
        return (Orchestration) currentSession().merge(entity);
    }

    /**
     * Deletes the given {@link Orchestration}.
     *
     * @param entity the entity to delete
     */
    public void delete(Orchestration entity) throws HibernateException {
        currentSession().delete(entity);
    }
}
