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
import java.util.List;
import java.util.Locale;

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

    Resource resource = configureResource(config);

    List<String> exporterNames = config.getCommaSeparatedValues("otel.exporter");

    configureMeterProvider(resource, exporterNames, config);

    SdkTracerProvider tracerProvider =
        TracerProviderConfiguration.configureTracerProvider(resource, exporterNames, config);
    ContextPropagators propagators = PropagatorConfiguration.configurePropagators(config);

    return OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setPropagators(propagators)
        .build();
  }

  private static void configureMeterProvider(
      Resource resource, List<String> exporterNames, ConfigProperties config) {
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().setResource(resource).buildAndRegisterGlobal();

    boolean metricsConfigured = false;
    for (String exporterName : exporterNames) {
      exporterName = exporterName.toLowerCase(Locale.ROOT);
      metricsConfigured =
          MetricExporterConfiguration.configureExporter(
              exporterName, config, metricsConfigured, meterProvider);
    }
  }

  private static Resource configureResource(ConfigProperties config) {
    AttributesBuilder resourceAttributes = Attributes.builder();
    config.getCommaSeparatedMap("otel.resource.attributes").forEach(resourceAttributes::put);
    return Resource.create(resourceAttributes.build()).merge(Resource.getDefault());
  }

  private OpenTelemetrySdkAutoConfiguration() {}
}
