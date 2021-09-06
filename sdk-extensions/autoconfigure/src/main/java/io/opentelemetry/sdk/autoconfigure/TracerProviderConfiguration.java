/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.traces.SdkTracerProviderConfigurer;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
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
import java.util.ServiceLoader;

final class TracerProviderConfiguration {

  static SdkTracerProvider configureTracerProvider(Resource resource, ConfigProperties config) {
    SdkTracerProviderBuilder tracerProviderBuilder =
        SdkTracerProvider.builder()
            .setResource(resource)
            .setSpanLimits(configureSpanLimits(config));

    String sampler = config.getString("otel.traces.sampler");
    if (sampler != null) {
      tracerProviderBuilder.setSampler(configureSampler(sampler, config));
    }

    // Run user configuration before setting exporters from environment to allow user span
    // processors to effect export.
    for (SdkTracerProviderConfigurer configurer :
        ServiceLoader.load(SdkTracerProviderConfigurer.class)) {
      configurer.configure(tracerProviderBuilder, config);
    }

    Map<String, SpanExporter> exportersByName =
        SpanExporterConfiguration.configureSpanExporters(config);

    configureSpanProcessors(config, exportersByName)
        .forEach(tracerProviderBuilder::addSpanProcessor);

    SdkTracerProvider tracerProvider = tracerProviderBuilder.build();
    Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));
    return tracerProvider;
  }

  static List<SpanProcessor> configureSpanProcessors(
      ConfigProperties config, Map<String, SpanExporter> exportersByName) {
    Map<String, SpanExporter> exportersByNameCopy = new HashMap<>(exportersByName);
    List<SpanProcessor> spanProcessors = new ArrayList<>();

    if (exportersByNameCopy.containsKey("logging")) {
      spanProcessors.add(SimpleSpanProcessor.create(exportersByName.get("logging")));
      exportersByNameCopy.remove("logging");
    }

    if (!exportersByNameCopy.isEmpty()) {
      SpanExporter compositeSpanExporter = SpanExporter.composite(exportersByNameCopy.values());
      spanProcessors.add(configureBatchSpanProcessor(config, compositeSpanExporter));
    }

    return spanProcessors;
  }

  // VisibleForTesting
  static BatchSpanProcessor configureBatchSpanProcessor(
      ConfigProperties config, SpanExporter exporter) {
    BatchSpanProcessorBuilder builder = BatchSpanProcessor.builder(exporter);

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

    Integer maxAttrs = config.getInt("otel.span.attribute.count.limit");
    if (maxAttrs != null) {
      builder.setMaxNumberOfAttributes(maxAttrs);
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
  static Sampler configureSampler(String sampler, ConfigProperties config) {
    Map<String, Sampler> spiSamplers =
        SpiUtil.loadConfigurable(
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
        Sampler spiSampler = spiSamplers.get(sampler);
        if (spiSampler == null) {
          throw new ConfigurationException(
              "Unrecognized value for otel.traces.sampler: " + sampler);
        }
        return spiSampler;
    }
  }

  private TracerProviderConfiguration() {}
}
