# OpenTelemetry Java

[![Release](https://img.shields.io/github/v/release/open-telemetry/opentelemetry-java?include_prereleases&style=)](https://github.com/open-telemetry/opentelemetry-java/releases/)
[![Coverage Status][codecov-image]][codecov-url]
[![FOSSA License Status](https://app.fossa.com/api/projects/custom%2B162%2Fgithub.com%2Fopen-telemetry%2Fopentelemetry-java.svg?type=shield&issueType=license)](https://app.fossa.com/projects/custom%2B162%2Fgithub.com%2Fopen-telemetry%2Fopentelemetry-java?ref=badge_shield&issueType=license)
[![FOSSA Security Status](https://app.fossa.com/api/projects/custom%2B162%2Fgithub.com%2Fopen-telemetry%2Fopentelemetry-java.svg?type=shield&issueType=security)](https://app.fossa.com/projects/custom%2B162%2Fgithub.com%2Fopen-telemetry%2Fopentelemetry-java?ref=badge_shield&issueType=security)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/open-telemetry/opentelemetry-java/badge)](https://scorecard.dev/viewer/?uri=github.com/open-telemetry/opentelemetry-java)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/io/opentelemetry/java/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/io/opentelemetry/java/README.md)

`opentelemetry-java` is the home of the Java implementation of the OpenTelemetry API for recording
telemetry, and SDK for managing telemetry recorded by the API.

See [opentelemetry.io Java Documentation](https://opentelemetry.io/docs/languages/java/intro/) for:

* An overview of the OpenTelemetry Java ecosystem and key repositories
* Detailed documentation on the components published from this repository
* Review of instrumentation ecosystem, including OpenTelemetry Java agent
* End-to-end working code examples
* And more

> [!IMPORTANT]
> We are currently seeking additional contributors! See [help wanted](#help-wanted) for details.

## Requirements

Unless otherwise noted, all published artifacts support Java 8 or higher.
See [language version compatibility](VERSIONING.md#language-version-compatibility) for complete
details.

**Android Disclaimer:** For compatibility
reasons, [library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring)
must be enabled.

See [contributing](#contributing) for details on building this project locally.

## Releases

Releases are published to maven central. We
publish [minor releases monthly](RELEASING.md#release-cadence)
and [patch releases as needed](RELEASING.md#preparing-a-new-patch-release).

See [releases](https://github.com/open-telemetry/opentelemetry-java/releases) for a listing of
released versions and notes (see also [changelog](CHANGELOG.md)).

## Artifacts

The artifacts published by this repository are summarized below in tables, organized in collapsible
sections by topic.

As discussed in [compatibility](#compatibility), artifact versions must be kept in sync, for which
we strongly recommend [using one of our BOMs][dependencies-and-boms].

<details>
  <summary>Bill of Materials (BOMs)</summary>

A bill of materials (or BOM) helps sync dependency versions of related artifacts.

| Component                                    | Description                            | Artifact ID               | Version                                                     | Javadoc |
|----------------------------------------------|----------------------------------------|---------------------------|-------------------------------------------------------------|---------|
| [Bill of Materials (BOM)](./bom)             | Bill of materials for stable artifacts | `opentelemetry-bom`       | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | N/A     |
| [Alpha Bill of Materials (BOM)](./bom-alpha) | Bill of materials for alpha artifacts  | `opentelemetry-bom-alpha` | <!--VERSION_UNSTABLE-->1.53.0-alpha<!--/VERSION_UNSTABLE--> | N/A     |
</details>

<details open>
  <summary>API</summary>

The OpenTelemetry API for recording telemetry.

| Component                         | Description                                                                          | Artifact ID                   | Version                                                     | Javadoc                                                                                                                                                               |
|-----------------------------------|--------------------------------------------------------------------------------------|-------------------------------|-------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [API](./api/all)                  | OpenTelemetry API, including metrics, traces, baggage, context                       | `opentelemetry-api`           | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-api.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api)                     |
| [API  Incubator](./api/incubator) | API incubator, including pass through propagator, and extended tracer, and Event API | `opentelemetry-api-incubator` | <!--VERSION_UNSTABLE-->1.53.0-alpha<!--/VERSION_UNSTABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-api-incubator.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api-incubator) |
| [Context API](./context)          | OpenTelemetry context API                                                            | `opentelemetry-context`       | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-context.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-context)             |
| [Common](./common)                | Common utility methods used across API components                                    | `opentelemetry-common`        | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-common.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-common)               |
</details>

<details>
  <summary>API Extensions</summary>

Extensions to the OpenTelemetry API.

| Component                                                     | Description                                                                                                                                                                             | Artifact ID                                 | Version                                                     | Javadoc                                                                                                                                                                                           |
|---------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------|-------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Kotlin Extension](./extensions/kotlin)                       | Context extension for coroutines                                                                                                                                                        | `opentelemetry-extension-kotlin`            | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-extension-kotlin.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-extension-kotlin)                       |
| [Trace Propagators Extension](./extensions/trace-propagators) | Trace propagators, including B3, Jaeger, OT Trace                                                                                                                                       | `opentelemetry-extension-trace-propagators` | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-extension-trace-propagators.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-extension-trace-propagators) |
</details>

<details open>
  <summary>SDK</summary>

The OpenTelemetry SDK for managing telemetry producing by the API.

| Component                    | Description                                            | Artifact ID                 | Version                                           | Javadoc                                                                                                                                                           |
|------------------------------|--------------------------------------------------------|-----------------------------|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [SDK](./sdk/all)             | OpenTelemetry SDK, including metrics, traces, and logs | `opentelemetry-sdk`         | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk)                 |
| [Metrics SDK](./sdk/metrics) | OpenTelemetry metrics SDK                              | `opentelemetry-sdk-metrics` | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-metrics.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-metrics) |
| [Trace SDK](./sdk/trace)     | OpenTelemetry trace SDK                                | `opentelemetry-sdk-trace`   | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-trace.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-trace)     |
| [Log SDK](./sdk/logs)        | OpenTelemetry log SDK                                  | `opentelemetry-sdk-logs`    | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-logs.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-logs)       |
| [SDK Common](./sdk/common)   | Shared SDK components                                  | `opentelemetry-sdk-common`  | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-common.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-common)   |
| [SDK Testing](./sdk/testing) | Components for testing OpenTelemetry instrumentation   | `opentelemetry-sdk-testing` | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-testing.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-testing) |
</details>

<details>
  <summary>SDK Exporters</summary>

SDK exporters for shipping traces, metrics, and logs out of process.

| Component                                                             | Description                                                                  | Artifact ID                                          | Version                                                     | Javadoc                                                                                                                                                                                                             |
|-----------------------------------------------------------------------|------------------------------------------------------------------------------|------------------------------------------------------|-------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [OTLP Exporters](./exporters/otlp/all)                                | OTLP gRPC & HTTP exporters, including traces, metrics, and logs              | `opentelemetry-exporter-otlp`                        | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-otlp.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-otlp)                                               |
| [OTLP Logging Exporters](./exporters/logging-otlp)                    | Logging exporters in OTLP JSON encoding, including traces, metrics, and logs | `opentelemetry-exporter-logging-otlp`                | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-logging-otlp.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-logging-otlp)                               |
| [OTLP Common](./exporters/otlp/common)                                | Shared OTLP components (internal)                                            | `opentelemetry-exporter-otlp-common`                 | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-otlp-common.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-otlp-common)                                 |
| [Logging Exporter](./exporters/logging)                               | Logging exporters, including metrics, traces, and logs                       | `opentelemetry-exporter-logging`                     | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-logging.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-logging)                                         |
| [Zipkin Exporter](./exporters/zipkin)                                 | Zipkin trace exporter                                                        | `opentelemetry-exporter-zipkin`                      | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-zipkin.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-zipkin)                                           |
| [Prometheus Exporter](./exporters/prometheus)                         | Prometheus metric exporter                                                   | `opentelemetry-exporter-prometheus`                  | <!--VERSION_UNSTABLE-->1.53.0-alpha<!--/VERSION_UNSTABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-prometheus.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-prometheus)                                   |
| [Exporter Common](./exporters/common)                                 | Shared exporter components (internal)                                        | `opentelemetry-exporter-common`                      | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-common.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-common)                                           |
| [OkHttp Sender](./exporters/sender/okhttp)                            | OkHttp implementation of HttpSender (internal)                               | `opentelemetry-exporter-sender-okhttp`               | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-sender-okhttp.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-sender-okhttp)                             |
| [JDK Sender](./exporters/sender/jdk)                                  | Java 11+ native HttpClient implementation of HttpSender (internal)           | `opentelemetry-exporter-sender-jdk`                  | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-sender-jdk.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-sender-jdk)                                   |                                                                                                                                                            |
| [gRPC ManagedChannel Sender](./exporters/sender/grpc-managed-channel) | gRPC ManagedChannel implementation of GrpcSender (internal)                  | `opentelemetry-exporter-sender-grpc-managed-channel` | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-exporter-sender-grpc-managed-channel.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-sender-grpc-managed-channel) |                                                                                                                                                            |
</details>

