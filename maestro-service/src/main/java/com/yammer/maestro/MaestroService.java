package com.yammer.maestro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yammer.maestro.cluster.Cluster;
import com.yammer.maestro.cluster.ClusterHealthCheck;
import com.yammer.maestro.config.MaestroConfiguration;
import com.yammer.maestro.daos.OrchestrationDAO;
import com.yammer.maestro.daos.OutboundEndpointDAO;
import com.yammer.maestro.engine.OrchestrationEngine;
import com.yammer.maestro.models.RdbmsOutboundEndpoint;
import com.yammer.maestro.models.HttpOutboundEndpoint;
import com.yammer.maestro.models.Orchestration;
import com.yammer.maestro.models.OutboundEndpoint;
import com.yammer.maestro.proxy.OrchestrationProxyServlet;
import com.yammer.maestro.resources.OrchestrationResource;
import com.yammer.maestro.resources.OutboundEndpointResource;
import com.yammer.maestro.resources.TestResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaestroService extends Application<MaestroConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(MaestroService.class);

    public static void main(String[] args) throws Exception {
        new MaestroService().run(args);
    }

    private final HibernateBundle<MaestroConfiguration> hibernateBundle = new HibernateBundle<MaestroConfiguration>(
            Orchestration.class,
            OutboundEndpoint.class,
            RdbmsOutboundEndpoint.class,
            HttpOutboundEndpoint.class
        ) {
        @Override
        public DataSourceFactory getDataSourceFactory(MaestroConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public String getName() {
        return "maestro";
    }

    @Override
    public void initialize(Bootstrap<MaestroConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/app/", "/", "index.html"));
        bootstrap.addBundle(hibernateBundle);
        ObjectMapper mapper = bootstrap.getObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        initializeMule();
    }

    private void initializeMule() {
        // set mule.home so that Mule is running in standalone mode
        // see https://www.mulesoft.org/docs/site/3.5.0/apidocs/org/mule/api/config/MuleConfiguration.html
        // this will prevent the HTTP connection manager from being shutdown
        // when a muleContext is disposed
        System.setProperty("mule.home", System.getProperty("user.dir"));
    }

    @Override
    public void run(MaestroConfiguration configuration,
                    Environment environment) throws Exception {

        final SessionFactory sessionFactory = hibernateBundle.getSessionFactory();
        final OrchestrationDAO orchestrationDAO = new OrchestrationDAO(sessionFactory);
        final OutboundEndpointDAO outboundEndpointDAO = new OutboundEndpointDAO(sessionFactory);
        final Cluster cluster = buildCluster(configuration, environment);
        final OrchestrationEngine engine = buildEngine(configuration, environment, orchestrationDAO, cluster);
        final OrchestrationProxyServlet servlet = new OrchestrationProxyServlet(configuration, orchestrationDAO);

        environment.servlets().addServlet("proxy", servlet).addMapping(configuration.getRootPath() + "/*");

        environment.jersey().setUrlPattern("/maestro/*");
        
        environment.jersey().register(new TestResource());
        environment.jersey().register(new OrchestrationResource(engine, orchestrationDAO));
        environment.jersey().register(new OutboundEndpointResource(orchestrationDAO, outboundEndpointDAO));
    }

    private Cluster buildCluster(MaestroConfiguration configuration, Environment environment) {
        final Cluster cluster = new Cluster(configuration.getClusterConfiguration(), environment.metrics());
        environment.lifecycle().manage(cluster);
        environment.healthChecks().register("cluster",
                new ClusterHealthCheck(cluster, configuration.getClusterConfiguration().getMembers().size(), environment.metrics()));
        return cluster;
    }

    private OrchestrationEngine buildEngine(MaestroConfiguration configuration, Environment environment,
                                            OrchestrationDAO dao, Cluster cluster) {
        final OrchestrationEngine engine = new OrchestrationEngine(configuration, dao, cluster);
        environment.lifecycle().manage(engine);
        return engine;
    }
}
