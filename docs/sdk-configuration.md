# Design for configuring the SDK

This document outlines some of the goals we have for user configuration of the SDK. It is a
continuation of discussion started with https://github.com/open-telemetry/opentelemetry-java/issues/2022.

## Target audiences

There are a few different target audiences that are related to our configuration story.

- Application developers, aka end-users. Often have no knowledge of tracing but want to add 
OpenTelemetry to their app and see traces show up in a console. Application developers will increase
in number forever, while the below are more constant.

- Dev-ops / framework developers. Write components or frameworks to support providing tracing to
their application developers. May write custom SDK extensions such as exporters, samplers, to fit
with their internal infrastructure and as such have some familiarity with tracing at least as the
SDK presents it.

- Telemetry extension authors. Write custom SDK extensions, often to support a particular backend.
Verify familiar with telemetry.

- OpenTelemetry maintainers. Write the SDK code.

When making decisions, especially about complexity, we always prioritize the application developers,
then framework developers, then maintainers. This is because we expect those with less domain
knowledge about tracing to require a simpler experience than those with more. There is also more
bang-for-the-buck by making the end-user experience as streamlined as possible since we expect there
to be much more of them than other audiences.

## Goals and non-goals

### Goals

- Provide a single entrypoint to configuring the SDK. For end-users, less familiar with the SDK, we
want to have everything together to provide discoverability and simpler end-user code. If there are
several, clear use cases which benefit from different entrypoints, we could have multiple
corresponding to each one.

- Fit well with common Java idioms such as dependency injection, or common frameworks like Spring.

- Reduce the chance of gotchas or configuration mishaps.

- Aim for optimal performance of the configured SDK for the most common use case.

### Non-goals

- Provide the best possible experience for custom SDKs. Generally the burden of the experience for
custom SDKs can fall on their authors, we optimize for our standard usage, the full SDK. Any
reference to "the SDK" in this document refers to the full SDK with all signals.

- Make sure everything is auto-configurable. This is out of the scope of the SDK, and instead is
left to auto-configuration layers, which are also described below but not as part of the core SDK. 
In partiular, SignalProvider SPIs which currently exist are proposed to be removed from the SDK. The 
SDK may provide an autoconfiguration extension as an option which is not internal to the main SDK 
components.

## Configuring an instance of the SDK

The SDK exposes configuration options for all the signals it supports. Users all have different
requirements for how they use the SDK; for example they may use different exporters depending on
their backend. Because we cannot guess the configuration the user needs, we expect that the SDK must
be configured by the user before it can be used.

Goals for configuring the SDK are

- Discoverability of options
- Ease of use by end users, e.g., less complicated code required
- Avoid requiring duplicate configuration, which can lead to errors or confusion
- Provide good defaults where possible

In Java, the builder pattern is common for configuring instances. Let's look at what that may look
like. The simplest configuration will be when a user wants to get a default experience, exporting
with a specific exporter to an endpoint.

The SDK builder will simply allow accept its components as builder parameters. It only allows
setting SDK implementations and is not meant for use with partial SDKs.

```java
class OpenTelemetrySdkBuilder {
  public OpenTelemetrySdkBuilder setTracerProvider(TracerSdkProvider tracerProvider);
  public OpenTelemetrySdkBuilder setMeterProvider(MeterSdkProvider meterProvider);
  public OpenTelemetrySdk buildAndSetAsGlobal();
  public OpenTelemetrySdk buildWithoutSettingAsGlobal();
}
```

A very simple configuration may look like this.

```java
class HelloWorld {
    public static void main(String[] args) {
        TracerSdkProvider tracerProvider = TracerSdkProvider.builder()
          .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder())
              .setEndpoint("spans-service:4317")
              .build())
          .build();
        
        MeterSdkProvider meterProvider = MeterSdkProvider.builder().build();
        IntervalMetricReader.builder()
          .setMetricProducers(meterProvider.getMetricProducer())
          .setMetricExporter(PrometheusMetricServer.create())
          .build();
        
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
          .addPropagator(W3CHttpTraceContextPropagator.getInstance())
          .setTracerProvider(tracerProvider)
          .setMeterProvider(meterProvider)
          .build();
   }
}
```

This

- Exports spans using OTLP to `spans-service`
  - Uses the BatchSpanProcessor. Future work is to make configuring this more intuitive
