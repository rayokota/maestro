package com.yammer.maestro.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

@Entity
@Table(name = "rdbms_outbound_endpoints")
@PrimaryKeyJoinColumn(name = "id")
public class RdbmsOutboundEndpoint extends OutboundEndpoint {

    public RdbmsOutboundEndpoint() {
        setType(EndpointType.RDBMS);
    }

    @NotNull
    @Column(name = "url")
    private String url;

    @NotNull
    @Column(name = "driverClassName")
    private String driverClassName;

    @NotNull
    @Column(name = "query", length = 2048)
    private String query;

    @Column(name = "max_rows")
    private int maxRows = 0;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }
}
