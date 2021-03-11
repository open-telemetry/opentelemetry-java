# Java OpenTelemetry Examples

This module contains a set of fully-functional, working examples of using the OpenTelemetry Java 
APIs and SDK that should all be able to be run locally. Some of them assume you have docker 
running on your local machine.

## Example modules:

- [Using the SDK AutoConfiguration module](autoconfigure)
  - This module contains a fully-functional example of using the autoconfigure SDK extension module to
    configure the SDK using only environment variables (or system properties).
  - Note: the `opentelemetry-sdk-extension-autoconfigure` module is still experimental at this time.
- [Setting up OTLP exporters](otlp)
  - OTLP is the OpenTelemetry Protocol. This module will demonstrate how to configure the OTLP exporters,
    and send data to the OpenTelemetry collector using them.
  - Note: this example requires having docker installed to run the example.
- [Configuring the Jaeger Exporter](jaeger)
  - This module contains a fully-functional example of configuring the OpenTelemetry SDK to use a 
  Jaeger exporter, and send some spans to it using the OpenTelemetry API.
  - Note: this example requires having docker installed to run the example.
- [Setting up the Zipkin exporter](zipkin)
  - This module contains a fully-functional example of configuring the OpenTelemetry SDK to use a
    Jaeger exporter, and send some spans to a zipkin backend using the OpenTelemetry API.
  - Note: this example requires having docker installed to run the example.
- [Configuring the Logging Exporters](logging)
  - This module contains a fully-functional example of configuring the OpenTelemetry SDK to use a 
  logging exporter.
- [Manually Configuring the SDK](sdk-usage)
  - This module shows some concrete examples of manually configuring the Java OpenTelemetry SDK for Tracing.
- [Using the OpenTelemetry metrics API](metrics)
  - This module contains examples of using the (still experimental) OpenTelemetry metrics APIs.
- [Setting up the Prometheus exporter](prometheus)
  - The module shows how to configure the OpenTelemetry SDK to expose an endpoint that can be scraped
    by Prometheus.
  - Note: this example uses experimental metrics APIs and SDK.
- [Manual instrumentation of GRPC](grpc)
  - This module provides an example of writing manual instrumentation for GRPC, both client and
    server.
  - Note that if you want to use more production-ready instrumentation for GRPC, this is provided
    as a part of the [OpenTelemetry Java Instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation) project.