- Exposes metrics using Prometheus format
  - Future work is to make configuring this more intuitive
- Uses ParentBased(AlwaysOn) sampler
- Uses standard random IDs
- Uses a default Resource which simply consists of the SDK (or any other resources we decide to)
include in the core SDK, not extensions
- Uses the default Clock, which uses Java 8 / 9+ optimized APIs for getting time
  - The only real reason a user would set this is for unit tests, not for production
- Enforces default numeric limits related to number of attribute, etc
- Enables single w3c propagator

Because the exporting is often the only aspect an end user needs to configure, this is the simplest
possible API for configuring the SDK.

Let's look at a more complicated example

```java
class HelloWorld {
    public static void main(String[] args) {
        Resource resource = Resource.getDefault().merge(CoolResource.getDefault());
        Clock clock = AtomicClock.create();
        TracerSdkProvider tracerProvider = TracerSdkProvider.builder()
            .setResource(resource)
            .setClock(clock)
            .addSpanProcessor(CustomAttributeAddingProcessor.create())
            .addSpanProcessor(CustomEventAddingProcessor.create())
            .addSpanProcessor(BatchSpanProcessor.builder(
                OtlpGrpcSpanExporter.builder()
                    .setEndpoint("spans-service:4317")
                    .setTimeout(Duration.ofSeconds(10))
                    .setBatchQueueSize(10000)
                    .build())
                .build())
            .addSpanProcessor(SimpleSpanProcessor.builder(
                ZipkinExporter.builder()
                    .setEndpoint("spans-service:4317")
                    .build())
                .build())
            .setTraceSampler(Sampler.rateLimiting())
            .setTraceLimits(TraceLimits.builder().setMaxAttributes(10).build())
            .setIdGenerator(TimestampedIdGenerator.create())
            .build();
        
        MeterSdkProvider meterProvider = MeterSdkProvider.builder()
            .setResource(resource)
            .setClock(clock)
            .setMeterStuff(InProgressMetricsStuff.create())
            .build();
        IntervalMetricReader.builder()
            .setMetricProducers(meterProvider.getMetricProducer())
            .setMetricExporter(PrometheusMetricServer.create())
            .build();
        
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
            .addPropagator(W3CHttpTraceContextPropagator.getInstance())
            .addPropagator(B3Propagator.getInstance())
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .build();
 
        OpenTelemetrySdk openTelemetryForJaegerBackend = OpenTelemetrySdk.builder()
           .addPropagator(JaegerPropagator.getInstance())
           .setTracerProvider(tracerProvider)
           .setMeterProvider(meterProvider)
           .build();
   }
}
```

This configures resource, clock, exporters, span processors, propagators, sampler, trace limits, and metrics.
It configures two SDKs with different propagators. We unfortunately cannot achieve our goal of only
setting a property once - `Resource` and `Clock` are shared among signals but configured repeatedly.
An alternative is to flatten all settings onto `OpenTelemetrySdkBuilder` - this has a downside that
it makes it very natural to create new tracer / meter providers for each configuration of the SDK,
which could result in a lot of duplication of resources like threads, workers, TCP connections. So
instead, this can make it clearer that those signals themselves are full-featured, and 
`OpenTelemetry` is just a bag of signals.

Keep in mind, reconfiguring `Clock` is expected to be an extremely uncommon operation.

Another thing to keep in mind is that it will be less common for an application developer to go this far
and we can expect it is actually framework developers that use the full configuration capabilities
of the SDK, likely by tying it to a separate configuration system.

### Why build instances

We have found that even at such an early stage of user adoption, users want to build instances of
the SDK.

- Integrates with dependency injection, e.g., Spring, in a similar way as many other libraries
- Can allow having multiple instances in the same app, in multi-concern single-classloader scenarios
- Allow managing lifecycle of SDK, e.g., shutting down and starting along with the lifecycle of a
serverless runtime

### Configuring the SDK in a framework

It is extremely common for Java apps to be written using a dependency injection framework like
Spring, Guice, Dagger, HK, and many more. They will all follow a very similar pattern though.

