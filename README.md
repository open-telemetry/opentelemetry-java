# OpenTelemetry Java
[![Continuous Build][ci-image]][ci-url]
[![Coverage Status][codecov-image]][codecov-url]
[![Maven Central][maven-image]][maven-url]

> [!IMPORTANT]
> We are currently seeking additional contributors! See [help wanted](#help-wanted) for details.

## Project Status

See [Java status on OpenTelemetry.io][otel-java-status].

## Getting Started

If you are looking for an all-in-one, easy-to-install **auto-instrumentation
javaagent**, see [opentelemetry-java-instrumentation][].

If you are looking for **examples** on how to use the OpenTelemetry API to
write your own **manual instrumentation**, or how to set up the OpenTelemetry
Java SDK, see [Manual instrumentation][]. Fully-functional examples
are available in [opentelemetry-java-examples][].

If you are looking for generated classes for
the [OpenTelemetry semantic conventions][opentelemetry-semantic-conventions],
see [semantic-conventions-java][opentelemetry-semantic-conventions-java].

For a general overview of OpenTelemetry, visit [opentelemetry.io][].

Would you like to get involved with the project? Read our [contributing guide](CONTRIBUTING.md). We welcome
contributions!

## Contacting us

We hold regular meetings. See details at [community page](https://github.com/open-telemetry/community#java-sdk).

We use [GitHub Discussions](https://github.com/open-telemetry/opentelemetry-java/discussions)
for support or general questions. Feel free to drop us a line.

We are also present in the [`#otel-java`](https://cloud-native.slack.com/archives/C014L2KCTE3) channel in the [CNCF slack](https://slack.cncf.io/).
Please join us for more informal discussions.

To report a bug, or request a new feature,
please [open an issue](https://github.com/open-telemetry/opentelemetry-java/issues/new/choose).

## Overview

OpenTelemetry is the merging of OpenCensus and OpenTracing into a single project.

This project contains the following top level components:

* [OpenTelemetry API](api/):
  * [stable apis](api/all) including `Tracer`, `Span`, `SpanContext`, `Meter`, and `Baggage`.
  * [context api](context/) The OpenTelemetry Context implementation.
  * [incubating apis](api/incubator) incubating APIs, including `Events`.
* [extensions](extensions/) define additional API extensions not part of the core API, including propagators.
* [sdk](sdk/) defines the implementation of the OpenTelemetry API.
* [exporters](exporters/) trace, metric, and log exporters for the SDK.
* [sdk-extensions](sdk-extensions/) defines additional SDK extensions, which are not part of the core SDK.
* [OpenTracing shim](opentracing-shim/) defines a bridge layer from OpenTracing to the OpenTelemetry API.
* [OpenCensus shim](opencensus-shim/) defines a bridge layer from OpenCensus to the OpenTelemetry API.

This project publishes a lot of artifacts, listed in [releases](#releases).
[`opentelemetry-bom`](https://mvnrepository.com/artifact/io.opentelemetry/opentelemetry-bom) (BOM =
Bill of Materials) is provided to assist with synchronizing versions of
dependencies. [`opentelemetry-bom-alpha`](https://mvnrepository.com/artifact/io.opentelemetry/opentelemetry-bom-alpha)
provides the same function for unstable artifacts. See [published releases](#published-releases) for
instructions on using the BOMs.

We would love to hear from the larger community: please provide feedback proactively.

## Requirements

Unless otherwise noted, all published artifacts support Java 8 or higher.
See [language version compatibility](VERSIONING.md#language-version-compatibility) for complete
details.

**Android Disclaimer:** For compatibility
reasons, [library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring)
must be enabled.

See [CONTRIBUTING.md](./CONTRIBUTING.md) for additional instructions for building this project for
development.

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
        <version>1.40.0</version>
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
  implementation platform("io.opentelemetry:opentelemetry-bom:1.40.0")
  implementation('io.opentelemetry:opentelemetry-api')
}
```

Note that if you want to use any artifacts that have not fully stabilized yet (such as the [prometheus exporter](https://github.com/open-telemetry/opentelemetry-java/tree/main/exporters/prometheus), then you will need to add an entry for the Alpha BOM as well, e.g.

```groovy
dependencies {
  implementation platform("io.opentelemetry:opentelemetry-bom:1.40.0")
  implementation platform('io.opentelemetry:opentelemetry-bom-alpha:1.40.0-alpha')

  implementation('io.opentelemetry:opentelemetry-api')
  implementation('io.opentelemetry:opentelemetry-exporter-prometheus')
  implementation('io.opentelemetry:opentelemetry-sdk-extension-autoconfigure')
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
        <version>1.41.0-SNAPSHOT</version>
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
  implementation platform("io.opentelemetry:opentelemetry-bom:1.41.0-SNAPSHOT")
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

See the project [milestones](https://github.com/open-telemetry/opentelemetry-java/milestones)
for details on upcoming releases. The dates and features described in issues
and milestones are estimates, and subject to change.

The following tables describe the artifacts published by this project. To take a dependency, follow
the instructions in [Published Released](#published-releases) to include the BOM, and specify the
dependency as follows, replacing `{{artifact-id}}` with the value from the "Artifact ID" column:

```xml
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>{{artifact-id}}</artifactId>
</dependency>
```

```groovy
  implementation('io.opentelemetry:{{artifact-id}}')
```

### Bill of Material

| Component                                    | Description                            | Artifact ID               | Version                                                     | Javadoc |
|----------------------------------------------|----------------------------------------|---------------------------|-------------------------------------------------------------|---------|
| [Bill of Materials (BOM)](./bom)             | Bill of materials for stable artifacts | `opentelemetry-bom`       | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | N/A     |
| [Alpha Bill of Materials (BOM)](./bom-alpha) | Bill of materials for alpha artifacts  | `opentelemetry-bom-alpha` | <!--VERSION_UNSTABLE-->1.40.0-alpha<!--/VERSION_UNSTABLE--> | N/A     |

### API


| Component                         | Description                                                                          | Artifact ID                   | Version                                           | Javadoc                                                                                                                                                               |
|-----------------------------------|--------------------------------------------------------------------------------------|-------------------------------|---------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [API](./api/all)                  | OpenTelemetry API, including metrics, traces, baggage, context                       | `opentelemetry-api`           | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-api.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api)                     |
| [API  Incubator](./api/incubator) | API incubator, including pass through propagator, and extended tracer, and Event API | `opentelemetry-api-incubator` | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-api-incubator.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api-incubator) |
| [Context API](./context)          | OpenTelemetry context API                                                            | `opentelemetry-context`       | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-context.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-context)             |

### API Extensions

| Component                                                     | Description                                                                                                                                                                             | Artifact ID                                 | Version                                                     | Javadoc                                                                                                                                                                                           |
|---------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------|-------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Kotlin Extension](./extensions/kotlin)                       | Context extension for coroutines                                                                                                                                                        | `opentelemetry-extension-kotlin`            | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-extension-kotlin.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-extension-kotlin)                       |
| [Trace Propagators Extension](./extensions/trace-propagators) | Trace propagators, including B3, Jaeger, OT Trace                                                                                                                                       | `opentelemetry-extension-trace-propagators` | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-extension-trace-propagators.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-extension-trace-propagators) |

### SDK

| Component                    | Description                                            | Artifact ID                 | Version                                           | Javadoc                                                                                                                                                           |
|------------------------------|--------------------------------------------------------|-----------------------------|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [SDK](./sdk/all)             | OpenTelemetry SDK, including metrics, traces, and logs | `opentelemetry-sdk`         | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk)                 |
| [Metrics SDK](./sdk/metrics) | OpenTelemetry metrics SDK                              | `opentelemetry-sdk-metrics` | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-metrics.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-metrics) |
| [Trace SDK](./sdk/trace)     | OpenTelemetry trace SDK                                | `opentelemetry-sdk-trace`   | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-trace.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-trace)     |
| [Log SDK](./sdk/logs)        | OpenTelemetry log SDK                                  | `opentelemetry-sdk-logs`    | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-logs.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-logs)       |
| [SDK Common](./sdk/common)   | Shared SDK components                                  | `opentelemetry-sdk-common`  | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-common.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-common)   |
| [SDK Testing](./sdk/testing) | Components for testing OpenTelemetry instrumentation   | `opentelemetry-sdk-testing` | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-testing.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-testing) |

