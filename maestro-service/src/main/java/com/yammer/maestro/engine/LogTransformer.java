package com.yammer.maestro.engine;

import com.google.common.base.Optional;
import com.yammer.maestro.daos.LogDAO;
import com.yammer.maestro.daos.OrchestrationDAO;
import com.yammer.maestro.models.*;
import org.joda.time.LocalDateTime;
import org.mule.api.ExceptionPayload;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class LogTransformer extends AbstractMessageTransformer implements MuleContextAware {

    private ProcessState processState;
    private MuleContext muleContext;

    public void setProcessState(String processState) {
        this.processState = ProcessState.valueOf(processState);
    }

    @Override
    public void setMuleContext(MuleContext muleContext) {
        this.muleContext = muleContext;
    }

    // This was originally implemented as an onCall method for a Callable component, but there is some bug in Mule
    // preventing it from working in the catch-exception-strategy element; using a transformer seems to work
    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {
        String contextPath = message.getInvocationProperty("orchContextPath");
        OrchestrationEngine engine = muleContext.getRegistry().lookupObject(OrchestrationEngine.ORCH_ENGINE_KEY);
        LogDAO logDAO = engine.getLogDAO();
        OrchestrationDAO orchestrationDAO = engine.getOrchestrationDAO();
        Optional<Orchestration> orchestrationOpt = orchestrationDAO.findByContextPath(contextPath);
        if (orchestrationOpt.isPresent()) {
            Orchestration orchestration = orchestrationOpt.get();
            switch (processState) {
                case Started:
                    doStarted(message, logDAO, orchestration);
                    break;
                case Completed:
                    doCompleted(message, logDAO, orchestration);
                    break;
                case Errored:
                    doErrored(message, logDAO, orchestration);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal state " + processState);
            }
        }
        return message;
    }

    private void doStarted(MuleMessage message, LogDAO dao, Orchestration orchestration) {
        if (orchestration.getLogLevel() != LogLevel.OFF) {
            String method = message.getInboundProperty("http.method");
            String request = message.getInboundProperty("http.request");
            String version = message.getInboundProperty("http.version");

            Log log = new Log();
            log.setState(ProcessState.Started);
            log.setStartTime(LocalDateTime.now());
            log.setOrchestration(orchestration);
            log.setMethod(HttpMethod.valueOf(method.toUpperCase()));
            log.setRequest(request);
            log.setVersion(version);
            message.setInvocationProperty("orchLog", log);
        }
    }

    private void doCompleted(MuleMessage message, LogDAO dao, Orchestration orchestration) {
        if (orchestration.getLogLevel() == LogLevel.DEBUG) {
            String httpStatus = message.getOutboundProperty("http.status");
            Log log = message.getInvocationProperty("orchLog");
            try {
                log.setMessage(message.getPayloadAsString());
            } catch (Exception e) {
                // ignore
            }
            log.setState(ProcessState.Completed);
            log.setEndTime(LocalDateTime.now());
            log.setStatus(Integer.parseInt(httpStatus));
            dao.save(log);
        }
    }

    private void doErrored(MuleMessage message, LogDAO dao, Orchestration orchestration) {
        if (orchestration.getLogLevel() != LogLevel.OFF) {
            String httpStatus = message.getOutboundProperty("http.status");
            Log log = message.getInvocationProperty("orchLog");
            ExceptionPayload ep = message.getExceptionPayload();
            if (ep != null) {
                Throwable t = ep.getException();
                if (t instanceof MuleException) {
                    log.setMessage(((MuleException) t).getSummaryMessage());
                } else {
                    log.setMessage(t.getMessage());
                }
            }
            log.setState(ProcessState.Errored);
            log.setEndTime(LocalDateTime.now());
            log.setStatus(Integer.parseInt(httpStatus));
            dao.save(log);
        }
    }
}
