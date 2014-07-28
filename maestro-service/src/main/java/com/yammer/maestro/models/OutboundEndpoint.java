package com.yammer.maestro.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Maps;
import org.joda.time.LocalDateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Map;


@Entity
@Table(name = "outbound_endpoints")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RdbmsOutboundEndpoint.class, name = "RDBMS"),
        @JsonSubTypes.Type(value = HttpOutboundEndpoint.class, name = "HTTP")
})
public class OutboundEndpoint {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @JsonIgnore  // since type is added by JsonTypeInfo
    private EndpointType type;

    @NotNull
    @Column(name = "variable_name")
    private String variableName;

    @Column(name = "script", length = 32_000)
    private String script;

    @NotNull
    @Column(name = "script_type")
    @Enumerated(EnumType.STRING)
    private ScriptType scriptType = ScriptType.JavaScript;

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="properties", joinColumns=@JoinColumn(name="endpoint_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name="value")
    private Map<String, String> properties = Maps.newHashMap();

    @ManyToOne
    @JsonIgnore
    private Orchestration orchestration;

    @Transient
    @JsonProperty
    private long orchestrationId;

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

    public EndpointType getType() {
        return type;
    }

    public void setType(EndpointType type) {
        this.type = type;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> props) {
        this.properties = props;
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    public Orchestration getOrchestration() {
        return orchestration;
    }

    public void setOrchestration(Orchestration orchestration) {
        this.orchestration = orchestration;
    }

    public long getOrchestrationId() {
        return orchestrationId;
    }

    public void setOrchestrationId(long orchestrationId) {
        this.orchestrationId = orchestrationId;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboundEndpoint)) return false;
        OutboundEndpoint that = (OutboundEndpoint) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
