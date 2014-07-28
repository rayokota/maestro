package com.yammer.maestro.proxy;

import com.google.common.base.Optional;
import com.yammer.maestro.config.MaestroConfiguration;
import com.yammer.maestro.daos.OrchestrationDAO;
import com.yammer.maestro.models.Orchestration;
import org.eclipse.jetty.proxy.ProxyServlet;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrchestrationProxyServlet extends ProxyServlet {

    private final MaestroConfiguration configuration;
    private final OrchestrationDAO dao;
    private final Pattern urlPattern;

    public OrchestrationProxyServlet(MaestroConfiguration configuration, OrchestrationDAO dao) {
        this.configuration = configuration;
        this.dao = dao;
        this.urlPattern = Pattern.compile(configuration.getRootPath() + "/([^/]+)/?.*");
    }

    @Override
    protected URI rewriteURI(HttpServletRequest request) {

        String path = request.getRequestURI();
        final Matcher matcher = urlPattern.matcher(path);
        if (!matcher.find()) {
            return null;
        }
        String contextPath = matcher.group(1);
        Optional<Orchestration> orchestrationOpt = dao.findByContextPath(contextPath);
        if (!orchestrationOpt.isPresent()) {
            return null;
        }
        Orchestration orchestration = orchestrationOpt.get();
        StringBuilder uri = new StringBuilder("http://127.0.0.1:").
                append(configuration.getBasePort() + orchestration.getDerivedPort()).
                append(path.substring(configuration.getRootPath().length()));
        String query = request.getQueryString();
        if (query != null) {
            uri.append("?").append(query);
        }

        return URI.create(uri.toString()).normalize();
    }
}
