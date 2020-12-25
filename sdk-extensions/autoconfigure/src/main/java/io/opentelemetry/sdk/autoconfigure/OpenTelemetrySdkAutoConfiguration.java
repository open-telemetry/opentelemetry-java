/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.SdkMeterProviderConfigurer;
import io.opentelemetry.sdk.autoconfigure.spi.SdkTracerProviderConfigurer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

/**
 * Auto-configuration for the OpenTelemetry SDK. As an alternative to programmatically configuring
 * the SDK using {@link OpenTelemetrySdk#builder()}, this package can be used to automatically
 * configure the SDK using environment properties specified by OpenTelemetry.
 */
public final class OpenTelemetrySdkAutoConfiguration {

  /**
   * Returns an {@link OpenTelemetrySdk} automatically initialized through recognized system
   * properties and environment variables.
   */
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
      metricsConfigured =
          MetricExporterConfiguration.configureExporter(
              exporterName, config, metricsConfigured, meterProvider);
    }

    SdkTracerProviderBuilder tracerProviderBuilder =
        SdkTracerProvider.builder()
            .setResource(resource)
            .setTraceConfig(configureTraceConfig(config));

    List<SpanExporter> spanExporters = new ArrayList<>();
    for (String exporterName : exporterNames) {
      exporterName = exporterName.toLowerCase(Locale.ROOT);
      SpanExporter exporter = SpanExporterConfiguration.getExporter(exporterName, config);
      if (exporter != null) {
        spanExporters.add(exporter);
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
      propagatorName = propagatorName.toLowerCase(Locale.ROOT);
      TextMapPropagator propagator = PropagatorConfiguration.getPropagator(propagatorName);
      if (propagator != null) {
        propagators.add(propagator);
      }
    }

    SdkTracerProvider tracerProvider = tracerProviderBuilder.build();
    Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::shutdown));

    return OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setPropagators(ContextPropagators.create(TextMapPropagator.composite(propagators)))
        .build();
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
