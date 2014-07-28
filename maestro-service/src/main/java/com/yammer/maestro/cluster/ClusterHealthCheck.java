package com.yammer.maestro.cluster;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.HealthCheck;

import java.util.concurrent.atomic.AtomicInteger;

public class ClusterHealthCheck extends HealthCheck {
    private final Cluster cluster;
    private final int clusterSize;
    private MetricRegistry metrics;
    private final Timer clusterCheckTimer;
    private final AtomicInteger lastHealthCheckResult = new AtomicInteger();

    public ClusterHealthCheck(Cluster c, int clusterSize, MetricRegistry metrics) {
        this.cluster = c;
        this.clusterSize = clusterSize;
        this.metrics = metrics;

        this.clusterCheckTimer = metrics.timer(MetricRegistry.name(ClusterHealthCheck.class, "cluster-health-check"));

        metrics.register(MetricRegistry.name(ClusterHealthCheck.class, "cluster-health-check-state"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return lastHealthCheckResult.get();
            }
        });
        metrics.register(MetricRegistry.name(ClusterHealthCheck.class, "cluster-live-state"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return cluster.isRunning() ? 1 : 0;
            }
        });
    }

    @Override
    protected Result check() throws Exception {
        Timer.Context timerContext = clusterCheckTimer.time();
        boolean isRunning = cluster.isRunning();

        lastHealthCheckResult.set(isRunning ? 1 : 0);

        timerContext.stop();
        if (isRunning) {
            int actualSize = cluster.getMembers().size();
            if (actualSize != clusterSize) {
                return Result.unhealthy(String.format("Cluster size expected %d actual %d", clusterSize, actualSize));
            }
            return Result.healthy();
        }
        return Result.unhealthy("Not running");
    }
}
