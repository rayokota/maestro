package com.yammer.maestro.engine;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.yammer.maestro.daos.OrchestrationDAO;
import com.yammer.maestro.daos.ProcessDAO;
import com.yammer.maestro.models.MonitoringType;
import com.yammer.maestro.models.Orchestration;
import com.yammer.maestro.models.Process;
import com.yammer.maestro.models.ProcessState;
import org.joda.time.LocalDateTime;
import org.mule.api.ExceptionPayload;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class MonitoringTransformer extends AbstractMessageTransformer implements MuleContextAware {

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
        ProcessDAO processDAO = engine.getProcessDAO();
        OrchestrationDAO orchestrationDAO = engine.getOrchestrationDAO();
        Optional<Orchestration> orchestrationOpt = orchestrationDAO.findByContextPath(contextPath);
        if (orchestrationOpt.isPresent()) {
            Orchestration orchestration = orchestrationOpt.get();
            switch (processState) {
                case Started:
                    doStarted(message, processDAO, orchestration);
                    break;
                case Completed:
                    doCompleted(message, processDAO, orchestration);
                    break;
                case Errored:
                    doErrored(message, processDAO, orchestration);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal state " + processState);
            }
        }
        return message;
    }

    private void doStarted(MuleMessage message, ProcessDAO dao, Orchestration orchestration) {
        Process process = new Process();
        process.setState(ProcessState.Started);
        process.setStartTime(LocalDateTime.now());
        process.setOrchestration(orchestration);
        if (orchestration.getMonitoringType() == MonitoringType.DEBUG) {
            process = dao.save(process);
        }
        message.setInvocationProperty("orchProcess", process);
    }

    private void doCompleted(MuleMessage message, ProcessDAO dao, Orchestration orchestration) {
        try {
            Process process = message.getInvocationProperty("orchProcess");
            process.setMessage(message.getPayloadAsString());
            process.setState(ProcessState.Completed);
            process.setEndTime(LocalDateTime.now());
            if (orchestration.getMonitoringType() == MonitoringType.DEBUG) {
                dao.merge(process);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void doErrored(MuleMessage message, ProcessDAO dao, Orchestration orchestration) {
        Process process = message.getInvocationProperty("orchProcess");
        ExceptionPayload ep = message.getExceptionPayload();
        if (ep != null) {
            Throwable t = ep.getException();
            if (t instanceof MuleException) {
                process.setMessage(((MuleException)t).getSummaryMessage());
            } else {
                process.setMessage(t.getMessage());
            }
        }
        process.setState(ProcessState.Errored);
        process.setEndTime(LocalDateTime.now());
        switch (orchestration.getMonitoringType()) {
            case DEBUG:
                dao.merge(process);
                break;
            case ERROR:
                dao.save(process);
                break;
        }
    }
}
