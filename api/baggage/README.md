# OpenTelemetry Baggage API

[![Javadocs][javadoc-image]][javadoc-url]

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-api-baggage.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api-baggage

---
#### Running micro-benchmarks
From the root of the repo run `./gradlew clean :opentelemetry-api-baggage:jmh` to run all the benchmarks 
or run `./gradlew clean :opentelemetry-api-baggage:jmh -PjmhIncludeSingleClass=<ClassNameHere>` 
to run a specific benchmark class.