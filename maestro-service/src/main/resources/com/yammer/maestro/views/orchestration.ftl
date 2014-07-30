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

        <custom-transformer class="com.yammer.maestro.engine.MonitoringTransformer">
            <spring:property name="actionType" value="STARTED"/>
        </custom-transformer>

        <byte-array-to-string-transformer doc:name="Byte Array to String"/>
        <json:object-to-json-transformer doc:name="Object to JSON"/>
        <json:json-to-object-transformer returnClass="java.lang.Object" doc:name="JSON to Object"/>
        <set-variable variableName="orchInboundPayload" value="#[message.payload]" />

        <set-variable variableName="orchContextPath" value="${orchestration.contextPath?xml}" doc:name="Set Context Path"/>
        <set-variable variableName="orchRelativePathTemplate" value="${orchestration.relativePathTemplate?xml}" doc:name="Set Relative Path Template"/>
        <custom-transformer class="com.yammer.maestro.engine.ParametersTransformer"/>

        <#list orchestration.outboundEndpoints as endpoint>
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
        <set-variable variableName="${endpoint.variableName?xml}" value="#[message.payload]" doc:name="Variable"/>
        </#list>

        <set-payload value="#[orchInboundPayload]" />
        <scripting:transformer doc:name="Script">
            <scripting:script engine="${orchestration.scriptType?xml}"><![CDATA[
                ${orchestration.script}
            ]]></scripting:script>
        </scripting:transformer>

        <custom-transformer class="com.yammer.maestro.engine.MonitoringTransformer">
            <spring:property name="actionType" value="COMPLETED"/>
        </custom-transformer>

        <catch-exception-strategy>
            <logger message="The request cannot be processed, the error is #[exception.getSummaryMessage()]" level="ERROR"/>
            <set-payload value="The request cannot be processed, the error is #[exception.getSummaryMessage()]"/>
            <set-property propertyName="http.status" value="500"/>

            <custom-transformer class="com.yammer.maestro.engine.MonitoringTransformer">
                <spring:property name="actionType" value="ERRORED"/>
            </custom-transformer>
        </catch-exception-strategy>
    </flow>
</mule>

