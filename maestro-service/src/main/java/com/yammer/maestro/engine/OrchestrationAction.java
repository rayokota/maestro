package com.yammer.maestro.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestrationAction implements OrchestrationEngineAwareCallable<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(OrchestrationAction.class);

    private final OrchestrationActionType actionType;
    private final String contextPath;
    private transient OrchestrationEngine engine;

    public OrchestrationAction(OrchestrationActionType actionType, String contextPath) {
        this.actionType = actionType;
        this.contextPath = contextPath;
    }

    @Override
    public void setOrchestrationEngine(OrchestrationEngine engine) {
        this.engine = engine;
    }

    @Override
    public Boolean call() throws Exception {
        switch (actionType) {
            case START:
                LOG.info("Remote starting orchestration, context path: {}", contextPath);
                return engine.doStart(contextPath);
            case STOP:
                LOG.info("Remote stopping orchestration, context path: {}", contextPath);
                return engine.doStop(contextPath);
            default:
                throw new IllegalArgumentException("Illegal action " + actionType);
        }
    }
}
