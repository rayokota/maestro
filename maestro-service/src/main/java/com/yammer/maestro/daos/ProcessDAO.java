package com.yammer.maestro.daos;

import com.google.common.base.Optional;
import com.yammer.maestro.models.Process;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

import java.util.List;

/**
 * A DAO for managing {@link com.yammer.maestro.models.Orchestration} objects.
 */
public class ProcessDAO extends AbstractDAO<Process> {

    protected final SessionFactory sessionFactory;

    /**
     * Creates a new DAO with the given session provider.
     *
     * @param provider a session provider
     */
    public ProcessDAO(SessionFactory provider) {
        super(provider);
        this.sessionFactory = provider;  // save the session factory since it's private in the superclass
    }

    /**
     * Returns the {@link com.yammer.maestro.models.Process} with the given ID.
     *
     * @param id the entity ID
     * @return the entity with the given ID
     */
    public Optional<Process> find(long id) {
        return Optional.fromNullable(get(id));
    }

    /**
    /**
     * Returns all {@link com.yammer.maestro.models.Process} entities, ordered by name.
     *
     * @return the list of entities
     */
    public List<Process> findAll() {
        return (List<Process>) criteria().list();
    }

    /**
     * Saves the given {@link com.yammer.maestro.models.Process}.
     *
     * @param entity the entity to save
     * @return the persistent entity
     */
    public Process save(Process entity) throws HibernateException {
        boolean ownsSession = false;
        Session session = null;
        Transaction txn = null;
        try {
            session = currentSession();
        } catch (HibernateException ex) {
            session = sessionFactory.openSession();
            txn = session.beginTransaction();
            ownsSession = true;
        }
        try {
            session.saveOrUpdate(entity);
        } catch (Exception ex) {
            if (ownsSession) {
                txn.rollback();
            }
        } finally {
            if (ownsSession) {
                txn.commit();
                session.close();
            }
        }
        return entity;
    }

    /**
     * Merges the given {@link com.yammer.maestro.models.Process}.
     *
     * @param entity the entity to merge
     * @return the persistent entity
     */
    public Process merge(Process entity) throws HibernateException {
        Process process = null;
        boolean ownsSession = false;
        Session session = null;
        Transaction txn = null;
        try {
            session = currentSession();
        } catch (HibernateException ex) {
            session = sessionFactory.openSession();
            txn = session.beginTransaction();
            ownsSession = true;
        }
        try {
            process = (Process) session.merge(entity);
        } catch (Exception ex) {
            if (ownsSession) {
                txn.rollback();
            }
        } finally {
            if (ownsSession) {
                txn.commit();
                session.close();
            }
        }
        return process;
    }

    /**
     * Deletes the given {@link com.yammer.maestro.models.Process}.
     *
     * @param entity the entity to delete
     */
    public void delete(Process entity) throws HibernateException {
        currentSession().delete(entity);
    }
}