```java
@Component
public class OpenTelemetryModule {
    @Bean
    public Resource resource() {
        return Resource.getDefault().merge(CoolResource.getDefault());
    }
    
    @Bean
    public Clock otelClock() {
        return AtomicClock.create();
    }

    @Bean
    public TracerSdkProvider tracerProvider(Resource resource, Clock clock, MonitoringConfig config) {
        return TracerSdkProvider.builder()
            .setResource(resource)
            .setClock(clock)
            .addSpanProcessor(CustomAttributeAddingProcessor.create())
            .addSpanProcessor(CustomEventAddingProcessor.create())
            .addSpanProcessor(BatchSpanProcessor.builder(
                OtlpGrpcSpanExporter.builder()
                    .setEndpoint(config.getOtlpExporter().getEndpoint())
                    .setTimeout(config.getOtlpExporter().getTimeout())
                    .build())
                .setBatchQueueSize(config.getOtlpExporter().getQueueSize())
                .setTimeout(config.getOtlpExporter().getTimeout())
                .build())
            .addSpanProcessor(SimpleSpanProcessor.builder(
                ZipkinExporter.builder()
                    .setEndpoint(config.getZipkinExporter().getEndpoint())
                    .build())
                .build())
            .setTraceSampler(config.getSamplingRate() != 0 
               ? Sampler.rateLimiting(config.getSamplingRate()) : Sampler.getDefault())
            .setTraceLimits(TraceLimits.builder().setMaxAttributes(config.getMaxSpanAttributes()).build())
            .setIdGenerator(TimestampedIdGenerator.create())
            .build();
    }
  
    @Bean
    public MeterSdkProvider meterProvider(Resource resource, Clock clock) {
        return MeterSdkProvider.builder()
            .setResource(resource)
            .setClock(clock)
            .setMeterStuff(InProgressMetricsStuff.create())
            .build();
    }

    @Bean
    public IntervalMetricReader metricReader(MeterSdkProvider meterProvider) {
        return IntervalMetricReader.builder()
            .addMetricProducer(meterProvider.getMetricProducer())
            .addMetricExporter(PrometheusMetricServer.create())
            .build();
    }

    @Bean
    public OpenTelemetry openTelemetry(TracerSdkProvider tracerProvider, MeterSdkProvider meterProvider) {
        return OpenTelemetrySdk.builder()
           .addPropagator(W3CHttpTraceContextPropagator.getInstance())
           .addPropagator(B3Propagator.getInstance())
           .setTracerProvider(tracerProvider)
           .setMeterProvider(meterProvider)
           .build();
    }

    @Bean
    @ForJaeger
    public OpenTelemetry openTelemetryJaeger(TracerSdkProvider tracerProvider, MeterSdkProvider meterProvider) {
        return OpenTelemetrySdk.builder()
           .addPropagator(JaegerPropagator.getInstance())
           .setTracerProvider(tracerProvider)
           .setMeterProvider(meterProvider)
           .build();
    }
  
    @Bean
    public AuthServiceStub authService(@ForJaeger OpenTelemetry openTelemetry, AuthConfig config) {
        return AuthServiceGrpc.newBlockingStub(ManagedChannelBuilder.forEndpoint(config.getEndpoint()))
          .withInterceptor(TracingClientInterceptor.create(openTelemetry));
    }
    
    @Bean
    public ServletFilter servletFilter(OpenTelemetry openTelemetry) {
        return TracingServletFilter.create(openTelemetry);
    }
}

// Use some instrumented client
@Component
public class MyAuthInterceptor {

  private final AuthServiceStub authService;
  
  @Inject
  public MyAuthInterceptor(AuthServiceStub authService) {
    this.authService = authService;
  }
  
  public void doAuth() {
    if (!authService.getToken("credential").isAuthenticated()) {
      throw new HackerException();
    }
  }
}

// Use tracer directly, not so common
@Component
public class MyService {

  private final Tracer tracer;
  private final Meter meter;
  
  @Inject
  public MyService(TracerProvider tracerProvider, MeterProvider meterProvider) {
    tracer = tracerProvider.get("my-service");
    meter = meterProvider.get("my-service");
  }
  
  public void doLogic() {
    Span span = tracer.spanBuilder("logic").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      Thread.sleep(1000);
    } finally {
      span.end();
    }
  }
}
```

## The global instance of the SDK

A built instance is convenient to use in most Java apps because of dependency injection. Because it
has a easy-to-reason initialization ordering, being tied into the dependency ordering (even if dependency 
injection happened to be done manually through constructor invocation), we encourage application
developers to only use it.

