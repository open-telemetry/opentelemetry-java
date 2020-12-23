/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.jaeger.thrift.JaegerThriftSpanExporter;
import io.opentelemetry.exporter.jaeger.thrift.JaegerThriftSpanExporterBuilder;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder;
import io.opentelemetry.extension.trace.propagation.AwsXRayPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.extension.trace.propagation.OtTracerPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.SdkMeterProviderConfigurer;
import io.opentelemetry.sdk.autoconfigure.spi.SdkTracerProviderConfigurer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.config.TraceConfigBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

public final class OpenTelemetrySdkAutoConfiguration {

  public static OpenTelemetrySdk initialize() {
    ConfigProperties config = ConfigProperties.get();

    AttributesBuilder resourceAttributes = Attributes.builder();
    config.getCommaSeparatedMap("otel.resource.attributes").forEach(resourceAttributes::put);
    Resource resource = Resource.getDefault().merge(Resource.create(resourceAttributes.build()));

    SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder().setResource(resource);
    for (SdkMeterProviderConfigurer configurer :
        ServiceLoader.load(SdkMeterProviderConfigurer.class)) {
      configurer.configure(meterProviderBuilder);
    }
    SdkMeterProvider meterProvider = meterProviderBuilder.build();

    List<String> exporterNames = config.getCommaSeparatedValues("otel.exporter");
    boolean metricsConfigured = false;
    for (String exporterName : exporterNames) {
      exporterName = exporterName.toLowerCase(Locale.ROOT);
      switch (exporterName) {
        case "otlp":
        case "otlp_metrics":
          if (metricsConfigured) {
            throw new IllegalStateException(
                "Multiple metrics exporters configured. Only one metrics exporter can be "
                    + "configured at a time.");
          }
          configureOtlpMetrics(config, meterProvider);
          metricsConfigured = true;
          break;
        case "prometheus":
          if (metricsConfigured) {
            throw new IllegalStateException(
                "Multiple metrics exporters configured. Only one metrics exporter can be "
                    + "configured at a time.");
          }
          configurePrometheusMetrics(config, meterProvider);
          metricsConfigured = true;
          break;
        default:
          // Ignore
      }
    }

    SdkTracerProviderBuilder tracerProviderBuilder =
        SdkTracerProvider.builder()
            .setResource(resource)
            .setTraceConfig(configureTraceConfig(config));

    List<SpanExporter> spanExporters = new ArrayList<>();
    for (String exporterName : exporterNames) {
      exporterName = exporterName.toLowerCase(Locale.ROOT);
      switch (exporterName) {
        case "otlp":
        case "otlp_span":
          spanExporters.add(configureOtlpSpans(config));
          break;
        case "jaeger":
          spanExporters.add(configureJaeger(config));
          break;
        case "jaeger-thrift":
          spanExporters.add(configureJaegerThrift(config));
          break;
        case "zipkin":
          spanExporters.add(configureZipkin(config));
          break;
        case "logging":
          spanExporters.add(new LoggingSpanExporter());
          break;
        default:
          // Ignore
      }
    }

    if (!spanExporters.isEmpty()) {
      tracerProviderBuilder.addSpanProcessor(configureSpanProcessor(config, spanExporters));
    }

    for (SdkTracerProviderConfigurer configurer :
        ServiceLoader.load(SdkTracerProviderConfigurer.class)) {
      configurer.configure(tracerProviderBuilder);
    }

    List<TextMapPropagator> propagators = new ArrayList<>();
    for (String propagatorName : config.getCommaSeparatedValues("otel.propagators")) {
      switch (propagatorName) {
        case "tracecontext":
          propagators.add(W3CTraceContextPropagator.getInstance());
          break;
        case "baggage":
          propagators.add(W3CBaggagePropagator.getInstance());
          break;
        case "b3":
          propagators.add(B3Propagator.getInstance());
          break;
        case "b3multi":
          propagators.add(B3Propagator.builder().injectMultipleHeaders().build());
          break;
        case "jaeger":
          propagators.add(JaegerPropagator.getInstance());
          break;
        case "ottracer":
          propagators.add(OtTracerPropagator.getInstance());
          break;
        case "xray":
          propagators.add(AwsXRayPropagator.getInstance());
          break;
        default:
          // Ignore
      }
    }

    return OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProviderBuilder.build())
        .setPropagators(ContextPropagators.create(TextMapPropagator.composite(propagators)))
        .build();
  }

  private static void configureOtlpMetrics(
      ConfigProperties config, SdkMeterProvider meterProvider) {
    OtlpGrpcMetricExporterBuilder builder = OtlpGrpcMetricExporter.builder();

    String endpoint = config.getString("otel.exporter.otlp.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    boolean insecure = config.getBoolean("otel.exporter.otlp.insecure");
    if (!insecure) {
      builder.setUseTls(true);
    }

    config.getCommaSeparatedMap("otel.exporter.otlp.headers").forEach(builder::addHeader);

    Long deadlineMs = config.getLong("otel.exporter.otlp.timeout");
    if (deadlineMs != null) {
      builder.setDeadlineMs(deadlineMs);
    }

    OtlpGrpcMetricExporter exporter = builder.build();

    IntervalMetricReader.Builder readerBuilder =
        IntervalMetricReader.builder()
            .setMetricProducers(Collections.singletonList(meterProvider.getMetricProducer()))
            .setMetricExporter(exporter);
    Long exportIntervalMillis = config.getLong("otel.imr.export.interval");
    if (exportIntervalMillis != null) {
      readerBuilder.setExportIntervalMillis(exportIntervalMillis);
    }
    IntervalMetricReader reader = readerBuilder.build();
    Runtime.getRuntime().addShutdownHook(new Thread(reader::shutdown));
  }

  private static SpanExporter configureOtlpSpans(ConfigProperties config) {
    OtlpGrpcSpanExporterBuilder builder = OtlpGrpcSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.otlp.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    boolean insecure = config.getBoolean("otel.exporter.otlp.insecure");
    if (!insecure) {
      builder.setUseTls(true);
    }

    config.getCommaSeparatedMap("otel.exporter.otlp.headers").forEach(builder::addHeader);

    Long deadlineMs = config.getLong("otel.exporter.otlp.timeout");
    if (deadlineMs != null) {
      builder.setDeadlineMs(deadlineMs);
    }

    return builder.build();
  }

  private static void configurePrometheusMetrics(
      ConfigProperties config, SdkMeterProvider meterProvider) {
    PrometheusCollector.builder()
        .setMetricProducer(meterProvider.getMetricProducer())
        .buildAndRegister();
    Integer port = config.getInt("otel.exporter.prometheus.port");
    if (port == null) {
      port = 9464;
    }
    String host = config.getString("otel.exporter.prometheus.host");
    if (host == null) {
      host = "0.0.0.0";
    }
    final HTTPServer server;
    try {
      server = new HTTPServer(host, port, true);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create Prometheus server", e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
  }

  private static TraceConfig configureTraceConfig(ConfigProperties config) {
    TraceConfigBuilder builder = TraceConfig.getDefault().toBuilder();

    Double samplerProbability = config.getDouble("otel.config.sampler.probability");
    if (samplerProbability != null) {
      builder.setSampler(Sampler.traceIdRatioBased(samplerProbability));
    }

    Integer maxAttrs = config.getInt("otel.config.max.attrs");
    if (maxAttrs != null) {
      builder.setMaxNumberOfAttributes(maxAttrs);
    }

    Integer maxEvents = config.getInt("otel.config.max.events");
    if (maxEvents != null) {
      builder.setMaxNumberOfEvents(maxEvents);
    }

    Integer maxLinks = config.getInt("otel.config.max.links");
    if (maxLinks != null) {
      builder.setMaxNumberOfLinks(maxLinks);
    }

    Integer maxEventAttrs = config.getInt("otel.config.max.event.attrs");
    if (maxEventAttrs != null) {
      builder.setMaxNumberOfAttributesPerEvent(maxEventAttrs);
    }

    Integer maxLinkAttrs = config.getInt("otel.config.max.link.attrs");
    if (maxLinkAttrs != null) {
      builder.setMaxNumberOfAttributesPerLink(maxLinkAttrs);
    }

    return builder.build();
  }

  private static SpanExporter configureJaeger(ConfigProperties config) {
    JaegerGrpcSpanExporterBuilder builder = JaegerGrpcSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.jaeger.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    return builder.build();
  }

  private static SpanExporter configureJaegerThrift(ConfigProperties config) {
    JaegerThriftSpanExporterBuilder builder = JaegerThriftSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.jaeger.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    return builder.build();
  }

  private static SpanExporter configureZipkin(ConfigProperties config) {
    ZipkinSpanExporterBuilder builder = ZipkinSpanExporter.builder();

    String endpoint = config.getString("otel.exporter.zipkin.endpoint");
    if (endpoint != null) {
      builder.setEndpoint(endpoint);
    }

    return builder.build();
  }

  private static SpanProcessor configureSpanProcessor(
      ConfigProperties config, List<SpanExporter> exporters) {
    SpanExporter exporter = SpanExporter.composite(exporters);
    BatchSpanProcessorBuilder builder = BatchSpanProcessor.builder(exporter);

    Long scheduleDelayMillis = config.getLong("otel.bsp.schedule.delay");
    if (scheduleDelayMillis != null) {
      builder.setScheduleDelayMillis(scheduleDelayMillis);
    }

    Integer maxQueue = config.getInt("otel.bsp.max.queue");
    if (maxQueue != null) {
      builder.setMaxQueueSize(maxQueue);
    }

    Integer maxExportBatch = config.getInt("otel.bsp.max.export.batch");
    if (maxExportBatch != null) {
      builder.setMaxExportBatchSize(maxExportBatch);
    }

    Integer timeout = config.getInt("otel.bsp.export.timeout");
    if (timeout != null) {
      builder.setExporterTimeoutMillis(timeout);
    }

    return builder.build();
  }

  private OpenTelemetrySdkAutoConfiguration() {}
}
