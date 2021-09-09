# OpenTelemetry API

[![Javadocs][javadoc-image]][javadoc-url]

* The code in this module is the implementation of stable OpenTelemetry signals.
* Semantic Conventions for OpenTelemetry are in the `opentelemetry-semconv` module.
* The default implementation of the interfaces in this module is in the OpenTelemetry SDK module.
* The interfaces in this directory can be implemented to create alternative
  implementations of the OpenTelemetry library.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-api.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api

---
#### Running micro-benchmarks
From the root of the repo run `./gradlew clean :api:jmh` to run all the benchmarks
or run `./gradlew clean :api:jmh -PjmhIncludeSingleClass=<ClassNameHere>`
to run a specific benchmark class.
