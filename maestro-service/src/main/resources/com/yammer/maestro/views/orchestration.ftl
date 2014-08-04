<#setting number_format="computer">
<?xml version="1.0" encoding="UTF-8"?>

<mule xmlns:db="http://www.mulesoft.org/schema/mule/db"
      xmlns:http="http://www.mulesoft.org/schema/mule/http"
      xmlns:json="http://www.mulesoft.org/schema/mule/json"
      xmlns:scripting="http://www.mulesoft.org/schema/mule/scripting"
      xmlns="http://www.mulesoft.org/schema/mule/core" xmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
      xmlns:spring="http://www.springframework.org/schema/beans"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-current.xsd
http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd
http://www.mulesoft.org/schema/mule/db http://www.mulesoft.org/schema/mule/db/current/mule-db.xsd
http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd
http://www.mulesoft.org/schema/mule/json http://www.mulesoft.org/schema/mule/json/current/mule-json.xsd
http://www.mulesoft.org/schema/mule/scripting http://www.mulesoft.org/schema/mule/scripting/current/mule-scripting.xsd">
    <#list orchestration.outboundEndpoints as endpoint>
    <#if endpoint.type == "RDBMS">
    <db:generic-config name="${endpoint.name?xml}" url="${endpoint.url?xml}" driverClassName="${endpoint.driverClassName?xml}" doc:name="Database Configuration"/>
    </#if>
    </#list>
    <flow name="${orchestration.name?xml}" doc:name="${orchestration.name?xml}">
        <http:inbound-endpoint exchange-pattern="request-response" host="localhost" port="${configuration.basePort + orchestration.derivedPort}" path="${orchestration.contextPath?xml}" contentType="${orchestration.contentType?xml}" keepAlive="${orchestration.keepAlive?c}" doc:name="HTTP"/>

        <custom-transformer class="com.yammer.maestro.engine.LifecycleTransformer">
            <spring:property name="processState" value="Started"/>
            <spring:property name="contextPath" value="${orchestration.contextPath?xml}"/>
        </custom-transformer>

        <byte-array-to-string-transformer doc:name="Byte Array to String"/>
        <json:object-to-json-transformer doc:name="Object to JSON"/>
        <json:json-to-object-transformer returnClass="java.lang.Object" doc:name="JSON to Object"/>
        <set-variable variableName="_inboundPayload" value="#[message.payload]" />

        <custom-transformer class="com.yammer.maestro.engine.ParametersTransformer">
            <spring:property name="relativePathTemplate" value="${orchestration.relativePathTemplate?xml}"/>
        </custom-transformer>

        <message-filter doc:name="Message" throwOnUnaccepted="true">
            <expression-filter expression="#[${(orchestration.filter!"true")?xml}]"/>
        </message-filter>

        <#if orchestration.parallel && orchestration.outboundEndpoints?size &gt; 1>
        <scatter-gather>
            <custom-aggregation-strategy class="com.yammer.maestro.engine.CollectVariablesAggregationStrategy"/>
        </#if>

        <#list orchestration.outboundEndpoints as endpoint>
        <choice doc:name="Choice">
            <when expression="#[${(endpoint.condition!"true")?xml}]">
                <#if endpoint.type == "HTTP">
                <#if endpoint.script?trim?length &gt; 0>
                <scripting:transformer doc:name="Script">
                    <scripting:script engine="${endpoint.scriptType?xml}"><![CDATA[
                        ${endpoint.script}
                    ]]></scripting:script>
                </scripting:transformer>
                </#if>
                <http:outbound-endpoint exchange-pattern="request-response" host="${endpoint.host?xml}" port="${endpoint.port}" method="${endpoint.method?xml}" path="${endpoint.path?xml}" contentType="${endpoint.contentType?xml}" keepAlive="${endpoint.keepAlive?c}" doc:name="HTTP">
                    <#list endpoint.properties?keys as key>
                    <set-property propertyName="${key?xml}" value="${endpoint.properties[key]?xml}"/>
                    </#list>
                </http:outbound-endpoint>
                <byte-array-to-string-transformer doc:name="Byte Array to String"/>
                <json:object-to-json-transformer doc:name="Object to JSON"/>
                <json:json-to-object-transformer returnClass="java.lang.Object" doc:name="JSON to Object"/>
                <#else>
                <db:select config-ref="${endpoint.name?xml}" doc:name="Database">
                    <db:parameterized-query><![CDATA[
                        ${endpoint.query}
                    ]]></db:parameterized-query>
                </db:select>
                </#if>
                <set-variable variableName="_variableName" value="${endpoint.variableName?xml}" doc:name="Variable"/>
                <set-variable variableName="${endpoint.variableName?xml}" value="#[message.payload]" doc:name="Variable"/>
            </when>
            <otherwise>
                <logger message="Skipping action ${endpoint.name}" level="DEBUG" doc:name="Logger"/>
            </otherwise>
        </choice>
        </#list>

        <#if orchestration.parallel && orchestration.outboundEndpoints?size &gt; 1>
        </scatter-gather>
        </#if>

        <set-payload value="#[_inboundPayload]" />
        <#if orchestration.script?trim?length &gt; 0>
        <scripting:transformer doc:name="Script">
            <scripting:script engine="${orchestration.scriptType?xml}"><![CDATA[
                ${orchestration.script}
            ]]></scripting:script>
        </scripting:transformer>
        </#if>

        <set-property propertyName="http.status" value="200"/>

        <custom-transformer class="com.yammer.maestro.engine.LifecycleTransformer">
            <spring:property name="processState" value="Completed"/>
            <spring:property name="contextPath" value="${orchestration.contextPath?xml}"/>
        </custom-transformer>

        <choice-exception-strategy>
            <catch-exception-strategy when="#[exception.causedBy(org.mule.api.routing.filter.FilterUnacceptedException)]">
                <logger message="The request cannot be processed, failed the filter: ${orchestration.filter}" level="ERROR"/>
                <set-payload value="The request cannot be processed, failed the filter: ${orchestration.filter}."/>
                <set-property propertyName="http.status" value="422"/>

                <custom-transformer class="com.yammer.maestro.engine.LifecycleTransformer">
                    <spring:property name="processState" value="Completed"/>
                    <spring:property name="contextPath" value="${orchestration.contextPath?xml}"/>
                </custom-transformer>
            </catch-exception-strategy>
            <catch-exception-strategy>
                <logger message="The request cannot be processed, the error is #[exception.getSummaryMessage()]" level="ERROR"/>
                <set-payload value="The request cannot be processed, the error is #[exception.getSummaryMessage()]"/>
                <set-property propertyName="http.status" value="500"/>

                <custom-transformer class="com.yammer.maestro.engine.LifecycleTransformer">
                    <spring:property name="processState" value="Errored"/>
                    <spring:property name="contextPath" value="${orchestration.contextPath?xml}"/>
                </custom-transformer>
            </catch-exception-strategy>
        </choice-exception-strategy>
    </flow>
</mule>

