/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

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
    ContextPropagators propagators = PropagatorConfiguration.configurePropagators(config);

    Resource resource = configureResource(config);

    Set<String> exporterNames =
        new LinkedHashSet<>(config.getCommaSeparatedValues("otel.exporter"));

    Set<String> unrecognizedExporters = new LinkedHashSet<>(exporterNames);
    unrecognizedExporters.removeAll(SpanExporterConfiguration.RECOGNIZED_NAMES);
    unrecognizedExporters.removeAll(MetricExporterConfiguration.RECOGNIZED_NAMES);
    if (!unrecognizedExporters.isEmpty()) {
      throw new ConfigurationException(
          "Unrecognized value for otel.exporter: " + String.join(",", exporterNames));
    }

    configureMeterProvider(resource, exporterNames, config);

    SdkTracerProvider tracerProvider =
        TracerProviderConfiguration.configureTracerProvider(resource, exporterNames, config);

    return OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setPropagators(propagators)
        .build();
  }

  private static void configureMeterProvider(
      Resource resource, Set<String> exporterNames, ConfigProperties config) {
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().setResource(resource).buildAndRegisterGlobal();

    boolean metricsConfigured = false;
    for (String exporterName : new ArrayList<>(exporterNames)) {
      metricsConfigured =
          MetricExporterConfiguration.configureExporter(
              exporterName, config, metricsConfigured, meterProvider);
    }
  }

  // Visible for testing
  static Resource configureResource(ConfigProperties config) {
    AttributesBuilder resourceAttributes = Attributes.builder();
    config.getCommaSeparatedMap("otel.resource.attributes").forEach(resourceAttributes::put);
    return Resource.create(resourceAttributes.build()).merge(Resource.getDefault());
  }

  private OpenTelemetrySdkAutoConfiguration() {}
}
