package com.yammer.maestro.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ClusterConfiguration {
    @Min(1024)
    @Max(65535)
    @JsonProperty
    private int port = 5701;

    @JsonProperty
    @NotNull
    private ImmutableSet<String> members = ImmutableSet.of();

    @NotBlank
    @JsonProperty
    private String name = null;

    public int getPort() {
        return port;
    }

    public ImmutableSet<String> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }
}
