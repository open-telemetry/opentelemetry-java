# OpenTelemetry Trace API

[![Javadocs][javadoc-image]][javadoc-url]

* The interfaces in this directory can be implemented to create alternative
  implementations of the OpenTelemetry library.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-api-trace.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api-trace

---
#### Running micro-benchmarks
From the root of the repo run `./gradlew clean :opentelemetry-api-trace:jmh` to run all the benchmarks 
or run `./gradlew clean :opentelemetry-api-trace:jmh -PjmhIncludeSingleClass=<ClassNameHere>` 
to run a specific benchmark class.