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

## Plan

[Please review the roadmap here](https://medium.com/opentracing/a-roadmap-to-convergence-b074e5815289).

In the coming months [we plan to merge the OpenCensus and OpenTracing
projects](https://medium.com/opentracing/merging-opentracing-and-opencensus-f0fe9c7ca6f0).
The technical committee will drive the merge effort. We’ve identified areas that
require deeper discussion and areas that merely require alignment around
terminology and usage patterns.

We have a three-step plan for the project merge: 

1. spike of merged interfaces,
2. beta release of new project binaries, and
3. kicking off the work towards a 1.0 release.

### Spike of merged interfaces

Spike API merge will happen in a separate repository, focused on Java
specifically. The main goal of the spike is to make sure that we have a clear
path forward, and that there will be no unforeseen technical issues blocking the
merge of the projects (while staying true to our  declared goals for the merge).

As a result of the spike we plan to produce:

- Alpha version of a merged interface in new repository.
- Rough port of OpenCensus implementation as an implementation of choice for
  this API.
- Rough OpenTracing bridge to new interface.
- Supplemental documentation and design documents

We expect this spike will take us a few weeks.

### Beta release of a new project

Once we have cleared out the path - we plan to initiate a transition of active
contribution and discussions from OpenCensus and OpenTracing to the new project.
We will

- Clean up OpenCensus into official SDK of new project
- Release an official OpenTracing bridge to new Interface

We will minimize the duration of this transition so that users can switch to the
new API as seamlessly as possible, and contributors can quickly ensure that
their work is compatible with future versions. We will also encourage all
contributors to start working in the new project once the merger announced. So
there will be no time of duplicative contributions.

### Kick off the work towards 1.0

After beta release we will encourage customers and tracing vendors to start
using the new project, providing feedback as they go. So we can ensure a high
quality v1.0 for the merged project:

- We will allow ourselves to break *implementations*, but not people using the
  public Interfaces.
- Additions (into interfaces for instance) will involve a best-effort attempt at
  backwards compatibility (again, for implementations – callers of the public
  APIs should not be negatively affected by these additions).

### Project setup and contribute

Please refer to the [contribution guide](https://github.com/newrelic-forks/opentelemetry-java/blob/master/CONTRIBUTING.md)
on how to setup this project and contribute!


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
