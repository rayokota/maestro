# Maestro

A Dropwizard service for running orchestrations.

Maestro wraps the [Mule ESB](http://www.mulesoft.org/) to automate integration with external applications.

## Running Maestro

Maestro was created with the [Angular-Dropwizard generator](https://github.com/rayokota/generator-angular-dropwizard).

To compile and run the service:

    mvn compile exec:exec -pl maestro-service
    
    
## Overview

The main components in Maestro are orchestrations and their constituent actions.  An orchestration is exposed as a REST endpoint that calls the constituent actions and aggregates the results of those actions into an HTTP response.

An orchestration has the following properties:

- **Name**: an arbitrary string.

- **Context path**:  a unique prefix to select this orchestration when a request is received.  The actual URL will be of the form `http://localhost:8080/api/{contextPath}/{relativePath}`.

- **Relative path template**: A URI template specifying the relative path that follows the context path.  When a request is received, any path parameters in the URI template will be made available as variables.  For example, with a template of `users/{userId}` and a request with a relative path of `users/3`, the variable `userId` would be set to the value `3`.

- **Validation filter**:  A [Mule expression](http://www.mulesoft.org/documentation/display/current/Mule+Expression+Language+MEL) to validate requests, such as `userId > 0`.  If the validation fails, a 422 (unprocessable entity) is returned.

- **Method**:  The HTTP method.

- **Whether to run actions in parallel**:  Actions can be run in sequence or in parallel.

- **Log level**:  OFF, ERROR, or DEBUG.

- **Response**:  A script to generate the HTTP response.  The final result should be a string.  Here is an example JavaScript script to generate the response, assuming that the result of one of the actions was saved in a variable named `order`.

        var orderId = order.get("id");
		var x = { id: orderId, name: "Fred" };
		JSON.stringify(x);

An action can be either an HTTP action or an RDBMS action.  Both types of action have the following properties:

- **Name**: an arbitrary string.

- **Routing condition**:  A [Mule expression](http://www.mulesoft.org/documentation/display/current/Mule+Expression+Language+MEL) to determine if this action should be performed, such as `userId > 100`.

- **Variable name**:  A variable to hold the result of the action.

The HTTP action has the following additional properties:

- **Host**:  The host to which to submit the request.

- **Port**:  The port to which to submit the request.

- **Path**:  The path to which to submit the request.

- **Method**:  The HTTP method.

- **Headers**:  A set of headers, specified on separate lines in the form `Cache-Control: no-cache`, for example.

- **Payload**:  A script to generate the HTTP request.  The final result should be a string.  Here is an example JavaScript script to generate the request:

		var x = { id: 3, name: "Fred" };
		JSON.stringify(x);

The RDBMS action has the following additional properties:

- **JDBC URL**:  The JDBC URL, such as `jdbc:postgresql://localhost:5432/mydb?user=fred&password=secret`.

- **Driver class**:  The JDBC driver class name, such as `org.postgresql.Driver`.

- **Query**:  A SQL query, such as `select * from Users`.

   
## Features

Maestro has the following features.

- **Browser-based lifecycle management**:  Development, deployment, and monitoring of orchestrations is performed entirely in the browser.

- **Hot deployment**:  Orchestrations can be deployed without restarting Maestro or affecting other orchestrations.

- **Cluster support**:  A set of Maestro nodes can form a cluster.  Starting or stopping an orchestration in one node will cause it to be started or stopped in all other nodes in the cluster.

- **Request validation**:  A validation filter allows requests to be properly validated before any actions are performed.

- **Conditional routing**:  A routing condition allows actions to be conditionally performed.

- **Choice of sequential or parallel execution of actions**:  Actions can be performed in parallel; otherwise if the input to one action depends on the output of another, actions can be performed in sequence.

- **Support for multiple action types**:  Both HTTP and RDBMS actions are supported.  Other action types might be added at a later time.

- **Support for multiple scripting languages**:  Scripts can be written in Groovy, JavaScript, Python, or Ruby.

- **Log aggregation**:  Logs are aggregated and made visible via the browser.

- **Automatic versioning**:  Every change to an orchestration results in a new revision which is persisted.  Revisions for an orchestration can be obtained via the following REST endpoints:

		/maestro/orchestrations/{id}/revisionIds
		
		/maestro/orchestrations/{id}/revision/{revisionId}

