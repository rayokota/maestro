package com.yammer.maestro.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import org.hibernate.annotations.NaturalId;
import org.joda.time.LocalDateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Entity
@Table(name = "orchestrations")
public class Orchestration {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Column(name = "name", unique = true)
    private String name;

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

    @OneToMany(mappedBy = "orchestration", fetch = FetchType.EAGER)
    private Set<OutboundEndpoint> outboundEndpoints = Sets.newHashSet();

    @Transient
    private boolean open = false;

    @Column(name = "created")
    private LocalDateTime created;
    
    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    @Transient
    @JsonIgnore
    public int getDerivedPort() {
        return getPort() == 0 ? (int)getId() : getPort();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Orchestration)) return false;
        Orchestration that = (Orchestration) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
