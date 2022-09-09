# OpenTelemetry AWS Utils

[![Javadocs][javadoc-image]][javadoc-url]

> **NOTICE**: This artifact is deprecated and its contents have been moved
> to [io.opentelemetry.contrib:opentelemetry-aws-resources](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-resources).
> Version 1.19.0 will be the last minor version published. However, it will continue to receive
> patches for security vulnerabilities, and`io.opentelemetry:opentelemetry-bom` will reference the
> last published version.

---
#### Running micro-benchmarks
From the root of the repo run `./gradlew clean :opentelemetry-sdk-extension-aws:jmh` to run all the benchmarks
or run `./gradlew clean :opentelemetry-sdk-extension-aws:jmh -PjmhIncludeSingleClass=<ClassNameHere>`
to run a specific benchmark class.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-aws.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-aws
