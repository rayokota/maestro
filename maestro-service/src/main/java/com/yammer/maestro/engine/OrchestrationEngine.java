package com.yammer.maestro.engine;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.yammer.maestro.cluster.Cluster;
import com.yammer.maestro.config.MaestroConfiguration;
import com.yammer.maestro.daos.OrchestrationDAO;
import com.yammer.maestro.daos.ProcessDAO;
import com.yammer.maestro.models.Orchestration;
import com.yammer.maestro.models.OrchestrationType;
import com.yammer.maestro.views.OrchestrationView;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;
import org.mule.api.MuleContext;
import org.mule.config.ConfigResource;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class OrchestrationEngine implements Managed {
    private static final Logger LOG = LoggerFactory.getLogger(OrchestrationEngine.class);

    public static final String ORCH_ENGINE_KEY = "orchEngine";

    private final MaestroConfiguration configuration;
    private final OrchestrationDAO orchestrationDAO;
    private final ProcessDAO processDAO;
    private final Cluster cluster;
    private final Map<String, MuleContext> contexts = Maps.newConcurrentMap();

    public OrchestrationEngine(MaestroConfiguration configuration,
                               OrchestrationDAO orchestrationDAO,
                               ProcessDAO processDAO,
                               Cluster cluster) {
        this.configuration = configuration;
        this.orchestrationDAO = orchestrationDAO;
        this.processDAO = processDAO;
        this.cluster = cluster;
    }

    public MaestroConfiguration getConfiguration() {
        return configuration;
    }

    public OrchestrationDAO getOrchestrationDAO() {
        return orchestrationDAO;
    }

    public ProcessDAO getProcessDAO() {
        return processDAO;
    }

    public Cluster getCluster() {
        return cluster;
    }

    @Override
    public void start() throws Exception {
        this.cluster.addUserContext(ORCH_ENGINE_KEY, this);
    }

    @Override
    public void stop() throws Exception {
        for (Map.Entry<String, MuleContext> entry : contexts.entrySet()) {
            String contextPath = entry.getKey();
            MuleContext context = entry.getValue();
            try {
                context.stop();
                context.dispose();
            } catch (Exception ex) {
                LOG.error("Failed to stop orchestration, context path: " + contextPath, ex);
            }
        }
    }

    public boolean start(Orchestration orchestration) {
        boolean result = doStart(orchestration);
        cluster.execute(new OrchestrationAction(OrchestrationActionType.START, orchestration.getContextPath()));
        return result;
    }

    protected boolean doStart(String contextPath) {
        Optional<Orchestration> orchestrationOpt = orchestrationDAO.findByContextPath(contextPath);
        return orchestrationOpt.isPresent() ? doStart(orchestrationOpt.get()) : false;
    }

    private boolean doStart(Orchestration orchestration) {
        try {
            MuleContext muleContext = contexts.get(orchestration.getContextPath());
            if (muleContext != null) {
                LOG.warn("Found running orchestration: " + orchestration.getName());
                return true;
            }
            DefaultMuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
            SpringXmlConfigurationBuilder configBuilder = new SpringXmlConfigurationBuilder(new ConfigResource[] {
                    new ConfigResource(
                            orchestration.getName(),
                            new ByteArrayInputStream(getGeneratedSpecification(orchestration).getBytes(StandardCharsets.UTF_8.name())))});
            muleContext = muleContextFactory.createMuleContext(configBuilder);
            muleContext.getRegistry().registerObject(ORCH_ENGINE_KEY, this);
            muleContext.start();
            if (muleContext.isStarted()) {
                contexts.put(orchestration.getContextPath(), muleContext);
                LOG.info("Started orchestration: " + orchestration.getName());
                return true;
            } else {
                LOG.error("Could not start orchestration: " + orchestration.getName());
                return false;
            }
        } catch (Exception ex) {
            LOG.error("Failed to start orchestration: " + orchestration.getName(), ex);
            return false;
        }
    }

    public boolean stop(Orchestration orchestration) {
        boolean result = doStop(orchestration);
        cluster.execute(new OrchestrationAction(OrchestrationActionType.STOP, orchestration.getContextPath()));
        return result;
    }

    protected boolean doStop(String contextPath) {
        Optional<Orchestration> orchestrationOpt = orchestrationDAO.findByContextPath(contextPath);
        return orchestrationOpt.isPresent() ? doStop(orchestrationOpt.get()) : false;
    }

    private boolean doStop(Orchestration orchestration) {
        try {
            MuleContext muleContext = contexts.get(orchestration.getContextPath());
            if (muleContext == null) {
                LOG.warn("Could not find running orchestration: " + orchestration.getName());
                return true;
            }
            muleContext.stop();
            muleContext.dispose();
            contexts.remove(orchestration.getContextPath());
            LOG.info("Stopped orchestration: " + orchestration.getName());
            return true;
        } catch (Exception ex) {
            LOG.error("Failed to stop orchestration: " + orchestration.getName(), ex);
            return false;
        }
    }

    public String getGeneratedSpecification(Orchestration orchestration) {
        try {
            if (orchestration.getType() == OrchestrationType.Scripted) {
                OrchestrationView view = new OrchestrationView(configuration, orchestration);
                FreemarkerViewRenderer renderer = new FreemarkerViewRenderer();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                renderer.render(view, Locale.getDefault(), baos);
                return baos.toString(StandardCharsets.UTF_8.name());
            } else {
                return orchestration.getSpecification();
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
