package com.yammer.maestro.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

@Entity
@Table(name = "http_outbound_endpoints")
@PrimaryKeyJoinColumn(name = "id")
public class HttpOutboundEndpoint extends OutboundEndpoint {

    public HttpOutboundEndpoint() {
        setType(EndpointType.HTTP);
    }

    @NotNull
    @Column(name = "host")
    private String host = "localhost";

    @Column(name = "port")
    private int port = 8080;

    @NotNull
    @Column(name = "path", length = 2048)
    private String path;

    @NotNull
    @Column(name = "content_type")
    private String contentType = MediaType.APPLICATION_JSON;

    @NotNull
    @Column(name = "method")
    @Enumerated(EnumType.STRING)
    private HttpMethod method = HttpMethod.GET;

    @NotNull
    @Column(name = "keep_alive")
    private boolean keepAlive = false;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
}
