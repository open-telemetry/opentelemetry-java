# OpenTelemetry for Java
[![Continuous Build][ci-image]][ci-url]
[![Coverage Status][codecov-image]][codecov-url]
[![Maven Central][maven-image]][maven-url]

## Getting Started

If you are looking for an all-in-one, easy-to-install **auto-instrumentation
javaagent**, see [opentelemetry-java-instrumentation][].

If you are looking for **examples** on how to use the OpenTelemetry API to
write your own **manual instrumentation**, or how to set up the OpenTelemetry
Java SDK, see [Manual instrumentation][]. Fully-functional examples
are available in [opentelemetry-java-docs][].

For a general overview of OpenTelemetry, visit [opentelemetry.io][].

Would you like to get involved with the project? Read our [contributing guide](CONTRIBUTING.md). We welcome
contributions!

## Contacting us

We hold regular meetings. See details at [community page](https://github.com/open-telemetry/community#java-sdk).

We use [GitHub Discussions](https://github.com/open-telemetry/opentelemetry-java/discussions)
for support or general questions. Feel free to drop us a line.

We are also present in the `#otel-java` channel in the [CNCF slack](https://slack.cncf.io/).
Please join us for more informal discussions.

## Overview

OpenTelemetry is the merging of OpenCensus and OpenTracing into a single project.

This project contains the following top level components:

* [OpenTelemetry API](api/):
  * [stable apis](api/all/src/main/java/io/opentelemetry/api/) including `Tracer`, `Span`, `SpanContext`, `Meter`, and `Baggage`
  * [semantic conventions](semconv/) Generated code for the OpenTelemetry semantic conventions.
  * [context api](context/src/main/java/io/opentelemetry/context/) The OpenTelemetry Context implementation.
* [extensions](extensions/) define additional API extensions, which are not part of the core API.
* [sdk](sdk/) defines the implementation of the OpenTelemetry API.
* [sdk-extensions](sdk-extensions/) defines additional SDK extensions, which are not part of the core SDK.
* [OpenTracing shim](opentracing-shim/) defines a bridge layer from OpenTracing to the OpenTelemetry API.
* [OpenCensus shim](opencensus-shim/) defines a bridge layer from OpenCensus to the OpenTelemetry API.

This project publishes a lot of artifacts. The easiest way to see the most recent stable artifacts is to use the
[`opentelemetry-bom`](https://mvnrepository.com/artifact/io.opentelemetry/opentelemetry-bom). Unstable artifacts are referenced by
the [`opentelemetry-bom-alpha`](https://mvnrepository.com/artifact/io.opentelemetry/opentelemetry-bom-alpha).

We would love to hear from the larger community: please provide feedback proactively.

## Requirements

Unless otherwise noted, all published artifacts support Java 8 or higher.

**Android Disclaimer:** For compatibility reasons, [library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) must be enabled.

See [CONTRIBUTING.md](./CONTRIBUTING.md) for additional instructions for building this project for development.

### Note about extensions

Both API and SDK extensions consist of various additional components which are excluded from the core artifacts
to keep them from growing too large.

We still aim to provide the same level of quality and guarantee for them as for the core components.
Please don't hesitate to use them if you find them useful.

## Project setup and contributing

Please refer to the [contribution guide](CONTRIBUTING.md) on how to set up for development and contribute!

## Published Releases

Published releases are available on maven central. We strongly recommend using our published BOM to keep all
dependency versions in sync.

### Maven

```xml
<project>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-bom</artifactId>
        <version>1.12.0</version>
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

### Gradle

```groovy
dependencies {
  implementation platform("io.opentelemetry:opentelemetry-bom:1.12.0")
  implementation('io.opentelemetry:opentelemetry-api')
}
```

Note that if you want to use any artifacts that have not fully stabilized yet (such as metrics), then you will need to add an entry for the Alpha BOM as well, e.g.

```groovy
dependencies {
  implementation platform("io.opentelemetry:opentelemetry-bom:1.12.0")
  implementation platform('io.opentelemetry:opentelemetry-bom-alpha:1.12.0-alpha')

  implementation('io.opentelemetry:opentelemetry-api')
  implementation('io.opentelemetry:opentelemetry-api-metrics')
}
```

## Snapshots

Snapshots based out the `main` branch are available for `opentelemetry-api`, `opentelemetry-sdk` and the rest of the artifacts.
We strongly recommend using our published BOM to keep all dependency versions in sync.

### Maven

```xml
<project>
  <repositories>
    <repository>
      <id>oss.sonatype.org-snapshot</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </repository>
  </repositories>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-bom</artifactId>
        <version>1.13.0-SNAPSHOT</version>
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

### Gradle

```groovy
repositories {
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
}

dependencies {
  implementation platform("io.opentelemetry:opentelemetry-bom:1.13.0-SNAPSHOT")
  implementation('io.opentelemetry:opentelemetry-api')
}
```

Libraries will usually only need `opentelemetry-api`, while applications
will want to use the `opentelemetry-sdk` module which contains our standard implementation
of the APIs.

## Gradle composite builds

For opentelemetry-java developers that need to test the latest source code with another
project, composite builds can be used as an alternative to `publishToMavenLocal`. This
requires some setup which is explained [here](CONTRIBUTING.md#composing-builds).

## Releases

See the [VERSIONING.md](VERSIONING.md) document for our policies for releases and compatibility
guarantees.

Check out information about the [latest release](https://github.com/open-telemetry/opentelemetry-java/releases).

This is a **current** feature status list:

| Component                   | Version                                                      |
|-----------------------------|--------------------------------------------------------------|
| Trace API                   | v<!--VERSION_STABLE-->1.12.0<!--/VERSION_STABLE-->           |
| Trace SDK                   | v<!--VERSION_STABLE-->1.12.0<!--/VERSION_STABLE-->           |
| Context                     | v<!--VERSION_STABLE-->1.12.0<!--/VERSION_STABLE-->           |
| Baggage                     | v<!--VERSION_STABLE-->1.12.0<!--/VERSION_STABLE-->           |
| Jaeger Trace Exporter       | v<!--VERSION_STABLE-->1.12.0<!--/VERSION_STABLE-->           |
| Zipkin Trace Exporter       | v<!--VERSION_STABLE-->1.12.0<!--/VERSION_STABLE-->           |
| OTLP Exporter (Spans)       | v<!--VERSION_STABLE-->1.12.0<!--/VERSION_STABLE-->           |
| Metrics API                 | v<!--VERSION_STABLE-->1.12.0<!--/VERSION_STABLE-->           |
| OTLP Exporter (Metrics)     | v<!--VERSION_UNSTABLE-->1.12.0-alpha<!--/VERSION_UNSTABLE--> |
| Metrics SDK                 | v<!--VERSION_UNSTABLE-->1.12.0-alpha<!--/VERSION_UNSTABLE--> |
| OTLP Exporter (Logs)        | v<!--VERSION_UNSTABLE-->1.12.0-alpha<!--/VERSION_UNSTABLE--> |
| Logs SDK                    | v<!--VERSION_UNSTABLE-->1.12.0-alpha<!--/VERSION_UNSTABLE--> |
| Prometheus Metrics Exporter | v<!--VERSION_UNSTABLE-->1.12.0-alpha<!--/VERSION_UNSTABLE--> |
| OpenTracing Bridge          | v<!--VERSION_UNSTABLE-->1.12.0-alpha<!--/VERSION_UNSTABLE--> |
| OpenCensus Bridge           | v<!--VERSION_UNSTABLE-->1.12.0-alpha<!--/VERSION_UNSTABLE--> |

See the project [milestones](https://github.com/open-telemetry/opentelemetry-java/milestones)
for details on upcoming releases. The dates and features described in issues
and milestones are estimates, and subject to change.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

Approvers ([@open-telemetry/java-approvers](https://github.com/orgs/open-telemetry/teams/java-approvers)):

- [Christian Neumüller](https://github.com/Oberon00), Dynatrace
- [Jakub Wach](https://github.com/kubawach), Splunk
- [Josh Suereth](https://github.com/jsuereth), Google

*Find more about the approver role in [community repository](https://github.com/open-telemetry/community/blob/master/community-membership.md#approver).*

Maintainers ([@open-telemetry/java-maintainers](https://github.com/orgs/open-telemetry/teams/java-maintainers)):

- [Anuraag Agrawal](https://github.com/anuraaga), AWS
- [Jack Berg](https://github.com/jack-berg), New Relic
- [John Watson](https://github.com/jkwatson), Splunk

Maintainers Emeritus:

- [Bogdan Drutu](https://github.com/BogdanDrutu), Splunk
- [Carlos Alberto](https://github.com/carlosalberto), LightStep

*Find more about the maintainer role in [community repository](https://github.com/open-telemetry/community/blob/master/community-membership.md#maintainer).*

### Thanks to all the people who have contributed

<a href="https://github.com/open-telemetry/opentelemetry-java/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=open-telemetry/opentelemetry-java" />
</a>

Made with [contrib.rocks](https://contrib.rocks).

[ci-image]: https://github.com/open-telemetry/opentelemetry-java/workflows/Continuous%20Build/badge.svg
[ci-url]: https://github.com/open-telemetry/opentelemetry-java/actions?query=workflow%3Aci+branch%3Amain
[codecov-image]: https://codecov.io/gh/open-telemetry/opentelemetry-java/branch/main/graph/badge.svg
[codecov-url]: https://codecov.io/gh/open-telemetry/opentelemetry-java/branch/main/
[Manual instrumentation]: https://opentelemetry.io/docs/java/manual_instrumentation/
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opentelemetry/opentelemetry-api/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opentelemetry/opentelemetry-api
[opentelemetry-java-instrumentation]: https://github.com/open-telemetry/opentelemetry-java-instrumentation
[opentelemetry-java-docs]: https://github.com/open-telemetry/opentelemetry-java-docs
[opentelemetry.io]: https://opentelemetry.io
