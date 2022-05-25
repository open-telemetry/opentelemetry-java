/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.SpanLimitsBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

final class TracerProviderConfiguration {

  static void configureTracerProvider(
      SdkTracerProviderBuilder tracerProviderBuilder,
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      MeterProvider meterProvider,
      BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
          spanExporterCustomizer,
      BiFunction<? super Sampler, ConfigProperties, ? extends Sampler> samplerCustomizer) {

    tracerProviderBuilder.setSpanLimits(configureSpanLimits(config));

    String sampler = config.getString("otel.traces.sampler");
    if (sampler == null) {
      sampler = "parentbased_always_on";
    }
    tracerProviderBuilder.setSampler(
        samplerCustomizer.apply(configureSampler(sampler, config, serviceClassLoader), config));

    Map<String, SpanExporter> exportersByName =
        SpanExporterConfiguration.configureSpanExporters(
            config, serviceClassLoader, meterProvider, spanExporterCustomizer);

    configureSpanProcessors(config, exportersByName, meterProvider)
        .forEach(tracerProviderBuilder::addSpanProcessor);
  }

  static List<SpanProcessor> configureSpanProcessors(
      ConfigProperties config,
      Map<String, SpanExporter> exportersByName,
      MeterProvider meterProvider) {
    Map<String, SpanExporter> exportersByNameCopy = new HashMap<>(exportersByName);
    List<SpanProcessor> spanProcessors = new ArrayList<>();

    SpanExporter exporter = exportersByNameCopy.remove("logging");
    if (exporter != null) {
      spanProcessors.add(SimpleSpanProcessor.create(exporter));
    }

    if (!exportersByNameCopy.isEmpty()) {
      SpanExporter compositeSpanExporter = SpanExporter.composite(exportersByNameCopy.values());
      spanProcessors.add(configureBatchSpanProcessor(config, compositeSpanExporter, meterProvider));
    }

    return spanProcessors;
  }

  // VisibleForTesting
  static BatchSpanProcessor configureBatchSpanProcessor(
      ConfigProperties config, SpanExporter exporter, MeterProvider meterProvider) {
    BatchSpanProcessorBuilder builder =
        BatchSpanProcessor.builder(exporter).setMeterProvider(meterProvider);

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
  static Sampler configureSampler(
      String sampler, ConfigProperties config, ClassLoader serviceClassLoader) {
    NamedSpiManager<Sampler> spiSamplersManager =
        SpiUtil.loadConfigurable(
            ConfigurableSamplerProvider.class,
            ConfigurableSamplerProvider::getName,
            ConfigurableSamplerProvider::createSampler,
            config,
            serviceClassLoader);

    switch (sampler) {
      case "always_on":
        return Sampler.alwaysOn();
      case "always_off":
        return Sampler.alwaysOff();
      case "traceidratio":
        {
          Double ratio = config.getDouble("otel.traces.sampler.arg");
          if (ratio == null) {
            ratio = 1.0d;
          }
          return Sampler.traceIdRatioBased(ratio);
        }
      case "parentbased_always_on":
        return Sampler.parentBased(Sampler.alwaysOn());
      case "parentbased_always_off":
        return Sampler.parentBased(Sampler.alwaysOff());
      case "parentbased_traceidratio":
        {
          Double ratio = config.getDouble("otel.traces.sampler.arg");
          if (ratio == null) {
            ratio = 1.0d;
          }
          return Sampler.parentBased(Sampler.traceIdRatioBased(ratio));
        }
      default:
        Sampler spiSampler = spiSamplersManager.getByName(sampler);
        if (spiSampler == null) {
          throw new ConfigurationException(
              "Unrecognized value for otel.traces.sampler: " + sampler);
        }
        return spiSampler;
    }
  }

  private TracerProviderConfiguration() {}
}