<details>
  <summary>SDK Extensions</summary>

Extensions to the OpenTelemetry SDK.

| Component                                                                     | Description                                                                        | Artifact ID                                         | Version                                                     | Javadoc                                                                                                                                                                                                           |
|-------------------------------------------------------------------------------|------------------------------------------------------------------------------------|-----------------------------------------------------|-------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [SDK Autoconfigure](./sdk-extensions/autoconfigure)                           | Autoconfigure OpenTelemetry SDK from env vars, system properties, and SPI          | `opentelemetry-sdk-extension-autoconfigure`         | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-autoconfigure.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-autoconfigure)                 |
| [SDK Autoconfigure SPI](./sdk-extensions/autoconfigure-spi)                   | Service Provider Interface (SPI) definitions for autoconfigure                     | `opentelemetry-sdk-extension-autoconfigure-spi`     | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-autoconfigure-spi.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-autoconfigure-spi)         |
| [SDK Jaeger Remote Sampler Extension](./sdk-extensions/jaeger-remote-sampler) | Sampler which obtains sampling configuration from remote Jaeger server             | `opentelemetry-sdk-extension-jaeger-remote-sampler` | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->           | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-jaeger-remote-sampler.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-jaeger-remote-sampler) |
| [SDK Incubator](./sdk-extensions/incubator)                                   | SDK incubator, including YAML based view configuration, LeakDetectingSpanProcessor | `opentelemetry-sdk-extension-incubator`             | <!--VERSION_UNSTABLE-->1.53.0-alpha<!--/VERSION_UNSTABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-incubator.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-incubator)                         |
</details>

