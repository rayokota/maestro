package com.yammer.maestro.engine;

import org.mule.DefaultMuleEvent;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.routing.AggregationContext;
import org.mule.routing.CollectAllAggregationStrategy;

public class CollectVariablesAggregationStrategy extends CollectAllAggregationStrategy {

    @Override
    public MuleEvent aggregate(AggregationContext context) throws MuleException {
        MuleEvent result = super.aggregate(context);
        MuleMessage resultMessage = result.getMessage();

        for (MuleEvent event : context.collectEventsWithoutExceptions()) {
            MuleMessage message = event.getMessage();
            String variableName = message.getInvocationProperty("_variableName");
            if (variableName != null) {
                Object variable = message.getInvocationProperty(variableName);
                if (variable != null) {
                    // copy variable result to composite message
                    resultMessage.setInvocationProperty(variableName, variable);
                }
            }
        }

        return result;
    }
}
