package com.yammer.maestro.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/test")
public class TestResource {

    private static final Logger LOG = LoggerFactory.getLogger(TestResource.class);

    private int counter = 0;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    public Test create(Test entity) {
        LOG.info("Create entity " + entity.getName());
        return new Test(++counter, entity.getName());
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
    public Test update(@PathParam("id") LongParam id, Test entity) {
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
