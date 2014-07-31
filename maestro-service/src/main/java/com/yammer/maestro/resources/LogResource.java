package com.yammer.maestro.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.sun.jersey.api.NotFoundException;
import com.yammer.maestro.daos.LogDAO;
import com.yammer.maestro.models.Log;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/logs")
public class LogResource {

    private static final Logger LOG = LoggerFactory.getLogger(LogResource.class);

    private final LogDAO dao;

    public LogResource(LogDAO dao) {
        this.dao = dao;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    @UnitOfWork
    public Log create(Log entity) {
        return dao.save(entity);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Set<Log> getAll() {
        return Sets.newLinkedHashSet(dao.findAll());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Log get(@PathParam("id") LongParam id) {
        Optional<Log> entity = dao.find(id.get());
        if (!entity.isPresent()) {
            throw new NotFoundException("Log " + id.get() + " not found");
        }
        return entity.get();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed 
    @UnitOfWork
    public Log update(@PathParam("id") LongParam id, Log entity) {
        Optional<Log> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("Log " + id.get() + " not found");
        }
        return dao.merge(entity);
    }

    @DELETE
    @Path("{id}")
    @Timed
    @UnitOfWork
    public void delete(@PathParam("id") LongParam id) {
        Optional<Log> ent = dao.find(id.get());
        if (!ent.isPresent()) {
            throw new NotFoundException("Log " + id.get() + " not found");
        }
        Log entity = ent.get();
        dao.delete(entity);
    }
}
