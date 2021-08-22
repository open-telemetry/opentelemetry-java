# OpenTelemetry AWS Utils

[![Javadocs][javadoc-image]][javadoc-url]

---
#### Running micro-benchmarks
From the root of the repo run `./gradlew clean :opentelemetry-sdk-extension-aws:jmh` to run all the benchmarks 
or run `./gradlew clean :opentelemetry-sdk-extension-aws:jmh -PjmhIncludeSingleClass=<ClassNameHere>` 
to run a specific benchmark class.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-aws.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-aws
