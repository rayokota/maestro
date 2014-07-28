package com.yammer.maestro.cluster;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.yammer.maestro.engine.OrchestrationEngine;
import com.yammer.maestro.engine.OrchestrationEngineAwareCallable;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class ClusterAwareCallable<T> implements HazelcastInstanceAware, Callable<T>, Serializable {
    private HazelcastInstance instance;
    private OrchestrationEngineAwareCallable<T> callable;

    public ClusterAwareCallable(OrchestrationEngineAwareCallable<T> callable) {
        this.callable = callable;
    }

    public void setHazelcastInstance(HazelcastInstance instance) {
        this.instance = instance;
    }

    public T call() throws Exception {
        callable.setOrchestrationEngine((OrchestrationEngine)instance.getUserContext().get(OrchestrationEngine.ENGINE_KEY));
        return callable.call();
    }
}
