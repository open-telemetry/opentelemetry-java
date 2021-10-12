OpenTelemetry SDK
======================================================

[![Javadocs][javadoc-image]][javadoc-url]

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk

---
#### Running micro-benchmarks
From the root of the repo run `./gradlew clean :opentelemetry-sdk:jmh` to run all the benchmarks
or run `./gradlew clean :opentelemetry-sdk:jmh -PjmhIncludeSingleClass=<ClassNameHere>`
to run a specific benchmark class.
