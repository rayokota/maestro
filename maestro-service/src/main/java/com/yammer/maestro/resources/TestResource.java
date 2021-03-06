package com.yammer.maestro.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/test")
public class TestResource {

    private static final Logger LOG = LoggerFactory.getLogger(TestResource.class);

    private int counter = 0;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Test create(@Valid Test entity) {
        LOG.info("Create entity " + entity.getName());
        return new Test(++counter, entity.getName());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public List<Test> getAll(@DefaultValue("myEntity") @QueryParam("name") String name) {
        LOG.info("Get entities");
        List<Test> entities = Lists.newArrayList();
        for (int i = 0; i < 2; i++) {
            entities.add(new Test(i, name + i));
        }
        return entities;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Test get(@PathParam("id") LongParam id,
                    @DefaultValue("myEntity") @QueryParam("name") String name) {
        LOG.info("Get entity " + id.get());
        return new Test(id.get(), name);
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Test update(@PathParam("id") LongParam id, @Valid Test entity) {
        LOG.info("Update entity " + id.get());
        return new Test(id.get(), entity.getName());
    }

    @DELETE
    @Path("{id}")
    @Timed
    @UnitOfWork
    public void delete(@PathParam("id") LongParam id) {
        LOG.info("Delete entity " + id.get());
    }

    public static class Test {
        @JsonProperty
        @Min(1)
        protected long id;
        @JsonProperty
        protected String name;
        public Test() {
        }
        public Test(long id, String name) {
            this.id = id;
            this.name = name;
        }
        public long getId() {
            return id;
        }
        public String getName() {
            return name;
        }
    }
}
