# Jaeger Remote Sampler

This module implements [Jaeger remote sampler](https://www.jaegertracing.io/docs/latest/sampling/#remote-sampling).
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