### SDK Exporters

| Component                                                             | Description                                                                  | Artifact ID                                          | Version                                                     | Javadoc                                                                                                                                                                                                             |
|-----------------------------------------------------------------------|------------------------------------------------------------------------------|------------------------------------------------------|-------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [OTLP Exporters](./exporters/otlp/all)                                | OTLP gRPC & HTTP exporters, including traces, metrics, and logs              | `opentelemetry-exporter-otlp`                        | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-otlp.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-otlp)                                               |
| [OTLP Logging Exporters](./exporters/logging-otlp)                    | Logging exporters in OTLP JSON encoding, including traces, metrics, and logs | `opentelemetry-exporter-logging-otlp`                | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-logging-otlp.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-logging-otlp)                               |
| [OTLP Common](./exporters/otlp/common)                                | Shared OTLP components (internal)                                            | `opentelemetry-exporter-otlp-common`                 | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-otlp-common.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-otlp-common)                                 |
| [Logging Exporter](./exporters/logging)                               | Logging exporters, including metrics, traces, and logs                       | `opentelemetry-exporter-logging`                     | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-logging.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-logging)                                         |
| [Zipkin Exporter](./exporters/zipkin)                                 | Zipkin trace exporter                                                        | `opentelemetry-exporter-zipkin`                      | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-zipkin.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-zipkin)                                           |
| [Prometheus Exporter](./exporters/prometheus)                         | Prometheus metric exporter                                                   | `opentelemetry-exporter-prometheus`                  | <!--VERSION_UNSTABLE-->1.40.0-alpha<!--/VERSION_UNSTABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-prometheus.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-prometheus)                                   |
| [Exporter Common](./exporters/common)                                 | Shared exporter components (internal)                                        | `opentelemetry-exporter-common`                      | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-common.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-common)                                           |
| [OkHttp Sender](./exporters/sender/okhttp)                            | OkHttp implementation of HttpSender (internal)                               | `opentelemetry-exporter-sender-okhttp`               | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-sender-okhttp.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-sender-okhttp)                             |
| [JDK Sender](./exporters/sender/jdk)                                  | Java 11+ native HttpClient implementation of HttpSender (internal)           | `opentelemetry-exporter-sender-jdk`                  | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-sender-jdk.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-sender-jdk)                                   |                                                                                                                                                            |
| [gRPC ManagedChannel Sender](./exporters/sender/grpc-managed-channel) | gRPC ManagedChannel implementation of GrpcSender (internal)                  | `opentelemetry-exporter-sender-grpc-managed-channel` | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-sender-grpc-managed-channel.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-sender-grpc-managed-channel) |                                                                                                                                                            |

