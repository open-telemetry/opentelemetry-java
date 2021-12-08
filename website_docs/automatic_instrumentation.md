---
Title: "Automatic Instrumentation"
Weight: 3
---

Automatic instrumentation with Java uses a Java agent JAR that can be attached to any Java 8+ application. It dynamically injects bytecode to capture telemetry from many popular libraries and frameworks. It is the preferred way to capture telemetry data at the "edges" of an app or service, such as inbound requests, outbound HTTP calls, database calls, and so on. To instrument application code in your app or service, use [Manual Instrumentation](manual_instrumentation.md)

## Setup

Download the [latest version](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/).

The JAR file (`opentelemetry-javaagent.jar`) contains the agent and all automatic instrumentation packages.

Place the JAR in your preferred directory and launch it with your app:

```
java -javaagent:path/to/opentelemetry-javaagent.jar \
     -jar myapp.jar
```

## Configuring the agent

The agent is highly configurable.

One option is to pass configuration properties via the `D` flag. In this example a service name and zipkin exporter for traces are configured:

```
java -javaagent:path/to/opentelemetry-javaagent.jar \
     -Dotel.resource.attributes=service.name=your-service-name \
     -Dotel.traces.exporter=zipkin \
     -jar myapp.jar
```

You can also use environment variables to configure the agent:

```
OTEL_SERVICE_NAME=your-service-name \
OTEL_TRACES_EXPORTER=zipkin \
java -javaagent:path/to/opentelemetry-javaagent.jar \
     -jar myapp.jar
```

You can also supply a Java properties file and load configuration values from there:

```
java -javaagent:path/to/opentelemetry-javaagent.jar \
     -Dotel.javaagent.configuration-file=path/to/properties/file.properties \
     -jar myapp.jar
```

or

```
OTEL_JAVAAGENT_CONFIGURATION_FILE=path/to/properties/file.properties \
java -javaagent:path/to/opentelemetry-javaagent.jar \
     -jar myapp.jar
```

To see the full range of configuration options, see [Agent Configuration](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/agent-config.md)

## Supported libraries, frameworks, application services, and JVMs

Many popular have supported automatic instrumentation. See [Supported libraries, frameworks, application services, and JVMs](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md) for the full list.

## Troubleshooting

You can pass the `-Dotel.javaagent.debug=true` parameter to the agent to see debug logs. Note that these are quite verbose.

## Next steps

After you have automatic instrumentation configured for your app or service, it is worthwhile to add [Manual Instrumentation](manual_instrumentation.md) to collect richer telemetry data.
