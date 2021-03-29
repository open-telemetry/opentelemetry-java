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
| Beta   | Alpha   | Experimental |

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

Published releases are available on maven central.

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
	implementation('io.opentelemetry:opentelemetry-api:0.10.0')
}
```

#### Other

  - [releases](https://github.com/open-telemetry/opentelemetry-java/releases)
  - [maven](https://mvnrepository.com/artifact/io.opentelemetry)
  - [bintray](https://bintray.com/open-telemetry/maven/opentelemetry-java)

### Additional Information

- [Javadoc](https://www.javadoc.io/doc/io.opentelemetry)
- [Example code](https://github.com/open-telemetry/opentelemetry-java/tree/main/examples)

## opentelemetry-java-instrumentation

| Traces | Metrics | Logs         |
| ------ | ------- | ------------ |
| Beta   | Alpha   | Experimental |

### Releases

> Published releases are *NOT* available on maven central, but will be by GA.

  - [releases](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases)
  - [bintray](https://bintray.com/open-telemetry/maven/opentelemetry-java-instrumentation)

> Snapshots are also available as documented
> [here](https://github.com/open-telemetry/opentelemetry-java/blob/main/CONTRIBUTING.md#snapshots).

### Additional Information

- [Javadoc](https://www.javadoc.io/doc/io.opentelemetry)
- [Example code](https://github.com/open-telemetry/opentelemetry-java/tree/main/examples)

## opentelemetry-java-contrib

| Component         | Status |
| ----------------- | ------ |
| JMX Metric Gather | Alpha  |

### Releases

  - [releases](https://github.com/open-telemetry/opentelemetry-java-contrib/releases)
