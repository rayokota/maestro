package com.yammer.maestro.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Entity
@Table(name = "orchestrations")
@Audited
public class Orchestration extends AuditedEntity {

    @NotNull
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private OrchestrationType type = OrchestrationType.Scripted;

    @NotNull
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private OrchestrationState state = OrchestrationState.Initialized;

    @Column(name = "port")
    @JsonIgnore
    private int port = 0;

    @NaturalId
    @NotNull
    @Column(name = "context_path", unique = true, length = 2048)
    @Pattern(regexp = "[^/]+")
    private String contextPath;

    @NotNull
    @Column(name = "relative_path_template", length = 2048)
    private String relativePathTemplate;

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

    @Column(name = "specification", length = 32_000)
    private String specification;

    @Column(name = "script", length = 32_000)
    private String script;

    @NotNull
    @Column(name = "script_type")
    @Enumerated(EnumType.STRING)
    private ScriptType scriptType = ScriptType.JavaScript;

    @NotNull
    @Column(name = "log_level")
    @Enumerated(EnumType.STRING)
    private LogLevel logLevel = LogLevel.ERROR;

    @OneToMany(mappedBy = "orchestration", fetch = FetchType.EAGER)
    @OrderBy("id")  // Hibernate preserves ordering even for sets
    private Set<OutboundEndpoint> outboundEndpoints = Sets.newLinkedHashSet();

    @Transient
    private boolean open = false;

    public OrchestrationType getType() {
        return type;
    }

    public void setType(OrchestrationType type) {
        this.type = type;
    }

    public OrchestrationState getState() {
        return state;
    }

    public void setState(OrchestrationState state) {
        this.state = state;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getRelativePathTemplate() {
        return relativePathTemplate;
    }

    public void setRelativePathTemplate(String relativePathTemplate) {
        this.relativePathTemplate = relativePathTemplate;
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

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public Set<OutboundEndpoint> getOutboundEndpoints() {
        return outboundEndpoints;
    }

    public void setOutboundEndpoints(Set<OutboundEndpoint> outboundEndpoints) {
        this.outboundEndpoints = outboundEndpoints;
    }

    @JsonProperty
    public boolean getOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Transient
    @JsonIgnore
    public int getDerivedPort() {
        return getPort() == 0 ? (int)getId() : getPort();
    }
}