<details>
  <summary>Shims</summary>

Shims for bridging data from one observability library to another.

| Component                              | Description                                                  | Artifact ID                      | Version                                                     | Javadoc                                                                                                                                                                     |
|----------------------------------------|--------------------------------------------------------------|----------------------------------|-------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [OpenCensus Shim](./opencensus-shim)   | Bridge opencensus metrics into the OpenTelemetry metrics SDK | `opentelemetry-opencensus-shim`  | <!--VERSION_UNSTABLE-->1.53.0-alpha<!--/VERSION_UNSTABLE--> | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-opencensus-shim.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-opencensus-shim)   |
| [OpenTracing Shim](./opentracing-shim) | Bridge opentracing spans into the OpenTelemetry trace API    | `opentelemetry-opentracing-shim` | <!--VERSION_STABLE-->1.53.0<!--/VERSION_STABLE-->          | [![Javadocs](https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-opentracing-shim.svg)](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-opentracing-shim) |
</details>

## Dependencies

To take a dependency, [include a BOM][dependencies-and-boms] and specify the dependency as follows,
replacing `{{artifact-id}}` with the value from the "Artifact ID" column
from [artifacts](#artifacts):

<details open>
  <summary>Gradle</summary>

```groovy
  implementation('io.opentelemetry:{{artifact-id}}')
```
</details>

<details>
  <summary>Maven</summary>

```xml
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>{{artifact-id}}</artifactId>
</dependency>
```
</details>

### Snapshots

Snapshots of the `main` branch are available as follows:

<details open>
  <summary>Gradle</summary>

```groovy
repositories {
    maven { url 'https://central.sonatype.com/repository/maven-snapshots/' }
}

dependencies {
  implementation platform("io.opentelemetry:opentelemetry-bom:1.53.0-SNAPSHOT")
  implementation('io.opentelemetry:opentelemetry-api')
}
```
</details>

<details>
  <summary>Maven</summary>

```xml
  <project>
    <repositories>
      <repository>
        <id>sonatype-snapshot-repository</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
      </repository>
    </repositories>
    <dependencyManagement>
      <dependencies>
        <dependency>
          <groupId>io.opentelemetry</groupId>
          <artifactId>opentelemetry-bom</artifactId>
          <version>1.53.0-SNAPSHOT</version>
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
</details>

## Compatibility

Artifacts from this repository follow semantic versioning.

Stable artifacts (i.e. artifacts without `-alpha` version suffix) come with strong backwards
compatibility guarantees for public APIs.

Artifacts may depend on other artifacts from this repository, and may depend on internal APIs (i.e.
non-public APIs) which are subject to change across minor versions. Therefore, it's critical to keep
artifact versions in sync in order to avoid possible runtime exceptions. We strongly
recommend [using one of our BOMs][dependencies-and-boms] to assist in keeping artifacts in sync.

See the [VERSIONING.md](VERSIONING.md) for complete details on compatibility policy.

## Contacting us

We hold regular meetings. See details at [community page](https://github.com/open-telemetry/community#implementation-sigs).

To report a bug, or request a new feature,
please [open an issue](https://github.com/open-telemetry/opentelemetry-java/issues/new/choose).

We use [GitHub Discussions](https://github.com/open-telemetry/opentelemetry-java/discussions)
for support or general questions. Feel free to drop us a line.

We are also present in the [`#otel-java`](https://cloud-native.slack.com/archives/C014L2KCTE3) channel in the [CNCF slack](https://slack.cncf.io/).
Please join us for more informal discussions.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for:

* Details on building locally
* Project scope
* Keys to successful PRs
* Guide to using gradle composite builds

### Maintainers

- [Jack Berg](https://github.com/jack-berg), New Relic
- [John Watson](https://github.com/jkwatson), Verta.ai

For more information about the maintainer role, see the [community repository](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#maintainer).

### Approvers

- [Jason Plumb](https://github.com/breedx-splk), Splunk
- [Josh Suereth](https://github.com/jsuereth), Google
- [Lauri Tulmin](https://github.com/laurit), Splunk
- [Trask Stalnaker](https://github.com/trask), Microsoft

For more information about the approver role, see the [community repository](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#approver).

### Triagers

- [Gregor Zeitlinger](https://github.com/zeitlinger), Grafana Labs

For more information about the triager role, see the [community repository](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#triager).

### Emeritus

- Maintainer [Bogdan Drutu](https://github.com/BogdanDrutu)
- Maintainer [Carlos Alberto](https://github.com/carlosalberto)
- Approver [Mateusz Rzeszutek](https://github.com/mateuszrzeszutek)

For more information about the emeritus role, see the [community repository](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#emeritus-maintainerapprovertriager).

### Help wanted

We are currently resource constrained and are actively seeking new contributors interested in working towards [approver](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#approver) / [maintainer](https://github.com/open-telemetry/community/blob/main/guides/contributor/membership.md#maintainer) roles. In addition to the documentation for approver / maintainer roles and the [contributing](./CONTRIBUTING.md) guide, here are some additional notes on engaging:

- [Pull request](https://github.com/open-telemetry/opentelemetry-java/pulls) reviews are equally or more helpful than code contributions. Comments and approvals are valuable with or without a formal project role. They're also a great forcing function to explore a fairly complex codebase.
- Attending the [Java: SDK + Automatic Instrumentation](https://github.com/open-telemetry/community?tab=readme-ov-file#implementation-sigs) Special Interest Group (SIG) is a great way to get to know community members and learn about project priorities.
- Issues labeled [help wanted](https://github.com/open-telemetry/opentelemetry-java/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22) are project priorities. Code contributions (or pull request reviews when a PR is linked) for these issues are particularly important.
- Triaging / responding to new issues and discussions is a great way to engage with the project.

### Thanks to all of our contributors!

<a href="https://github.com/open-telemetry/opentelemetry-java/graphs/contributors">
  <img alt="Repo contributors" src="https://contrib.rocks/image?repo=open-telemetry/opentelemetry-java" />
</a>

[codecov-image]: https://codecov.io/gh/open-telemetry/opentelemetry-java/branch/main/graph/badge.svg
[codecov-url]: https://app.codecov.io/gh/open-telemetry/opentelemetry-java/branch/main/
[dependencies-and-boms]: https://opentelemetry.io/docs/languages/java/intro/#dependencies-and-boms
