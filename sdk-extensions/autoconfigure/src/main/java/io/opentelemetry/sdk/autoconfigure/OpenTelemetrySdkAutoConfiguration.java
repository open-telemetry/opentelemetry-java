/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.SdkMeterProviderConfigurer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
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

    // Configure default exemplar filters.
    String exemplarFilter = config.getString("otel.metrics.exemplar.filter");
    if (exemplarFilter == null) {
      exemplarFilter = "with_sampled_trace";
    }
    switch (exemplarFilter) {
      case "none":
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.neverSample());
        break;
      case "all":
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.alwaysSample());
        break;
      case "with_sampled_trace":
      default:
        meterProviderBuilder.setExemplarFilter(ExemplarFilter.sampleWithTraces());
        break;
    }

    for (SdkMeterProviderConfigurer configurer :
        ServiceLoader.load(SdkMeterProviderConfigurer.class)) {
      configurer.configure(meterProviderBuilder, config);
    }

    String exporterName = config.getString("otel.metrics.exporter");
    if (exporterName == null) {
      exporterName = "none";
    }
    MetricExporterConfiguration.configureExporter(exporterName, config, meterProviderBuilder);

    // In the event no exporters are configured, this returns a stubbed SdkMeterProvider.
    SdkMeterProvider meterProvider = meterProviderBuilder.buildAndRegisterGlobal();

    // Make sure metrics shut down when JVM shuts down.
    Runtime.getRuntime().addShutdownHook(new Thread(meterProvider::close));
  }

  private OpenTelemetrySdkAutoConfiguration() {}
}
