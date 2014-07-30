package com.yammer.maestro.engine;

import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class MonitoringTransformer extends AbstractMessageTransformer implements MuleContextAware {

    private ProcessStateType actionType;
    private MuleContext muleContext;

    public void setActionType(String actionType) {
        this.actionType = ProcessStateType.valueOf(actionType);
    }

    @Override
    public void setMuleContext(MuleContext muleContext) {
        this.muleContext = muleContext;
    }

    // This was originally implemented as an onCall method for a Callable component, but there is some bug in Mule
    // preventing it from working in the catch-exception-strategy element; using a transformer seems to work
    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {
        System.out.println("*** TRANS actionType " + actionType);
        System.out.println("*** TRANS context " + muleContext);
        return message;
    }
}
