package com.yammer.maestro.views;

import com.yammer.maestro.config.MaestroConfiguration;
import com.yammer.maestro.models.Orchestration;
import io.dropwizard.views.View;

public class OrchestrationView extends View {

    private final MaestroConfiguration configuration;
    private final Orchestration orchestration;

    public OrchestrationView(MaestroConfiguration configuration, Orchestration orchestration) {
        super("orchestration.ftl");
        this.configuration = configuration;
        this.orchestration = orchestration;
    }

    public MaestroConfiguration getConfiguration() {
        return configuration;
    }

    public Orchestration getOrchestration() {
        return orchestration;
    }
}
