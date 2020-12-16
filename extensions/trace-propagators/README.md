OpenTelemetry Contrib Trace Propagators
======================================================

[![Javadocs][javadoc-image]][javadoc-url]

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-contrib-trace-propagators.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-contrib-trace-propagators

This repository provides several 
[trace propagators](https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/context/api-propagators.md),
used to propagate context across a distributed trace. 

OpenTelemetry Java provides first-party support for 
[B3 (OpenZipkin)](https://github.com/openzipkin/b3-propagation) and
[Jaeger](https://github.com/jaegertracing/jaeger) propagators.  Issues with those propagators
should be filed against this repo.

This project also contains 3rd-party propagators for other formats. These propagators are here for 
convenience and are not officially supported by the OpenTelemetry Java maintainers.

Issues/support for these propagators is only provided as a minimal "best effort", and critical
bugs should be filed with the respective vendors themselves.

* AWS X-Ray (file an issue here and mention @anuraaga)
* LightStep OpenTracing (file an issue here and mention @carlosalberto)

Extension providers that do not receive adequate support/maintenance by their respective vendors 
will become candidates for future removal.

---
#### Running micro-benchmarks
From the root of the repo run `./gradlew clean :opentelemetry-extension-trace-propagators:jmh` 
to run all the benchmarks 
or run `./gradlew clean :opentelemetry-extension-trace-propagators:jmh -PjmhIncludeSingleClass=<ClassNameHere>` 
to run a specific benchmark class.