However, there are corner cases where an instance cannot be injected. The famous example is MySQL -
MySQL interceptors are initialized by calling a default constructor, and there is no way to pass a
built instance of a signal provider. For this case, we must store an SDK instance into a global
variable. It is expected that frameworks or end-users will set the SDK as the global to support
instrumentation that requires this.

Before an SDK has been set, access to the global `OpenTelemetry` will return a no-op 
`DefaultOpenTelemetry`. This is because it is possible library instrumentation is using the global,
and it may even use it during the processing of a request (rather than only at initialization time).
For this reason, we cannot throw an exception. Instead, if the SDK is detected on the classpath, we
will log a `SEVERE` warning once-only indicating the API has been accessed before the SDK configured
with directions on how a user could solve the problem. SDKs must be configured early
in an application to ensure it applies to all of the logic in the app, and this will generally be
ensured by the configuration framework such as Spring. For application developers, this restriction
should not have any effect one way or the other in the vast majority of cases.

MySQL is the only known corner case that requires the global SDK instance. If such a corner case
didn't exist, we may not even support it in the first place.

See special note about Java Agent below though.

## Telemetry within SDK components

SDK components, such as exporters or remote samplers, may want to emit telemetry for their own
processing. However, the SDK components must be initialized before the SDK can be fully built. We do
not support partially built SDK because one cannot reason about the behavior of it. Similarly we
do not support using the global instance of the SDK before it has been built. Therefore, SDK
components that require `OpenTelemetry` must accept it lazily. This is a restriction, but given
such components are rarely developed by application developers, and generally developed by either
framework authors or OpenTelemetry maintainers, this restriction is deemed reasonable.

If this mechanism was built into the SDK, it may look like

```java
interface OpenTelemetryComponent {
  default void setOpenTelemetry(OpenTelemetry openTelemetry) {}
}
interface SpanExporter extends OpenTelemetryComponent {
}
public class BatchExporter implements SpanExporter {
  
  private volatile Tracer tracer;
  
  @Override
  public void setOpenTelemetry(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracerProvider().get("spanexporter");
  }
  
  @Override
  public void export() {
        Tracer tracer = this.tracer;
        if (tracer != null) {
          tracer.spanBuilder("export").startSpan();
        }
  }
}
public class OpenTelemetrySdkBuilder {
    public OpenTelemetrySdkBuilder addSpanExporter(SpanExporter exporter) {
        tracerProvider.addSpanExporter(exporter);
        components.add(exporter);
    }
    
    public OpenTelemetrySdkBuilder setSampler(Sampler sampler) {
        tracerProvider.setSampler(sampler);
        components.add(sampler);
    }
    
    public OpenTelemetry build() {
        OpenTelemetrySdk sdk = new OpenTelemetrySdk(tracerProvider.build(), meterProvider.build());
        for (OpenTelemetryComponent component : components) {
          component.setOpenTelemetry(sdk);
        }
    }
}
```

A framework author will have an even easier time since most dependency injection frameworks
natively support lazy injection.

```java
@Component
public class MonitoringModule {
  
    @Bean
    @ForSpanExporter
    public Tracer tracer(TracerProvider tracerProvider) {
    return tracerProvider.get("spanexporter");
    }
}

@Component
public class MyExporter implements SpanExporter {

    private Lazy<Tracer> tracer;
    
    @Inject
    public MyExporter(@ForSpanExporter Lazy<Tracer> tracer) {
    this.tracer = tracer;
    }
    
    @Override
    public void export() {
    tracer.get().spanBuilder("export").startSpan();
    }
}
```

## Immutability of OpenTelemetry configuration

The above attempts to avoid allowing a built SDK to be mutated. Allowing mutation can make code
harder to reason about (any component, even deep in business logic, could update the SDK without
hindrance), can reduce performance (require volatile read on most operations), and produce thread
safety issues if not well implemented. In particular, compared to the current state as of writing,

- `addSpanProcessor` is not needed because we instead push the complexity of handling telemetry
within telemetry components to those components, where the maintainers will have more domain
knowledge. It allows this mutator method to be removed from the end-user API.

- `updateTraceConfig` - instead of allowing updates at the top level, we should consider making
`TraceConfig` an interface, and the SDK default implementation is static. It allows the above
benefits of immutability to be in place for the common case where dynamic updates are not needed.
Where dynamic updates are needed, it can be replaced with a mutable implementation instead of making
the SDK configuration mutable. This keeps update methods out of the end-user APIs and will generally
give framework developers more control by handling dynamicism themselves without the chance of
end-users to affect it negatively.

