package com.yammer.maestro.cluster;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.*;
import com.yammer.maestro.config.ClusterConfiguration;
import com.yammer.maestro.engine.OrchestrationEngineAwareCallable;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * A cluster of hosts to which messages can be broadcast.
 */
public class Cluster implements Managed {
    private static final Logger LOG = LoggerFactory.getLogger(Cluster.class);

    private final Config config;
    private final String name;
    private final Timer executeTimer;
    private final Meter executions;

    private HazelcastInstance instance;
    private IExecutorService executor;

    private MetricRegistry metrics;

    /**
     * Creates a new {@link Cluster} of the given hosts, listening on the given port, with the given
     * name.
     *
     * @param configuration the cluster configuration
     * @param metrics       metrics registry
     */
    public Cluster(ClusterConfiguration configuration,
                   MetricRegistry metrics) {
        this.config = new Config();
        final MulticastConfig multicast = new MulticastConfig().setEnabled(false);
        final TcpIpConfig tcpIp = new TcpIpConfig().setEnabled(true);
        for (String host : configuration.getMembers()) {
            tcpIp.addMember(host);
        }
        config.getNetworkConfig().setJoin(new JoinConfig().setMulticastConfig(multicast)
                .setTcpIpConfig(tcpIp)).setPort(configuration.getPort());
        config.setProperty("hazelcast.version.check.enabled", "false");

        this.name = configuration.getName();
        this.metrics = metrics;
        this.executeTimer = metrics.timer(MetricRegistry.name(Cluster.class, "execution"));
        this.executions = metrics.meter(MetricRegistry.name(Cluster.class, "executions"));
    }

    public void addUserContext(String name, Object object) {
        instance.getUserContext().put(name, object);
    }

    /**
     * Execute the given callable on all other hosts in the cluster, excepting the local host.
     *
     * @param callable    the callable
     */
    public <T> void execute(OrchestrationEngineAwareCallable<T> callable) {
        final Timer.Context context = executeTimer.time();
        try {
            executions.mark();
            Set<Member> members = Sets.newHashSet(instance.getCluster().getMembers());
            members.remove(instance.getCluster().getLocalMember());
            if (!members.isEmpty()) {
                executor.submitToMembers(new ClusterAwareCallable(callable), members,
                    new MultiExecutionCallback() {
                        public void onResponse(Member member, Object value) {
                        }

                        public void onComplete(Map<Member, Object> values) {
                        }
                    });
            }
        } finally {
            context.stop();
        }
    }

    /**
     * Connects to the rest of the cluster.
     *
     * @throws Exception if there is an error connecting to the cluster
     */
    @Override
    public void start() throws Exception {
        LOG.info("Starting cluster {}", name);
        this.instance = Hazelcast.newHazelcastInstance(config);
        this.executor = instance.getExecutorService(name);
        metrics.register(MetricRegistry.name(Cluster.class, "cluster-size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return getMembers().size();
            }
        });
    }

    /**
     * Disconnects from the cluster.
     *
     * @throws Exception if there is an error disconnecting from the cluster
     */
    @Override
    public void stop() throws Exception {
        LOG.info("Stopping cluster {}", name);
        instance.getLifecycleService().shutdown();
        this.instance = null;
        this.executor = null;
    }

    /**
     * Returns {@code true} if the cluster is connected.
     *
     * @return {@code true} if the cluster is connected
     */
    public boolean isRunning() {
        return instance != null && instance.getLifecycleService().isRunning();
    }

    /**
     * Returns the set of members in the cluster.
     *
     * @return the set of members in the cluster
     */
    public ImmutableSet<String> getMembers() {
        final ImmutableSet.Builder<String> members = ImmutableSet.builder();
        for (Member member : instance.getCluster().getMembers()) {
            members.add(member.toString());
        }
        return members.build();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("hosts", config.getNetworkConfig()
                        .getJoin()
                        .getTcpIpConfig()
                        .getMembers())
                .add("name", name)
                .add("running", isRunning())
                .toString();
    }
}
