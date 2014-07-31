package com.yammer.maestro.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.sun.jersey.api.NotFoundException;
import com.yammer.maestro.daos.OrchestrationDAO;
import com.yammer.maestro.engine.OrchestrationEngine;
import com.yammer.maestro.models.Orchestration;
import com.yammer.maestro.models.OrchestrationState;
import com.yammer.maestro.models.OutboundEndpoint;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/orchestrations")
public class OrchestrationResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrchestrationResource.class);

    private final OrchestrationEngine engine;
    private final OrchestrationDAO dao;

    public OrchestrationResource(OrchestrationEngine engine, OrchestrationDAO dao) {
        this.engine = engine;
        this.dao = dao;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    @UnitOfWork
    public Orchestration create(Orchestration entity) {
        entity.setState(OrchestrationState.Initialized);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreated(now);
        entity.setLastModified(now);
        return dao.save(entity);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Set<Orchestration> getAll() {
        return Sets.newLinkedHashSet(dao.findAll());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Orchestration get(@PathParam("id") LongParam id) {
        Optional<Orchestration> entity = dao.find(id.get());
        if (!entity.isPresent()) {
            throw new NotFoundException("Orchestration " + id.get() + " not found");
        }
        return entity.get();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    @UnitOfWork
    public Orchestration update(@PathParam("id") LongParam id, Orchestration entity) {
        Optional<Orchestration> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("Orchestration " + id.get() + " not found");
        }
        if (ent.get().getState() == OrchestrationState.Started) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        entity.setLastModified(LocalDateTime.now());
        return dao.merge(entity);
    }

    @DELETE
    @Path("{id}")
    @Timed
    @UnitOfWork
    public void delete(@PathParam("id") LongParam id) {
        Optional<Orchestration> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("Orchestration " + id.get() + " not found");
        }
        Orchestration entity = ent.get();
        if (entity.getState() == OrchestrationState.Started) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        dao.delete(entity);
    }

    @POST
    @Path("{id}/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Orchestration start(@PathParam("id") LongParam id) {
        Optional<Orchestration> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("Orchestration " + id.get() + " not found");
        }
        Orchestration entity = ent.get();
        boolean result = engine.start(entity);
        if (result) {
            entity.setState(OrchestrationState.Started);
            entity.setLastModified(LocalDateTime.now());
            dao.save(entity);
        }
        return entity;
    }

    @POST
    @Path("{id}/stop")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Orchestration stop(@PathParam("id") LongParam id) {
        Optional<Orchestration> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("Orchestration " + id.get() + " not found");
        }
        Orchestration entity = ent.get();
        boolean result = engine.stop(entity);
        if (result) {
            entity.setState(OrchestrationState.Stopped);
            entity.setLastModified(LocalDateTime.now());
            dao.save(entity);
        }
        return entity;
    }

    @GET
    @Path("{id}/specification")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public String getSpecification(@PathParam("id") LongParam id) throws IOException {
        Optional<Orchestration> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("Orchestration " + id.get() + " not found");
        }
        return engine.getGeneratedSpecification(ent.get());
    }

    @GET
    @Path("{id}/revisionNumbers")
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
    public List<Orchestration> getRevisions(@PathParam("id") LongParam id) {
        List<Orchestration> orchestrations = dao.getRevisions(id.get());
        // eagerly load the related objects
        for (Orchestration orchestration : orchestrations) {
            for (OutboundEndpoint outboundEndpoint : orchestration.getOutboundEndpoints()) {
                Map<String, String> properties = outboundEndpoint.getProperties();
                for (Map.Entry<String, String> property : properties.entrySet()) {
                    property.getValue();
                }
            }
        }
        return orchestrations;
    }

    @GET
    @Path("{id}/revisions/{revisionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Orchestration getRevision(@PathParam("id") LongParam id, @PathParam("revisionId") IntParam revisionId) {
        Orchestration orchestration = dao.getRevision(id.get(), revisionId.get());
        // eagerly load the related objects
        for (OutboundEndpoint outboundEndpoint : orchestration.getOutboundEndpoints()) {
            Map<String, String> properties = outboundEndpoint.getProperties();
            for (Map.Entry<String, String> property : properties.entrySet()) {
                property.getValue();
            }
        }
        return orchestration;
    }
}