Some highly buggy code that could be enabled by mutability.

```java
class SleuthUsingService {
  
  @Inject
  private OpenTelemetry openTelemetry;

  public void doLogic() {
    // My logic is important, so always sample it!
    OpenTelemetrySdk.getTracerManagement().updateTraceConfig(config -> config.setSampler(ALWAYS_ON));
    // This service was able to affect other services, even though Sleuth intends to
    // "manage the SDK". Unlike the javaagent, it can't block access to SDK methods we may provide.
    doSampledLogicWhileOtherServicesAlsoGetSampled();
  }
}
```

## TracerSdkManagement

With the proposal to make a configured SDK fully built rather than allowing configuration
mutability, the SDK management interface would only have `shutdown` and `forceFlush`. It seems
reasonable to actually remove `shutdown` from the `TracerSdkProvider` - the SDK provider is mostly
a bag of parts and has no intrinsic component, if instead components like `BatchSpanProcessor` and
`SpanExporter` implement `Closeable`, we can model shutdown as the responsibility of the creator of
the resources, which is idiomatic. In DI frameworks, this would generally be automatic. This would
prevent the case where two `TracerProvider` share an exporter and one of them is shutdown - when
shutting down the exporter, shut down the exporter.

```java
public class MyApp {
 
    public static void main(String[] args) {
        try (OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder().build();
              BatchSpanProcessor otlpProcessor = BatchSpanProcessor.builder(otlpExporter).build()) {
            OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(TracerSdkProvider.builder()
                                        .addSpanProcessor(otlpProcessor)
                                        .build())
                .build();
 
            Server server = Server.builder().setOpenTelemetry(sdk).build();
            server.listenAndServe();
        }
    }

}
```

```java
@Component
public class MyModule {

    @Bean
    // If SpanExporter extends Closeable, it's automatically closed
    OtlpGrpcSpanExporter otlpExporter() {
        return OtlpGrpcSpanExporter.builder().build()
    }
   
    @Bean
    // If BatchSpanProcessor extends Closeable, it's automatically closed
    BatchSpanProcessor otlpSpanProcessor(OtlpGrpcSpanExporter otlpExporter) {
        return BatchSpanProcessor.builder(otlpExporter).build();
    }
 
    @Bean
    TracerSdkProvider tracerProvider(BatchSpanProcessor otlpSpanProcessor) {
        return TracerSdkProvider.builder().addSpanProcessor(otlpSpanProcessor).build();
    }

    @Bean
    OpenTelemetrySdk openTelemetry(TracerSdkProvider tracerProvider) {
        return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    }
}
```

The `OpenTelemetrySdk` and `TracerSdkProvider` are still exposed to provide access to `forceFlush`.
This makes it seem like `forceFlush`, though implemented in the SDK, may be more appropriate for the
API as it's an end-user API - it could make the separation of concerns of the initialization of the
SDK and the end-user tracing API complete. But the spec currently does not allow it.

## Library instrumentation

As the configuration of observability is contained on `OpenTelemetry` instances, it is expected that
library instrumentation accept an `OpenTelemetry` instance, often as a builder for their e.g.,
tracing interceptor, when configuring observability. An alternative method that leaves out the
parameter and falls back to the global can be added as well. Library authors would still have the
choice of starting with using the global and adding configurability by accepting `OpenTelemetry` if
users request it - we would expect official `OpenTelemetry` maintained library instrumentation to
follow our pattern though.

## Auto-configuration

The above presents programmatic configuration for the SDK and proposes that the core SDK has no
other mechanism for configuration. There is no SPI nor processing of environment variables or
system properties. There are many mechanisms for configuration, for example Spring Boot. 
Integration with these systems becomes easier to reason about if we consider auto-configuration at
a layer above the core SDK.

### Java Auto-Instrumentation Agent

Java Auto-Instrumentation Agent is the primary means of automatically configuring the SDK. It
contains system properties, environment variables, and SPIs for allowing a user to have a fully
setup tracing configuration just by applying the agent. In fact, the agent does not even allow a
user to use the SDK directly, actively blocking it. Instead of having a situation where some
configuration is automatic in the core SDK and some in the agent, we can move it all to the agent.
The agent already exposes exporter SPIs - it can also expose SPIs for customization of the SDK
components that are manually configured above.

