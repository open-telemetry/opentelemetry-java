# OpenTelemetry
[![Gitter chat][gitter-image]][gitter-url]
[![Build Status][circleci-image]][circleci-url]
[![Coverage Status][codecov-image]][codecov-url]
[![Maven Central][maven-image]][maven-url]

OpenTelemetry is a working name of a combined OpenCensus and OpenTracing
project.

This project contains the following top level components:

* [api](api/): The OpenTelemetry API.
  * [trace](api/src/main/java/io/opentelemetry/trace/): The tracing api. Includes `Tracer`, `Span` and `SpanContext`.
  * [distributedcontext](api/src/main/java/io/opentelemetry/distributedcontext/): Collection of entries in the form of key-value pairs of data that can be propagated to provide contextual information.
  * [context](api/src/main/java/io/opentelemetry/context/): In-process and inter-process propagation layer.
  * [metrics](api/src/main/java/io/opentelemetry/metrics/): Metrics layer.
* [sdk](sdk/): The reference implementation complying to the OpenTelemetry API.
* [OpenTracing shim](opentracing_shim/): A bridge layer from OpenTelemetry to the OpenTracing API.

We would love to hear from the larger community: please provide feedback proactively.

## Project setup and contribute

Please refer to the [contribution guide](https://github.com/newrelic-forks/opentelemetry-java/blob/master/CONTRIBUTING.md)
on how to setup and contribute!

## Snapshots

Snapshots based out the `master` branch are available for `opentelemetry-api`, `opentelemetry-sdk` and the rest of the artifacts:

### Maven

```xml
  <repositories>
    <repository>
      <id>oss.sonatype.org-snapshot</id>
      <url>https://oss.jfrog.org/artifactory/oss-snapshot-local</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>io.opentelemetry</groupId>
      <artifactId>opentelemetry-api</artifactId>
      <version>0.1.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
```

### Gradle

```groovy
repositories {
	maven { url 'https://oss.jfrog.org/artifactory/oss-snapshot-local' }
}

dependencies {
	compile('io.opentelemetry:opentelemetry-api:0.1.0-SNAPSHOT')
}
```

Libraries will usually only need `opentelemetry-api`, while applications
may want to use `opentelemetry-sdk`.

## Release Schedule

OpenTelemetry Java is under active development. Our goal is to release an
_alpha_ version of the library by middle October 2019. This release isn't
guaranteed to conform to a specific version of the specification, and future
releases will not attempt to maintain backwards compatibility with the alpha
release.

| Component                   | Version | Target Date       |
| --------------------------- | ------- | ----------------- |
| Tracing API                 | Alpha   | October 21 2019   |
| Tracing SDK                 | Alpha   | October 21 2019   |
| Metrics API                 | Alpha   | October 21 2019   |
| Metrics SDK                 | Alpha   | October 21 2019   |
| Jaeger Trace Exporter       | Alpha   | October 21 2019   |
| Zipkin Trace Exporter       | Alpha   | Unknown           |
| Prometheus Metrics Exporter | Alpha   | Unknown           |
| Context Propagation         | Alpha   | October 21 2019   |
| OpenTracing Bridge          | Alpha   | October 21 2019   |
| OpenCensus Bridge           | Alpha   | Unknown           |

### Kick off the work towards 1.0

After beta release we will encourage customers and tracing vendors to start
using the new project, providing feedback as they go. So we can ensure a high
quality v1.0 for the merged project:

- We will allow ourselves to break *implementations*, but not people using the
  public Interfaces.
- Additions (into interfaces for instance) will involve a best-effort attempt at
  backwards compatibility (again, for implementations – callers of the public
  APIs should not be negatively affected by these additions).

### Summary

We plan to merge projects and pave the path for future improvements as a unified
community of tracing vendors, users and library authors who wants apps be
managed better. We are open to feedback and suggestions from all of you!

[circleci-image]: https://circleci.com/gh/open-telemetry/opentelemetry-java.svg?style=svg 
[circleci-url]: https://circleci.com/gh/open-telemetry/opentelemetry-java
[gitter-image]: https://badges.gitter.im/open-telemetry/opentelemetry-java.svg 
[gitter-url]: https://gitter.im/open-telemetry/opentelemetry-java?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge
[codecov-image]: https://codecov.io/gh/open-telemetry/opentelemetry-java/branch/master/graph/badge.svg
[codecov-url]: https://codecov.io/gh/open-telemetry/opentelemetry-java/branch/master/
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opentelemetry/opentelemetry-api/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opentelemetry/opentelemetry-api
