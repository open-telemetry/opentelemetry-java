/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.SpanLimitsBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

final class TracerProviderConfiguration {

  private static final double DEFAULT_TRACEIDRATIO_SAMPLE_RATIO = 1.0d;
  private static final String PARENTBASED_ALWAYS_ON = "parentbased_always_on";
  private static final List<String> simpleProcessorExporterNames =
      Arrays.asList("console", "logging");

  static void configureTracerProvider(
      SdkTracerProviderBuilder tracerProviderBuilder,
      ConfigProperties config,
      SpiHelper spiHelper,
      MeterProvider meterProvider,
      BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
          spanExporterCustomizer,
      BiFunction<? super SpanProcessor, ConfigProperties, ? extends SpanProcessor>
          spanProcessorCustomizer,
      BiFunction<? super Sampler, ConfigProperties, ? extends Sampler> samplerCustomizer,
      List<Closeable> closeables) {

    tracerProviderBuilder.setSpanLimits(configureSpanLimits(config));
    InternalTelemetryVersion telemetryVersion = InternalTelemetryConfiguration.getVersion(config);
    if (telemetryVersion != InternalTelemetryVersion.LEGACY) {
      tracerProviderBuilder.setMeterProvider(() -> meterProvider);
    }

    String sampler = config.getString("otel.traces.sampler", PARENTBASED_ALWAYS_ON);
    tracerProviderBuilder.setSampler(
        samplerCustomizer.apply(configureSampler(sampler, config, spiHelper), config));

    Map<String, SpanExporter> exportersByName =
        SpanExporterConfiguration.configureSpanExporters(
            config, spiHelper, spanExporterCustomizer, closeables);

    List<SpanProcessor> processors =
        configureSpanProcessors(
            config, exportersByName, telemetryVersion, meterProvider, closeables);
    for (SpanProcessor processor : processors) {
      SpanProcessor wrapped = spanProcessorCustomizer.apply(processor, config);
      if (wrapped != processor) {
        closeables.add(wrapped);
      }
      tracerProviderBuilder.addSpanProcessor(wrapped);
    }
  }

  static List<SpanProcessor> configureSpanProcessors(
      ConfigProperties config,
      Map<String, SpanExporter> exportersByName,
      InternalTelemetryVersion telemetryVersion,
      MeterProvider meterProvider,
      List<Closeable> closeables) {
    Map<String, SpanExporter> exportersByNameCopy = new HashMap<>(exportersByName);
    List<SpanProcessor> spanProcessors = new ArrayList<>();

    for (String simpleProcessorExporterNames : simpleProcessorExporterNames) {
      SpanExporter exporter = exportersByNameCopy.remove(simpleProcessorExporterNames);
      if (exporter != null) {
        SimpleSpanProcessorBuilder spanProcessorBuilder = SimpleSpanProcessor.builder(exporter);
        if (telemetryVersion != InternalTelemetryVersion.LEGACY) {
          spanProcessorBuilder.setMeterProvider(() -> meterProvider);
        }
        SpanProcessor spanProcessor = spanProcessorBuilder.build();
        closeables.add(spanProcessor);
        spanProcessors.add(spanProcessor);
      }
    }

    if (!exportersByNameCopy.isEmpty()) {
      SpanExporter compositeSpanExporter = SpanExporter.composite(exportersByNameCopy.values());
      SpanProcessor spanProcessor =
          configureBatchSpanProcessor(
              config, compositeSpanExporter, telemetryVersion, meterProvider);
      closeables.add(spanProcessor);
      spanProcessors.add(spanProcessor);
    }

    return spanProcessors;
  }

