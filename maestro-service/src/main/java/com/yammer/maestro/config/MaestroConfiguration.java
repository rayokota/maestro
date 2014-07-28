package com.yammer.maestro.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class MaestroConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private String rootPath = "/api";

    @Valid
    @JsonProperty
    private int basePort = 9000;

    @Valid
    @NotNull
    @JsonProperty
    private ClusterConfiguration cluster = new ClusterConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    public String getRootPath() {
        return rootPath;
    }

    public int getBasePort() {
        return basePort;
    }
    public ClusterConfiguration getClusterConfiguration() {
        return cluster;
    }

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    public void setDataSourceFactory(DataSourceFactory database) {
        this.database = database;
    }
}