### SDK Extensions

| Component                                                                     | Description                                                                        | Artifact ID                                         | Version                                                     | Javadoc                                                                                                                                                                                                           |
|-------------------------------------------------------------------------------|------------------------------------------------------------------------------------|-----------------------------------------------------|-------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [SDK Autoconfigure](./sdk-extensions/autoconfigure)                           | Autoconfigure OpenTelemetry SDK from env vars, system properties, and SPI          | `opentelemetry-sdk-extension-autoconfigure`         | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-autoconfigure.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-autoconfigure)                 |
| [SDK Autoconfigure SPI](./sdk-extensions/autoconfigure-spi)                   | Service Provider Interface (SPI) definitions for autoconfigure                     | `opentelemetry-sdk-extension-autoconfigure-spi`     | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-autoconfigure-spi.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-autoconfigure-spi)         |
| [SDK Jaeger Remote Sampler Extension](./sdk-extensions/jaeger-remote-sampler) | Sampler which obtains sampling configuration from remote Jaeger server             | `opentelemetry-sdk-extension-jaeger-remote-sampler` | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-jaeger-remote-sampler.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-jaeger-remote-sampler) |
| [SDK Incubator](./sdk-extensions/incubator)                                   | SDK incubator, including YAML based view configuration, LeakDetectingSpanProcessor | `opentelemetry-sdk-extension-incubator`             | <!--VERSION_UNSTABLE-->1.40.0-alpha<!--/VERSION_UNSTABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-incubator.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-incubator)                         |

### Shims

