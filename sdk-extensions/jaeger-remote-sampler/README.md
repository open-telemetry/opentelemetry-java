# Jaeger Remote Sampler

[![Javadocs][javadoc-image]][javadoc-url]

This module implements [Jaeger remote sampler](https://www.jaegertracing.io/docs/latest/sampling/#collector-sampling-configuration).
The sampler configuration is received from collector's gRPC endpoint.

### Example

The following example shows initialization and installation of the sampler:

```java
JaegerRemoteSampler sampler = JaegerRemoteSampler.builder()
    .setServiceName("my-service")
    .build();
return SdkTracerProvider.builder()
    ...
    .setSampler(sampler)
    .build();
```

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-jaeger-remote-sampler.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-jaeger-remote-sampler
