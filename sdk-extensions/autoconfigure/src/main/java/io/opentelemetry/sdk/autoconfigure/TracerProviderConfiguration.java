/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.SdkTracerProviderConfigurer;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.config.TraceConfigBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.time.Duration;
import java.util.ServiceLoader;

final class TracerProviderConfiguration {

  static SdkTracerProvider configureTracerProvider(Resource resource, ConfigProperties config) {
    SdkTracerProviderBuilder tracerProviderBuilder =
        SdkTracerProvider.builder()
            .setResource(resource)
            .setTraceConfig(configureTraceConfig(config));

    String sampler = config.getString("otel.trace.sampler");
    if (sampler != null) {
      tracerProviderBuilder.setSampler(configureSampler(sampler, config));
    }

    // Run user configuration before setting exporters from environment to allow user span
    // processors to effect export.
    for (SdkTracerProviderConfigurer configurer :
        ServiceLoader.load(SdkTracerProviderConfigurer.class)) {
      configurer.configure(tracerProviderBuilder);
    }

    String exporterName = config.getString("otel.trace.exporter");
    if (exporterName != null) {
      SpanExporter exporter = SpanExporterConfiguration.configureExporter(exporterName, config);
      if (exporter != null) {
        tracerProviderBuilder.addSpanProcessor(configureSpanProcessor(config, exporter));
      }
    }

    SdkTracerProvider tracerProvider = tracerProviderBuilder.build();
    Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::shutdown));
    return tracerProvider;
  }

  // VisibleForTesting
  static BatchSpanProcessor configureSpanProcessor(ConfigProperties config, SpanExporter exporter) {
    BatchSpanProcessorBuilder builder = BatchSpanProcessor.builder(exporter);

    Long scheduleDelayMillis = config.getLong("otel.bsp.schedule.delay");
    if (scheduleDelayMillis != null) {
      builder.setScheduleDelay(Duration.ofMillis(scheduleDelayMillis));
    }

    Integer maxQueue = config.getInt("otel.bsp.max.queue.size");
    if (maxQueue != null) {
      builder.setMaxQueueSize(maxQueue);
    }

    Integer maxExportBatch = config.getInt("otel.bsp.max.export.batch.size");
    if (maxExportBatch != null) {
      builder.setMaxExportBatchSize(maxExportBatch);
    }

    Integer timeout = config.getInt("otel.bsp.export.timeout");
    if (timeout != null) {
      builder.setExporterTimeout(Duration.ofMillis(timeout));
    }

    return builder.build();
  }

  // Visible for testing
  static TraceConfig configureTraceConfig(ConfigProperties config) {
    TraceConfigBuilder builder = TraceConfig.builder();

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
    switch (sampler) {
      case "always_on":
        return Sampler.alwaysOn();
      case "always_off":
        return Sampler.alwaysOff();
      case "traceidratio":
        {
          Double ratio = config.getDouble("otel.trace.sampler.arg");
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
          Double ratio = config.getDouble("otel.trace.sampler.arg");
          if (ratio == null) {
            ratio = 1.0d;
          }
          return Sampler.parentBased(Sampler.traceIdRatioBased(ratio));
        }
      default:
        throw new ConfigurationException("Unrecognized value for otel.trace.sampler: " + sampler);
    }
  }

  private TracerProviderConfiguration() {}
}
