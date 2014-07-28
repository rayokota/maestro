package com.yammer.maestro.client;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.util.Duration;

import java.net.URI;
import java.util.concurrent.Executors;

public class MaestroClient {

    private final Client client;
    private final JerseyClientBuilder clientBuilder;
    private final URI root;

    public MaestroClient(Client client, URI root) {
        this.client = client;
        this.clientBuilder = null;
        this.root = root;
    }

    public MaestroClient(JerseyClientBuilder clientBuilder, URI root) {
        this.client = null;
        this.clientBuilder = clientBuilder;
        this.root = root;
    }

    public String getExample() {
        return getClient().resource(root).path("/v1/example/yo").queryParam("bar", "dude").get(String.class);
    }

    private Client getClient() {
        return client != null ? client : buildClient(clientBuilder);
    }

    private static Client buildClient(JerseyClientBuilder clientBuilder) {
        final JerseyClientConfiguration jerseyConfig = new JerseyClientConfiguration();
        jerseyConfig.setConnectionTimeout(Duration.milliseconds(500));
        jerseyConfig.setKeepAlive(Duration.minutes(5));
        jerseyConfig.setMaxConnections(2048);
        jerseyConfig.setMaxConnectionsPerRoute(2048);
        jerseyConfig.setTimeout(Duration.hours(1));
        jerseyConfig.setTimeToLive(Duration.minutes(5));

        final JerseyClientBuilder builder = clientBuilder != null ? clientBuilder : new JerseyClientBuilder(new MetricRegistry());

        return builder
            .using(Executors.newSingleThreadExecutor(), new ObjectMapper())
            .using(jerseyConfig).build("maestroClient");
    }
}