- We could consider having a very similar autoconfiguration wrapper artifact as an SDK extension too.
But we would assume the core SDK is always manually configured.

To allow users of the agent to apply tracing to their own code, the agent should attempt to
instrument dependency injection to provide an instance of `OpenTelemetry` using the agent-configured
SDK, for example it should add it to the Spring `ApplicationContext`. For cases where dependency
injection is not available, though, there is no option but to provide access to the SDK through a
global variable. We can expect such usage to still function correctly even if the agent is removed
and a different configuration mechanism is used, such as manual configuration as above, or Spring
Sleuth.

### SDK Auto-Configuration Wrapper

For non-agent users, we can still provide a non-programmatic solution for configuring the SDK -
it can be a different artifact which contains SPIs similar to what we have currently, supports
environment variables and other auto-configuration. A single entrypoint method, `initialize()` could
determine the configuration, initialize `OpenTelemetry`, and set it as the global. As this artifact
is in our control, it would be reasonable for `opentelemetry-api` to check the classpath for the
presence of the wrapper and invoke it automatically.

### Spring Sleuth

[Spring Sleuth](https://spring.io/projects/spring-cloud-sleuth) (or any similar observability-aware server framework such as 
[curio-server-framework](https://github.com/curioswitch/curiostack/blob/master/common/server/framework/src/main/java/org/curioswitch/common/server/framework/monitoring/MonitoringModule.java)
or internal frameworks developed by devops teams at companies) is also a mechanism for automatically
configuring the SDK. In general, we would expect Sleuth users to not be using the java agent.

Examples of how Sleuth could work are presented above in examples using `@Bean`. In particular, we
expect it to have its own set of configuration properties - by making sure we don't implement
configuration properties in the core SDK, only configuration layers like the agent or a possible
configuration wrapper, we avoid the possibility of confusion by having duplicate variables (in
practice, OpenTelemetry naming would likely be ignored and overwritten by Spring naming).

## Partial SDKs

We allow implementing particular signals of the OpenTelemetry API without using our SDK. For example,
a MeterProvider may be implemented with micrometer. For this reason, each signal must also present
all of its options in the form of, e.g., `TracerSdkProviderBuilder`. We expect the vast majority of
users to use `OpenTelemetrySdkBuilder` - while there is some duplication with the signal provider
builder, it is work maintainers can do to present the simplest interface for the most common use
case of using the whole SDK.

Without SPI, the way to initialize a partial SDK would be to use `DefaultOpenTelemetry`.

```java
@Bean
public OpenTelemetry openTelemetry() {
  return DefaultOpenTelemetry.builder()
    .setTraceProvider(TracerSdkProvider.builder().build())
    .setMeterProvider(MicrometerProvider.builder().build())
    .build();
}
``` 

As this should be a fairly minor use case, and commonly handled by framework developers, this seems
reasonable. We can also hope that where it is important, it is the author of partial SDKs that
provide a one-stop-shop entrypoint.

```java
@Bean
public OpenTelemetry openTelemetry() {
  return OpenTelemetrySdkWithMicrometer.builder()
    .addSpanExporter()
    .setMeterRegistry()
    .build();
}
```

## Alternatives considered

### Always allow using the global OpenTelemetry

We discuss some [advantages](#Immutability of OpenTelemetry configuration) of `OpenTelemetry` not
being mutable. One of the main side effects of this decision is not allowing the global to be used
before it is configured. An alternative approach may start with a core that is mutated when
configured, and global usage would still be valid even if references are made before configuration.
The lifecycle becomes difficult to reason about i.e., when is `OpenTelemetry` actually ready for use?
Dependency injection makes it explicit, global doesn't. It also seems to have performance
implications for less common end-user use cases (dynamic config) or for reasons that non-end-users
can handle (telemetry within telemetry).

### SPI loading of OpenTelemetry components

We could detect OpenTelemetry components using SPI, but we don't expect partial SDKs to be so common.
Instead of an inside-out approach of initializing a partial SDK within our code, we can instead just
encourage an outside-in approach where a partial-SDK-specific wrapper is created. This reduces the
magic in configuring `OpenTelemetry`, it all happens through our single entry-point.
