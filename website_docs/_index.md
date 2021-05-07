---
title: "Java"
weight: 18
description: >
  <img width="35" src="https://raw.github.com/open-telemetry/opentelemetry.io/main/iconography/32x32/Java_SDK.svg"></img>
  A language-specific implementation of OpenTelemetry in Java.
---

OpenTelemetry Java consists of the following repositories:

- [opentelemetry-java](https://github.com/open-telemetry/opentelemetry-java):
  Components for manual instrumentation including API and SDK as well as
  extensions, the OpenTracing shim and examples.
- [opentelemetry-java-instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation):
  Built on top of opentelemetry-java and provides a Java agent JAR that can be
  attached to any Java 8+ application and dynamically injects bytecode to
  capture telemetry from a number of popular libraries and frameworks.
- [opentelemetry-java-contrib](https://github.com/open-telemetry/opentelemetry-java-contrib):
  Provides helpful libraries and standalone OpenTelemetry-based utilities that
  don't fit the express scope of the OpenTelemetry Java or Java Instrumentation
  projects. For example, JMX metric gathering.

## opentelemetry-java

| Traces | Metrics | Logs         |
| ------ | ------- | ------------ |
| Stable | Alpha   | Experimental |

### Components

- Tracing API
- Tracing SDK
- Metrics API
- Metrics SDK
- OTLP Exporter
- Jaeger Trace Exporter
- Zipkin Trace Exporter
- Prometheus Metric Exporter
- Context Propagation
- OpenTracing Bridge
- OpenCensus Bridge

### Releases

Published releases are available on maven central. We strongly recommend using our BOM to keep the
versions of the various components in sync.

#### Maven

```xml
<project>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-bom</artifactId>
        <version>1.2.0</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.opentelemetry</groupId>
      <artifactId>opentelemetry-api</artifactId>
    </dependency>
  </dependencies>
</project>
```

#### Gradle

```groovy
dependencies {
  implementation platform("io.opentelemetry:opentelemetry-bom:1.2.0")
  implementation('io.opentelemetry:opentelemetry-api')
}
```

#### Other

  - [releases](https://github.com/open-telemetry/opentelemetry-java/releases)
  - [maven](https://mvnrepository.com/artifact/io.opentelemetry)

### Additional Information

- [Javadoc](https://www.javadoc.io/doc/io.opentelemetry)
- [Example code](https://github.com/open-telemetry/opentelemetry-java/tree/main/examples)
