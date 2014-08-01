package com.yammer.maestro.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.sun.jersey.api.NotFoundException;
import com.yammer.maestro.daos.OrchestrationDAO;
import com.yammer.maestro.daos.OutboundEndpointDAO;
import com.yammer.maestro.models.OutboundEndpoint;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/outboundEndpoints")
public class OutboundEndpointResource {

    private static final Logger LOG = LoggerFactory.getLogger(OutboundEndpointResource.class);

    private final OrchestrationDAO orchestrationDao;
    private final OutboundEndpointDAO dao;

    public OutboundEndpointResource(OrchestrationDAO orchestrationDAO, OutboundEndpointDAO dao) {
        this.orchestrationDao = orchestrationDAO;
        this.dao = dao;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    @UnitOfWork
    public OutboundEndpoint create(OutboundEndpoint entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setOrchestration(orchestrationDao.find(entity.getOrchestrationId()).get());
        entity.setCreated(now);
        entity.setLastModified(now);
        return dao.save(entity);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Set<OutboundEndpoint> getAll() {
        return Sets.newLinkedHashSet(dao.findAll());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public OutboundEndpoint get(@PathParam("id") LongParam id) {
        Optional<OutboundEndpoint> entity = dao.find(id.get());
        if (!entity.isPresent()) {
            throw new NotFoundException("OutboundEndpoint " + id.get() + " not found");
        }
        return entity.get();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    @UnitOfWork
    public OutboundEndpoint update(@PathParam("id") LongParam id, OutboundEndpoint entity) {
        Optional<OutboundEndpoint> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("OutboundEndpoint " + id.get() + " not found");
        }
        entity.setOrchestration(orchestrationDao.find(entity.getOrchestrationId()).get());
        entity.setLastModified(LocalDateTime.now());
        return dao.merge(entity);
    }

    @DELETE
    @Path("{id}")
    @Timed
    @UnitOfWork
    public void delete(@PathParam("id") LongParam id) {
        Optional<OutboundEndpoint> entity = dao.find(id.get());
        if (!entity.isPresent()) {
            throw new NotFoundException("OutboundEndpoint " + id.get() + " not found");
        }
        dao.delete(entity.get());
    }

    @GET
    @Path("{id}/revisionIds")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public List<Number> getRevisionNumbers(@PathParam("id") LongParam id) {
        return dao.getRevisionNumbers(id.get());
    }

    @GET
    @Path("{id}/revisions")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public List<OutboundEndpoint> getRevisions(@PathParam("id") LongParam id) {
        List<OutboundEndpoint> outboundEndpoints = dao.getRevisions(id.get());
        // eagerly load the related objects
        for (OutboundEndpoint outboundEndpoint : outboundEndpoints) {
            Map<String, String> properties = outboundEndpoint.getProperties();
            for (Map.Entry<String, String> property : properties.entrySet()) {
                property.getValue();
            }
        }
        return outboundEndpoints;
    }

    @GET
    @Path("{id}/revisions/{revisionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public OutboundEndpoint getRevision(@PathParam("id") LongParam id, @PathParam("revisionId") IntParam revisionId) {
        OutboundEndpoint outboundEndpoint = dao.getRevision(id.get(), revisionId.get());
        // eagerly load the related objects
        Map<String, String> properties = outboundEndpoint.getProperties();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            property.getValue();
        }
        return outboundEndpoint;
    }
}
