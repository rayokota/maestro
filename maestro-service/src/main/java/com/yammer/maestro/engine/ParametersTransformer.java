package com.yammer.maestro.engine;

import com.google.common.collect.Maps;
import com.sun.jersey.api.uri.UriTemplate;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ParametersTransformer extends AbstractMessageTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ParametersTransformer.class);

    @Override
    public Object transformMessage(MuleMessage message, String encoding) throws TransformerException {
        LOG.debug("Context path: {}",           message.getInboundProperty("http.context.path"));
        LOG.debug("Context URI: {}",            message.getInboundProperty("http.context.uri"));
        LOG.debug("Query string: {}",           message.getInboundProperty("http.query.string"));
        LOG.debug("Request: {}",                message.getInboundProperty("http.request"));
        LOG.debug("Request path: {}",           message.getInboundProperty("http.request.path"));
        LOG.debug("Relative path: {}",          message.getInboundProperty("http.relative.path"));
        LOG.debug("Relative path template: {}", message.getInvocationProperty("relativePathTemplate"));

        String relativePathTemplate = message.getInvocationProperty("orchRelativePathTemplate");
        String relativePath = message.getInboundProperty("http.relative.path");
        UriTemplate uriTemplate = new UriTemplate(relativePathTemplate);
        Map<String, String> pathParams = Maps.newHashMap();
        uriTemplate.match(relativePath, pathParams);

        message.setInvocationProperty("pathParams", pathParams);
        message.setInvocationProperty("queryParams", message.getInboundProperty("http.query.params"));
        return message;
    }
}
