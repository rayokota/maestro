package com.yammer.maestro.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.sun.jersey.api.NotFoundException;
import com.yammer.maestro.daos.OrchestrationDAO;
import com.yammer.maestro.daos.ProcessDAO;
import com.yammer.maestro.engine.OrchestrationEngine;
import com.yammer.maestro.models.Orchestration;
import com.yammer.maestro.models.OrchestrationState;
import com.yammer.maestro.models.OutboundEndpoint;
import com.yammer.maestro.models.Process;
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

@Path("/processes")
public class ProcessResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessResource.class);

    private final ProcessDAO dao;

    public ProcessResource(ProcessDAO dao) {
        this.dao = dao;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    @UnitOfWork
    public Process create(Process entity) {
        return dao.save(entity);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Set<Process> getAll() {
        return Sets.newLinkedHashSet(dao.findAll());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Process get(@PathParam("id") LongParam id) {
        Optional<Process> entity = dao.find(id.get());
        if (!entity.isPresent()) {
            throw new NotFoundException("Process " + id.get() + " not found");
        }
        return entity.get();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    @UnitOfWork
    public Process update(@PathParam("id") LongParam id, Process entity) {
        Optional<Process> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("Process " + id.get() + " not found");
        }
        return dao.merge(entity);
    }

    @DELETE
    @Path("{id}")
    @Timed
    @UnitOfWork
    public void delete(@PathParam("id") LongParam id) {
        Optional<Process> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("Process " + id.get() + " not found");
        }
        Process entity = ent.get();
        dao.delete(entity);
    }
}
