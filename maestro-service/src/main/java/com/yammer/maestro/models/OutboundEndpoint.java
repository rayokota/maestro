package com.yammer.maestro.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Maps;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Map;


@Entity
@Table(name = "outbound_endpoints")
@Inheritance(strategy = InheritanceType.JOINED)
@Audited
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RdbmsOutboundEndpoint.class, name = "RDBMS"),
        @JsonSubTypes.Type(value = HttpOutboundEndpoint.class, name = "HTTP")
})
public class OutboundEndpoint extends AuditedEntity {

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
    @OrderBy("property_key")
    private Map<String, String> properties = Maps.newHashMap();

    @ManyToOne
    @JsonIgnore
    private Orchestration orchestration;

    @Transient
    private long orchestrationId = 0;

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

    @JsonProperty
    public long getOrchestrationId() {
        return orchestrationId == 0 ? getOrchestration().getId() : orchestrationId;
    }

    public void setOrchestrationId(long orchestrationId) {
        this.orchestrationId = orchestrationId;
    }
}
