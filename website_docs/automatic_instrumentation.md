---
title: "Automatic Instrumentation"
weight: 3
---

## Supported Libraries and Frameworks

| Library/Framework                                                                                                                     | Versions                       |
|---------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|
| [Akka HTTP](https://doc.akka.io/docs/akka-http/current/index.html)                                                                    | 10.0+                          |
| [Apache HttpAsyncClient](https://hc.apache.org/index.html)                                                                            | 4.0+                           |
| [Apache HttpClient](https://hc.apache.org/index.html)                                                                                 | 2.0+                           |
| [Armeria](https://armeria.dev)                                                                                                        | 0.99.8+                        |
| [AWS Lambda](https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html)                                                          | 1.0+                           |
| [AWS SDK](https://aws.amazon.com/sdk-for-java/)                                                                                       | 1.11.x and 2.2.0+              |
| [Cassandra Driver](https://github.com/datastax/java-driver)                                                                           | 3.0+                           |
| [Couchbase Client](https://github.com/couchbase/couchbase-java-client)                                                                | 2.0+ (not including 3.x yet)   |
| [Dropwizard Views](https://www.dropwizard.io/en/latest/manual/views.html)                                                             | 0.7+                           |
| [Elasticsearch API](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html)                                 | 5.0+ (not including 7.x yet)   |
| [Elasticsearch REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html)                        | 5.0+                           |
| [Finatra](https://github.com/twitter/finatra)                                                                                         | 2.9+                           |
| [Geode Client](https://geode.apache.org/)                                                                                             | 1.4+                           |
| [Google HTTP Client](https://github.com/googleapis/google-http-java-client)                                                           | 1.19+                          |
| [Grizzly](https://javaee.github.io/grizzly/httpserverframework.html)                                                                  | 2.0+ (disabled by default, see below) |
| [Grizzly Client](https://github.com/javaee/grizzly-ahc)                                                                               | 1.9+                           |
| [gRPC](https://github.com/grpc/grpc-java)                                                                                             | 1.5+                           |
| [Hibernate](https://github.com/hibernate/hibernate-orm)                                                                               | 3.3+                           |
| [HttpURLConnection](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/HttpURLConnection.html)                     | Java 7+                        |
| [Hystrix](https://github.com/Netflix/Hystrix)                                                                                         | 1.4+                           |
| [JAX-RS](https://javaee.github.io/javaee-spec/javadocs/javax/ws/rs/package-summary.html)                                              | 0.5+                           |
| [JAX-RS Client](https://javaee.github.io/javaee-spec/javadocs/javax/ws/rs/client/package-summary.html)                                | 2.0+                           |
| [JDBC](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/package-summary.html)                                     | Java 7+                        |
| [Jedis](https://github.com/xetorthio/jedis)                                                                                           | 1.4+                           |
| [Jetty](https://www.eclipse.org/jetty/)                                                                                               | 8.0+                           |
| [JMS](https://javaee.github.io/javaee-spec/javadocs/javax/jms/package-summary.html)                                                   | 1.1+                           |
| [JSP](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/jsp/package-summary.html)                                           | 2.3+                           |
| [Kafka](https://kafka.apache.org/20/javadoc/overview-summary.html)                                                                    | 0.11+                          |
| [khttp](https://khttp.readthedocs.io)                                                                                                 | 0.1+                           |
| [Kubernetes Client](https://github.com/kubernetes-client/java)                                                                        | 7.0+                           |
| [Lettuce](https://github.com/lettuce-io/lettuce-core)                                                                                 | 4.0+ (not including 6.x yet)   |
| [Log4j 1](https://logging.apache.org/log4j/1.2/)                                                                                      | 1.2+                           |
| [Log4j 2](https://logging.apache.org/log4j/2.x/)                                                                                      | 2.7+                           |
| [Logback](http://logback.qos.ch/)                                                                                                     | 1.0+                           |
| [MongoDB Drivers](https://mongodb.github.io/mongo-java-driver/)                                                                       | 3.3+                           |
| [Netty](https://github.com/netty/netty)                                                                                               | 3.8+                           |
| [OkHttp](https://github.com/square/okhttp/)                                                                                           | 3.0+                           |
| [Play](https://github.com/playframework/playframework)                                                                                | 2.3+ (not including 2.8.x yet) |
| [Play WS](https://github.com/playframework/play-ws)                                                                                   | 1.0+                           |
| [RabbitMQ Client](https://github.com/rabbitmq/rabbitmq-java-client)                                                                   | 2.7+                           |
| [Ratpack](https://github.com/ratpack/ratpack)                                                                                         | 1.4+                           |
| [Reactor](https://github.com/reactor/reactor-core)                                                                                    | 3.1+                           |
| [Rediscala](https://github.com/etaty/rediscala)                                                                                       | 1.8+                           |
| [Redisson](https://github.com/redisson/redisson)                                                                                      | 3.0+                           |
| [RMI](https://docs.oracle.com/en/java/javase/11/docs/api/java.rmi/java/rmi/package-summary.html)                                      | Java 7+                        |
| [RxJava](https://github.com/ReactiveX/RxJava)                                                                                         | 1.0+                           |
| [Servlet](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/package-summary.html)                                           | 2.2+                           |
| [Spark Web Framework](https://github.com/perwendel/spark)                                                                             | 2.3+                           |
| [Spring Data](https://spring.io/projects/spring-data)                                                                                 | 1.8+                           |
| [Spring Scheduling](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/package-summary.html)       | 3.1+                           |
| [Spring Web MVC](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/mvc/package-summary.html)     | 3.1+                           |
| [Spring Webflux](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/reactive/package-summary.html)        | 5.0+                           |
| [Spymemcached](https://github.com/couchbase/spymemcached)                                                                             | 2.12+                          |
| [Twilio](https://github.com/twilio/twilio-java)                                                                                       | 6.6+ (not including 8.x yet)   |
| [Vert.x](https://vertx.io)                                                                                                            | 3.0+                           |
| [Vert.x RxJava2](https://vertx.io/docs/vertx-rx/java2/)                                                                               | 3.5+                           |

### Disabled Instrumentations

Some instrumentations can produce too many spans and make traces very noisy.
For this reason the following instrumentations are disabled by default:
- `jdbc-datasource` which creates spans whenever `java.sql.DataSource#getConnection` method is called.
- `servlet-filter` which creates spans around Servlet Filter methods.
- `servlet-service` which creates spans around Servlet methods.

To enable them, add `otel.integration.<name>.enabled` system property:
`-Dotel.integration.jdbc-datasource.enabled=true`

### Grizzly Instrumentation

Whenever you use
[Grizzly](https://javaee.github.io/grizzly/httpserverframework.html) for
Servlet-based applications, you get better experience from Servlet-specific
support. As these two instrumentations conflict with each other, more generic
instrumentation for Grizzly http server is disabled by default. If needed,
you can enable it by add the following system property:
`-Dotel.integration.grizzly.enabled=true`

### Suppressing Instrumentation

#### Specific Libraries

You can suppress auto-instrumentation of specific libraries by using
`-Dotel.integration.[id].enabled=false` where `id` is the instrumentation `id`.

#### Specific Classes

You can also exclude specific classes from being instrumented. This can be
useful for a few reasons including:

- To completely silence spans from a given class/package.
- As a quick workaround for an instrumentation bug, when byte code in one specific class is problematic.

> This option should not be used lightly, as it can leave some instrumentation
> partially applied, which could have unknown side-effects.

| System property       | Environment variable  | Purpose                                                                                           |
|-----------------------|-----------------------|---------------------------------------------------------------------------------------------------|
| otel.trace.classes.exclude | OTEL_TRACE_CLASSES_EXCLUDE | Suppresses all instrumentation for specific classes, format is "my.package.MyClass,my.package2.\*" |

### Logger MDC auto-instrumentation

The agent injects several pieces of information about the current span into
each logging event's MDC copy. As a result any services or tools that parse the
application logs can correlate traces/spans with log statements.

- `traceId` (same as `Span.current().getSpanContext().getTraceIdAsHexString()`)
- `spanId` (same as `Span.current().getSpanContext().getSpanIdAsHexString()`)
- `sampled` (same as `Span.current().getSpanContext().isSampled()`)

This information can be included in log statements produced by the logging library
by specifying them in the pattern/format.

Example for Spring Boot configuration (which uses logback):

```properties
logging.pattern.console = %d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg trace_id=%X{trace_id} span_id=%X{span_id} trace_flags=%X{trace_flags} %n
```

#### Supported logging libraries

| Library | Version |
|---------|---------|
| Log4j 1 | 1.2+    |
| Log4j 2 | 2.7+    |
| Logback | 1.0+    |

## Configuration Options

> Configuration parameters names are very likely to change until GA.

### Exporters

The following configuration properties are common to all exporters:

| System property | Environment variable | Purpose                                                                                                                                                 |
|-----------------|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| otel.exporter   | OTEL_EXPORTER        | Exporter to be used, can be a comma-separated list to use multiple exporters. Currently does not support multiple metric exporters. Defaults to `otlp`. |

#### OTLP exporter (both span and metric exporters)

> In order to configure the service name for the OTLP exporter, you must add
`service.name` key to the OpenTelemetry Resource ([see
below](#opentelemetry-resource)). For example:
`OTEL_RESOURCE_ATTRIBUTES=service.name=myservice`.

| System property              | Environment variable        | Purpose                                                               |
|------------------------------|-----------------------------|-----------------------------------------------------------------------|
| otel.exporter=otlp (default) | OTEL_EXPORTER=otlp          | To select OpenTelemetry exporter (default)                            |
| otel.exporter.otlp.endpoint  | OTEL_EXPORTER_OTLP_ENDPOINT | The OTLP endpoint to connect to, default is "localhost:55680"         |
| otel.exporter.otlp.insecure  | OTEL_EXPORTER_OTLP_INSECURE | Whether to enable client transport security for the connection        |
| otel.exporter.otlp.headers   | OTEL_EXPORTER_OTLP_HEADERS  | The key-value pairs separated by semicolon to pass as request headers |
| otel.exporter.otlp.timeout   | OTEL_EXPORTER_OTLP_TIMEOUT  | The max waiting time allowed to send each batch, default is 1000      |

#### Jaeger exporter

> Currently only supports gRPC.

| System property                   | Environment variable              | Purpose                                                                                            |
|-----------------------------------|-----------------------------------|----------------------------------------------------------------------------------------------------|
| otel.exporter=jaeger              | OTEL_EXPORTER=jaeger              | To select Jaeger exporter                                                                          |
| otel.exporter.jaeger.endpoint     | OTEL_EXPORTER_JAEGER_ENDPOINT     | The Jaeger endpoint to connect to, default is "localhost:14250", currently only gRPC is supported. |
| otel.exporter.jaeger.service.name | OTEL_EXPORTER_JAEGER_SERVICE_NAME | The service name of this JVM instance, default is "unknown".                                       |

#### Zipkin exporter

| System property                   | Environment variable              | Purpose                                                                                                               |
|-----------------------------------|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| otel.exporter=zipkin              | OTEL_EXPORTER=zipkin              | To select Zipkin exporter                                                                                             |
| otel.exporter.zipkin.endpoint     | OTEL_EXPORTER_ZIPKIN_ENDPOINT     | The Zipkin endpoint to connect to, default is "http://localhost:9411/api/v2/spans". Currently only HTTP is supported. |
| otel.exporter.zipkin.service.name | OTEL_EXPORTER_ZIPKIN_SERVICE_NAME | The service name of this JVM instance, default is "unknown".                                                          |

#### Prometheus exporter

| System property               | Environment variable          | Purpose                                                                            |
|-------------------------------|-------------------------------|------------------------------------------------------------------------------------|
| otel.exporter=prometheus      | OTEL_EXPORTER=prometheus      | To select Prometheus exporter                                                      |
| otel.exporter.prometheus.port | OTEL_EXPORTER_PROMETHEUS_PORT | The local port used to bind the prometheus metric server, defaults to 9464         |
| otel.exporter.prometheus.host | OTEL_EXPORTER_PROMETHEUS_HOST | The local address used to bind the prometheus metric server, defaults to "0.0.0.0" |

#### Logging exporter

> The logging exporter simply prints the name of the span along with its
attributes to stdout. It is used mainly for testing and debugging.

| System property              | Environment variable         | Purpose                                                                      |
|------------------------------|------------------------------|------------------------------------------------------------------------------|
| otel.exporter=logging        | OTEL_EXPORTER=logging        | To select logging exporter                                                   |
| otel.exporter.logging.prefix | OTEL_EXPORTER_LOGGING_PREFIX | An optional string that is printed in front of the span name and attributes. |

### Propagator

The propagator controls which distributed tracing header format is used.

If this is set to a comma-delimited list of the values, the multi-propagator
will be used. The multi-propagator will try to extract the context from
incoming requests using each of the configured propagator formats (in order),
stopping after the first successful context extraction. The multi-propagator
will inject the context into outgoing requests using all the configured
propagator formats.

| System property  | Environment variable | Purpose                                                                                                     |
|------------------|----------------------|-------------------------------------------------------------------------------------------------------------|
| otel.propagators | OTEL_PROPAGATORS     | Default is "tracecontext" (W3C). Other supported values are "b3", "b3single", "jaeger", "ottracer", "xray". |

### OpenTelemetry Resource

The [OpenTelemetry
Resource](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/sdk.md)
is a representation of the entity producing telemetry.

| System property          | Environment variable     | Purpose                                                                      |
|--------------------------|--------------------------|------------------------------------------------------------------------------|
| otel.resource.attributes | OTEL_RESOURCE_ATTRIBUTES | Used to specify resource attributes in format: key1=val1,key2=val2,key3=val3 |

### Peer service name

The [peer service
name](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/semantic_conventions/span-general.md#general-remote-service-attributes)
is the name of a remote service that is being connected to. It corresponds to
`service.name` in the
[Resource](https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/resource/semantic_conventions#service)
for the local service.

| System property                     | Environment variable              | Purpose                                                                      |
|------------------------------------|------------------------------------|------------------------------------------------------------------------------|
| otel.endpoint.peer.service.mapping | OTEL_ENDPOINT_PEER_SERVICE_MAPPING | Used to specify a mapping from hostnames or IP addresses to peer services, as a comma separated list of host=name pairs. The peer service name will be added as an attribute to a span whose host or IP match the mapping. For example, if set to 1.2.3.4=cats-service,dogs-abcdef123.serverlessapis.com=dogs-api, requests to `1.2.3.4` will have a `peer.service` attribute of `cats-service` and requests to `dogs-abcdef123.serverlessapis.com` will have one of `dogs-api` |

### Batch span processor

| System property           | Environment variable      | Purpose                                                                      |
|---------------------------|---------------------------|------------------------------------------------------------------------------|
| otel.bsp.schedule.delay   | OTEL_BSP_SCHEDULE_DELAY   | The interval in milliseconds between two consecutive exports (default: 5000) |
| otel.bsp.max.queue        | OTEL_BSP_MAX_QUEUE        | Maximum queue size (default: 2048)                                           |
| otel.bsp.max.export.batch | OTEL_BSP_MAX_EXPORT_BATCH | Maximum batch size (default: 512)                                            |
| otel.bsp.export.timeout   | OTEL_BSP_EXPORT_TIMEOUT   | Maximum allowed time in milliseconds to export data (default: 30000)         |
| otel.bsp.export.sampled   | OTEL_BSP_EXPORT_SAMPLED   | Whether only sampled spans should be exported (default: true)                |

### Trace config

| System property                 | Environment variable            | Purpose                                              |
|---------------------------------|---------------------------------|------------------------------------------------------|
| otel.config.sampler.probability | OTEL_CONFIG_SAMPLER_PROBABILITY | Sampling probability between 0 and 1 (default: 1)    |
| otel.config.max.attrs           | OTEL_CONFIG_MAX_ATTRS           | Maximum number of attributes per span (default: 32)  |
| otel.config.max.events          | OTEL_CONFIG_MAX_EVENTS          | Maximum number of events per span (default: 128)     |
| otel.config.max.links           | OTEL_CONFIG_MAX_LINKS           | Maximum number of links per span (default: 32)       |
| otel.config.max.event.attrs     | OTEL_CONFIG_MAX_EVENT_ATTRS     | Maximum number of attributes per event (default: 32) |
| otel.config.max.link.attrs      | OTEL_CONFIG_MAX_LINK_ATTRS      | Maximum number of attributes per link (default: 32)  |

### Interval metric reader

| System property          | Environment variable     | Purpose                                                                      |
|--------------------------|--------------------------|------------------------------------------------------------------------------|
| otel.imr.export.interval | OTEL_IMR_EXPORT_INTERVAL | The interval in milliseconds between pushes to the exporter (default: 60000) |

#### Customizing the OpenTelemetry SDK

> This is highly advanced behavior and still in the prototyping phase. It may
change drastically or be removed completely. Use with caution

The OpenTelemetry API exposes SPI
[hooks](https://github.com/open-telemetry/opentelemetry-java/blob/main/api/src/main/java/io/opentelemetry/trace/spi/TracerProviderFactory.java)
for customizing its behavior, such as the `Resource` attached to spans or the
`Sampler`.

Because the auto instrumentation runs in a separate classpath than the
instrumented application, it is not possible for customization in the
application to take advantage of this customization. In order to provide such
customization, you can provide the path to a JAR file including an SPI
implementation using the system property `otel.initializer.jar`. Note that this
JAR will need to shade the OpenTelemetry API in the same way as the agent does.
The simplest way to do this is to use the same shading configuration as the
agent from
[here](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/cfade733b899a2f02cfec7033c6a1efd7c54fd8b/java-agent/java-agent.gradle#L39).
In addition, you will have to specify the
`io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.spi.TraceProvider`
to the name of the class that implements the SPI.

## Manual Instrumentation

> Starting with 0.6.0, and prior to version 1.0.0, `opentelemetry-javaagent-all.jar`
only supports manual instrumentation using the `opentelemetry-api` version with the same version
number as the Java agent you are using. Starting with 1.0.0, the Java agent will start supporting
multiple (1.0.0+) versions of `opentelemetry-api`.

With auto-instrumentation it may be desirable to also add manual
instrumentation. In this scenario, it is critical that both automatic and
manual instrumentation stitch together properly. Two options exist to configure
manual instrumentation with the appropriate span context:

- `@WithSpan` annotation: Simply annotate the functions or methods you wish to instrument
- `getTracer`: Traditional way to instrument with OpenTelemetry

Both options require a dependency on the `opentelemetry-api` library to get started.

### Dependency

#### Maven

```xml
  <dependencies>
    <dependency>
      <groupId>io.opentelemetry</groupId>
      <artifactId>opentelemetry-api</artifactId>
      <version>0.10.0</version>
    </dependency>
  </dependencies>
```

#### Gradle

```groovy
dependencies {
    compile('io.opentelemetry:opentelemetry-api:0.10.0')
}
```

### Configure `@WithSpan` annotation

Add the trace annotation to your application's code. Then, each time the
application invokes the annotated method, it creates a span that denotes its
duration and provides any thrown exceptions.

```java
import io.opentelemetry.extensions.auto.annotations.WithSpan;

public class MyClass {
  @WithSpan
  public void MyLogic() {
      <...>
  }
}
```

#### Dependency

An additional dependency is required for this annotation:

##### Maven

```xml
  <dependencies>
    <dependency>
      <groupId>io.opentelemetry</groupId>
      <artifactId>opentelemetry-extension-auto-annotations</artifactId>
      <version>0.10.0</version>
    </dependency>
  </dependencies>
```

##### Groovy

```groovy
dependencies {
    compile('io.opentelemetry:opentelemetry-extension-auto-annotations:0.10.0')
}
```

#### Suppressing `@WithSpan` instrumentation

This is useful in case you have code that is over-instrumented using `@WithSpan`,
and you want to suppress some of them without modifying the code.

| System property                 | Environment variable            | Purpose                                                                                                                                  |
|---------------------------------|---------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| trace.annotated.methods.exclude | TRACE_ANNOTATED_METHODS_EXCLUDE | Suppress `@WithSpan` instrumentation for specific methods, format is "my.package.MyClass1[method1,method2];my.package.MyClass2[method3]" |

### Configure the OpenTelemetry getTracer

See the [Manual Instrumentation documentation](/docs/java/manual_instrumentation/#instantiate-tracer)
for configuration information and examples.

## Troubleshooting

To turn on the agent's internal debug logging:

`-Dio.opentelemetry.javaagent.slf4j.simpleLogger.defaultLogLevel=debug`

> Note these logs are extremely verbose. Enable debug logging only when needed.
Debug logging negatively impacts the performance of your application.