| Component                              | Description                                                  | Artifact ID                      | Version                                                     | Javadoc                                                                                                                                                                     |
|----------------------------------------|--------------------------------------------------------------|----------------------------------|-------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [OpenCensus Shim](./opencensus-shim)   | Bridge opencensus metrics into the OpenTelemetry metrics SDK | `opentelemetry-opencensus-shim`  | <!--VERSION_UNSTABLE-->1.40.0-alpha<!--/VERSION_UNSTABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-opencensus-shim.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-opencensus-shim)   |
| [OpenTracing Shim](./opentracing-shim) | Bridge opentracing spans into the OpenTelemetry trace API    | `opentelemetry-opentracing-shim` | <!--VERSION_STABLE-->1.40.0<!--/VERSION_STABLE-->     | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-opentracing-shim.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-opentracing-shim) |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

Triagers:

- [Gregor Zeitlinger](https://github.com/zeitlinger), Grafana Labs

*Find more about the triager role in [community repository](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#triager).*

Approvers ([@open-telemetry/java-approvers](https://github.com/orgs/open-telemetry/teams/java-approvers)):

- [Jason Plumb](https://github.com/breedx-splk), Splunk
- [Josh Suereth](https://github.com/jsuereth), Google
- [Lauri Tulmin](https://github.com/laurit), Splunk
- [Trask Stalnaker](https://github.com/trask), Microsoft

*Find more about the approver role in [community repository](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#approver).*

Maintainers ([@open-telemetry/java-maintainers](https://github.com/orgs/open-telemetry/teams/java-maintainers)):

- [Jack Berg](https://github.com/jack-berg), New Relic
- [John Watson](https://github.com/jkwatson), Verta.ai

Emeritus:

- Maintainer [Bogdan Drutu](https://github.com/BogdanDrutu)
- Maintainer [Carlos Alberto](https://github.com/carlosalberto)
- Approver [Mateusz Rzeszutek](https://github.com/mateuszrzeszutek)

*Find more about the maintainer role in [community repository](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#maintainer).*

### Help wanted

We are currently resource constrained and are actively seeking new contributors interested in working towards [approver](https://github.com/open-telemetry/community/blob/master/community-membership.md#approver) / [maintainer](https://github.com/open-telemetry/community/blob/master/community-membership.md#maintainer) roles. In addition to the documentation for approver / maintainer roles and the [contributing](./CONTRIBUTING.md) guide, here are some additional notes on engaging:

- [Pull request](https://github.com/open-telemetry/opentelemetry-java/pulls) reviews are equally or more helpful than code contributions. Comments and approvals are valuable with or without a formal project role. They're also a great forcing function to explore a fairly complex codebase.
- Attending the [Java: SDK + Automatic Instrumentation](https://github.com/open-telemetry/community?tab=readme-ov-file#implementation-sigs) SIG is a great way to get to know community members and learn about project priorities.
- Issues labeled [help wanted](https://github.com/open-telemetry/opentelemetry-java/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22) are project priorities. Code contributions (or pull request reviews when a PR is linked) for these issues are particularly important.
- Triaging / responding to new issues and discussions is a great way to engage with the project.

### Thanks to all the people who have contributed

<a href="https://github.com/open-telemetry/opentelemetry-java/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=open-telemetry/opentelemetry-java" />
</a>

Made with [contrib.rocks](https://contrib.rocks).

[ci-image]: https://github.com/open-telemetry/opentelemetry-java/workflows/Build/badge.svg
[ci-url]: https://github.com/open-telemetry/opentelemetry-java/actions?query=workflow%3ABuild+branch%3Amain
[codecov-image]: https://codecov.io/gh/open-telemetry/opentelemetry-java/branch/main/graph/badge.svg
[codecov-url]: https://app.codecov.io/gh/open-telemetry/opentelemetry-java/branch/main/
[Manual instrumentation]: https://opentelemetry.io/docs/java/manual_instrumentation/
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opentelemetry/opentelemetry-api/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opentelemetry/opentelemetry-api
[opentelemetry-java-instrumentation]: https://github.com/open-telemetry/opentelemetry-java-instrumentation
[opentelemetry-java-examples]: https://github.com/open-telemetry/opentelemetry-java-examples
[opentelemetry-semantic-conventions]: https://opentelemetry.io/docs/specs/semconv/
[opentelemetry-semantic-conventions-java]: https://github.com/open-telemetry/semantic-conventions-java
[opentelemetry.io]: https://opentelemetry.io
[otel-java-status]: https://opentelemetry.io/docs/instrumentation/java/#status-and-releases
