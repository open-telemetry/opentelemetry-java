# Jaeger Remote Sampler

[![Javadocs][javadoc-image]][javadoc-url]

This module implements [Jaeger remote sampler](https://www.jaegertracing.io/docs/latest/sampling/#collector-sampling-configuration).
The sampler configuration is received from collector's gRPC endpoint.

### Example

The following example shows initialization and installation of the sampler:

```java
Builder remoteSamplerBuilder = JaegerRemoteSampler.builder()
    .setChannel(grpcChannel)
    .setServiceName("my-service");
TraceConfig traceConfig = provider.getActiveTraceConfig()
    .toBuilder().setSampler(remoteSamplerBuilder.build())
    .build();
provider.updateActiveTraceConfig(traceConfig);
```

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-jaeger-remote-sampler.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-jaeger-remote-sampler
