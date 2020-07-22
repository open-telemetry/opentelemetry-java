# OpenTelemetry SDK Contrib - zPages

[![Javadocs][javadoc-image]][javadoc-url]

This module contains code for OpenTelemetry's Java zPages, which are a collection of dynamic HTML
web pages that display stats and trace data.

* Java 7 compatible.

<!--- TODO: Update javadoc -->
[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-contrib-auto-config.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-contrib-auto-config

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-api</artifactId>
        <version>0.7.0</version>
      </dependency>
      <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>0.7.0</version>
      </dependency>
      <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk-extension-zpages</artifactId>
        <version>0.7.0</version>
      </dependency>
</dependencies>
```

<!--- TODO: Verify gradle configuration -->
For Gradle add to your dependencies:
```groovy
api 'io.opentelemetry:opentelemetry-api:0.7.0'
api 'io.opentelemetry:opentelemetry-sdk:0.7.0'
implementation 'io.opentelemetry:opentelemetry-sdk-extension-zpages:0.7.0'
```

### Register the zPages

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    ZPageServer.startHttpServerAndRegisterAllPages(8080);
    // ... do work
  }
}
```

### Access the zPages

#### View trace spans on the /tracez zPage

The /tracez zPage displays information on running spans, sample latencies, and sample error spans.
Example:

![tracez-example](screenshots/tracez-example.png)

#### View and update the tracing configuration on the /traceconfigz zPage

The /traceconfigz zPage displays information about the current active tracing configuration and 
allows users to change it. Example:

![traceconfigz-example](screenshots/traceconfigz-example.png)