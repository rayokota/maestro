package com.yammer.maestro.daos;

import com.google.common.base.Optional;
import com.yammer.maestro.models.OutboundEndpoint;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;

import java.util.List;

/**
 * A DAO for managing {@link com.yammer.maestro.models.OutboundEndpoint} objects.
 */
public class OutboundEndpointDAO extends AbstractDAO<OutboundEndpoint> {

    /**
     * Creates a new DAO with the given session provider.
     *
     * @param provider a session provider
     */
    public OutboundEndpointDAO(SessionFactory provider) {
        super(provider);
    }

    /**
     * Returns the {@link com.yammer.maestro.models.OutboundEndpoint} with the given ID.
     *
     * @param id the entity ID
     * @return the entity with the given ID
     */
    public Optional<OutboundEndpoint> find(long id) {
        return Optional.fromNullable(get(id));
    }

    /**
     * Returns all {@link com.yammer.maestro.models.OutboundEndpoint} entities.
     *
     * @return the list of entities
     */
    public List<OutboundEndpoint> findAll() {
        return (List<OutboundEndpoint>) criteria().addOrder(Order.asc("id")).list();
    }

    /**
     * Saves the given {@link com.yammer.maestro.models.OutboundEndpoint}.
     *
     * @param entity the entity to save
     * @return the persistent entity
     */
    public OutboundEndpoint save(OutboundEndpoint entity) throws HibernateException {
        return persist(entity);
    }

    /**
     * Merges the given {@link com.yammer.maestro.models.OutboundEndpoint}.
     *
     * @param entity the entity to merge
     * @return the persistent entity
     */
    public OutboundEndpoint merge(OutboundEndpoint entity) throws HibernateException {
        return (OutboundEndpoint) currentSession().merge(entity);
    }

    /**
     * Deletes the given {@link com.yammer.maestro.models.OutboundEndpoint}.
     *
     * @param entity the entity to delete
     */
    public void delete(OutboundEndpoint entity) throws HibernateException {
        currentSession().delete(entity);
    }
}
