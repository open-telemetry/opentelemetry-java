OpenTelemetry Extension Trace Propagators
======================================================

[![Javadocs][javadoc-image]][javadoc-url]

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-extension-trace-propagators.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-extension-trace-propagators

This repository provides several 
[trace propagators](https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/context/api-propagators.md),
used to propagate context across a distributed trace. 

OpenTelemetry Java provides first-party support for 
[B3 (OpenZipkin)](https://github.com/openzipkin/b3-propagation) and
[Jaeger](https://github.com/jaegertracing/jaeger) propagators.  Issues with those propagators
should be filed against this repo.

---
#### Running micro-benchmarks
From the root of the repo run `./gradlew clean :opentelemetry-extension-trace-propagators:jmh` 
to run all the benchmarks 
or run `./gradlew clean :opentelemetry-extension-trace-propagators:jmh -PjmhIncludeSingleClass=<ClassNameHere>` 
to run a specific benchmark class.