  // VisibleForTesting
  static BatchSpanProcessor configureBatchSpanProcessor(
      ConfigProperties config,
      SpanExporter exporter,
      InternalTelemetryVersion telemetryVersion,
      MeterProvider meterProvider) {
    BatchSpanProcessorBuilder builder =
        BatchSpanProcessor.builder(exporter).setMeterProvider(() -> meterProvider);

    Duration scheduleDelay = config.getDuration("otel.bsp.schedule.delay");
    if (scheduleDelay != null) {
      builder.setScheduleDelay(scheduleDelay);
    }

    Integer maxQueue = config.getInt("otel.bsp.max.queue.size");
    if (maxQueue != null) {
      builder.setMaxQueueSize(maxQueue);
    }

    Integer maxExportBatch = config.getInt("otel.bsp.max.export.batch.size");
    if (maxExportBatch != null) {
      builder.setMaxExportBatchSize(maxExportBatch);
    }

    Duration timeout = config.getDuration("otel.bsp.export.timeout");
    if (timeout != null) {
      builder.setExporterTimeout(timeout);
    }

    builder.setInternalTelemetryVersion(telemetryVersion);

    return builder.build();
  }

  // Visible for testing
  static SpanLimits configureSpanLimits(ConfigProperties config) {
    SpanLimitsBuilder builder = SpanLimits.builder();

    Integer maxAttrLength = config.getInt("otel.attribute.value.length.limit");
    if (maxAttrLength != null) {
      builder.setMaxAttributeValueLength(maxAttrLength);
    }
    Integer maxSpanAttrLength = config.getInt("otel.span.attribute.value.length.limit");
    if (maxSpanAttrLength != null) {
      builder.setMaxAttributeValueLength(maxSpanAttrLength);
    }

    Integer maxAttrs = config.getInt("otel.attribute.count.limit");
    if (maxAttrs != null) {
      builder.setMaxNumberOfAttributes(maxAttrs);
      builder.setMaxNumberOfAttributesPerEvent(maxAttrs);
      builder.setMaxNumberOfAttributesPerLink(maxAttrs);
    }
    Integer maxSpanAttrs = config.getInt("otel.span.attribute.count.limit");
    if (maxSpanAttrs != null) {
      builder.setMaxNumberOfAttributes(maxSpanAttrs);
    }

    Integer maxEvents = config.getInt("otel.span.event.count.limit");
    if (maxEvents != null) {
      builder.setMaxNumberOfEvents(maxEvents);
    }

    Integer maxLinks = config.getInt("otel.span.link.count.limit");
    if (maxLinks != null) {
      builder.setMaxNumberOfLinks(maxLinks);
    }

    return builder.build();
  }

  // Visible for testing
  static Sampler configureSampler(String sampler, ConfigProperties config, SpiHelper spiHelper) {
    NamedSpiManager<Sampler> spiSamplersManager =
        spiHelper.loadConfigurable(
            ConfigurableSamplerProvider.class,
            ConfigurableSamplerProvider::getName,
            ConfigurableSamplerProvider::createSampler,
            config);

    switch (sampler) {
      case "always_on":
        return Sampler.alwaysOn();
      case "always_off":
        return Sampler.alwaysOff();
      case "traceidratio":
        return ratioSampler(config);
      case PARENTBASED_ALWAYS_ON:
        return Sampler.parentBased(Sampler.alwaysOn());
      case "parentbased_always_off":
        return Sampler.parentBased(Sampler.alwaysOff());
      case "parentbased_traceidratio":
        return Sampler.parentBased(ratioSampler(config));
      case "parentbased_jaeger_remote":
        Sampler jaegerRemote = spiSamplersManager.getByName("jaeger_remote");
        if (jaegerRemote == null) {
          throw new ConfigurationException(
              "parentbased_jaeger_remote configured but opentelemetry-sdk-extension-jaeger-remote-sampler not on classpath");
        }
        return Sampler.parentBased(jaegerRemote);
      default:
        Sampler spiSampler = spiSamplersManager.getByName(sampler);
        if (spiSampler == null) {
          throw new ConfigurationException(
              "Unrecognized value for otel.traces.sampler: " + sampler);
        }
        return spiSampler;
    }
  }

  private static Sampler ratioSampler(ConfigProperties config) {
    double ratio = config.getDouble("otel.traces.sampler.arg", DEFAULT_TRACEIDRATIO_SAMPLE_RATIO);
    return Sampler.traceIdRatioBased(ratio);
  }

  private TracerProviderConfiguration() {}
}
