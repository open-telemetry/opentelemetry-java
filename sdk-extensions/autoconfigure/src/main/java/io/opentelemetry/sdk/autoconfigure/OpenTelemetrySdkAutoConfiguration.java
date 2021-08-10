/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.SdkMeterProviderConfigurer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
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
   *
   * <p>This will automatically set the resulting SDK as the {@link
   * io.opentelemetry.api.GlobalOpenTelemetry} instance.
   */
  public static OpenTelemetrySdk initialize() {
    return initialize(true);
  }

  /**
   * Returns an {@link OpenTelemetrySdk} automatically initialized through recognized system
   * properties and environment variables.
   *
   * @param setResultAsGlobal Whether to automatically set the configured SDK as the {@link
   *     io.opentelemetry.api.GlobalOpenTelemetry} instance.
   */
  public static OpenTelemetrySdk initialize(boolean setResultAsGlobal) {
    return initialize(setResultAsGlobal, DefaultConfigProperties.get());
  }

  /**
   * Returns an {@link OpenTelemetrySdk} automatically initialized through recognized properties
   * contained in the {@code config} parameter.
   *
   * @param setResultAsGlobal Whether to automatically set the configured SDK as the {@link
   *     GlobalOpenTelemetry} instance.
   * @param config A {@link ConfigProperties} instance that contains properties that are to be used
   *     to auto-configure the returned {@link OpenTelemetrySdk}.
   */
  public static OpenTelemetrySdk initialize(boolean setResultAsGlobal, ConfigProperties config) {
    ContextPropagators propagators = PropagatorConfiguration.configurePropagators(config);

    Resource resource = OpenTelemetryResourceAutoConfiguration.configureResource(config);

    configureMeterProvider(resource, config);

    SdkTracerProvider tracerProvider =
        TracerProviderConfiguration.configureTracerProvider(resource, config);

    OpenTelemetrySdk openTelemetrySdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(propagators)
            .build();
    if (setResultAsGlobal) {
      GlobalOpenTelemetry.set(openTelemetrySdk);
    }
    return openTelemetrySdk;
  }

  private static void configureMeterProvider(Resource resource, ConfigProperties config) {
    SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder().setResource(resource);

    for (SdkMeterProviderConfigurer configurer :
        ServiceLoader.load(SdkMeterProviderConfigurer.class)) {
      configurer.configure(meterProviderBuilder);
    }

    SdkMeterProvider meterProvider = meterProviderBuilder.buildAndRegisterGlobal();

    String exporterName = config.getString("otel.metrics.exporter");
    if (exporterName == null) {
      exporterName = "otlp";
    }
    MetricExporterConfiguration.configureExporter(exporterName, config, meterProvider);
  }

  private OpenTelemetrySdkAutoConfiguration() {}
}
