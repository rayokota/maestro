package com.yammer.maestro.engine;

import java.io.Serializable;
import java.util.concurrent.Callable;

public interface OrchestrationEngineAwareCallable<T> extends Callable<T>, Serializable {
    void setOrchestrationEngine(OrchestrationEngine engine);
}
