# OpenTelemetry Spring Boot2 Starter

Provides autoconfiguration of the OpenTelemetry SDK in Spring Boot v2.x
applications. Additional SDK plugins defined elsewhere in the application
context are injected into the configured and initialized SDK.

## Setup

```groovy
dependencies {
  implementation "io.opentelemetry:opentelemetry-api:${otelVersion}"
  implementation "io.opentelemetry:opentelemetry-sdk:${otelVersion}"
  implementation "io.opentelemetry:opentelemetry-contrib-spring-boot2:${otelVersion}"
}
```

## Configuration Properties

Any or all of these can be specified in `application.properties` or 
`application.yaml`. If not defined, the default value is used.

| Key                                                                | Default Value | Description                       |
| :----------------------------------------------------------------- | :------------ | :-------------------------------- |
| management.opentelemetry.enabled                                   | true          | Enables Spring autoconfiguration of the OpenTelemetry SDK. |
| management.opentelemetry.tracer.sampler.name                       | always_off    | One of `always_on`, `always_off`, `probability`, `custom`. |
| management.opentelemetry.tracer.sampler.impl-class                 |               | Fully-qualified class name of Sampler implementation if name is `custom`. |
| management.opentelemetry.tracer.sampler.properties.*               |               | Configuration values for the Sampler implementation. |
| management.opentelemetry.tracer.max-number-of-attributes           | 32            | Maximum number of attributes per span. |
| management.opentelemetry.tracer.max-number-of-events               | 128           | Maximum number of events per span. |
| management.opentelemetry.tracer.max-number-of-links                | 32            | Maximum number of links per span. |
| management.opentelemetry.tracer.max-number-of-attributes-per-event | 32            | Maximum number of attributes per event. |
| management.opentelemetry.tracer.max-number-of-attributes-per-link  | 32            | Maximum number of attributes per link. |
| management.opentelemetry.tracer.export-sampled-only                | true          | Publish only spans flagged as sampled to exporters or not. |
| management.opentelemetry.tracer.log-spans                          | false         | Add the logging-only exporter or not. |
| management.opentelemetry.tracer.export-inmemory                    | false         | Add the testing in-memory exporter or not. |

## Resource Providers

OpenTelemetry Resource labels that can be obtained from the Spring Boot
project info actuator are added by default during autoconfiguration.
To add other Resource labels define other application context beans which
implement the `io.opentelemetry.contrib.spring.boot.ResourceProvider`
interface.

## Span Processors and Exporters

Additional tracing span processors and/or exporters can be wired into
the OpenTelemetry SDK defining application context beans which implement
the `io.opentelemetry.sdk.trace.SpanProcessor` or 
`io.opentelemetry.sdk.trace.exporter.SpanExporter`.

## Usage in Non Spring Boot Applications

Configuration of the primary components of the OpenTelemetry SDK is performed
by components which implement `org.springframework.beans.factory.FactoryBean`.
These can be used in a Spring-wired application that is not Spring Boot by
disabled context classpath scanning for this library and defining instances
of the factories in a Spring XML context file.
