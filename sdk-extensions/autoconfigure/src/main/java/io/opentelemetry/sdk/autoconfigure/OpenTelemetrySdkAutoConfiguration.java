/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.SdkMeterProviderConfigurer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.ServiceLoader;
import java.util.function.BiFunction;

/**
 * Auto-configuration for the OpenTelemetry SDK. As an alternative to programmatically configuring
 * the SDK using {@link OpenTelemetrySdk#builder()}, this package can be used to automatically
 * configure the SDK using environment properties specified by OpenTelemetry.
 *
 * @deprecated Use {@link AutoConfiguredOpenTelemetrySdk}.
 */
@Deprecated
public final class OpenTelemetrySdkAutoConfiguration {

  /**
   * Returns an {@link OpenTelemetrySdk} automatically initialized through recognized system
   * properties and environment variables.
   *
   * <p>This will automatically set the resulting SDK as the {@link
   * io.opentelemetry.api.GlobalOpenTelemetry} instance.
   *
   * @deprecated Use {@link AutoConfiguredOpenTelemetrySdk#initialize()}.
   */
  @Deprecated
  public static OpenTelemetrySdk initialize() {
    return AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
  }

  /**
   * Returns an {@link OpenTelemetrySdk} automatically initialized through recognized system
   * properties and environment variables.
   *
   * @param setResultAsGlobal Whether to automatically set the configured SDK as the {@link
   *     io.opentelemetry.api.GlobalOpenTelemetry} instance.
   * @deprecated Use {@code
   *     AutoConfiguredSdk.builder().setResultAsGlobal(setResultAsGlobal).build().getOpenTelemetrySdk()}.
   */
  @Deprecated
  public static OpenTelemetrySdk initialize(boolean setResultAsGlobal) {
    return AutoConfiguredOpenTelemetrySdk.builder()
        .setResultAsGlobal(setResultAsGlobal)
        .build()
        .getOpenTelemetrySdk();
  }

  /**
   * Returns an {@link OpenTelemetrySdk} automatically initialized through recognized properties
   * contained in the {@code config} parameter.
   *
   * @param setResultAsGlobal Whether to automatically set the configured SDK as the {@link
   *     GlobalOpenTelemetry} instance.
   * @param config A {@link ConfigProperties} instance that contains properties that are to be used
   *     to auto-configure the returned {@link OpenTelemetrySdk}.
   * @deprecated Use {@code
   *     AutoConfiguredSdk.builder().setResultAsGlobal(setResultAsGlobal).setConfig(config).build().getOpenTelemetrySdk()}.
   */
  @Deprecated
  public static OpenTelemetrySdk initialize(boolean setResultAsGlobal, ConfigProperties config) {
    return AutoConfiguredOpenTelemetrySdk.builder()
        .setResultAsGlobal(setResultAsGlobal)
        .setConfig(config)
        .build()
        .getOpenTelemetrySdk();
  }

  static OpenTelemetrySdk newOpenTelemetrySdk(
      ConfigProperties config,
      Resource resource,
      ClassLoader serviceClassLoader,
      BiFunction<? super TextMapPropagator, ConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer,
      BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
          spanExporterCustomizer,
      BiFunction<? super Sampler, ConfigProperties, ? extends Sampler> samplerCustomizer,
      boolean setResultAsGlobal) {
    ContextPropagators propagators =
        PropagatorConfiguration.configurePropagators(
            config, serviceClassLoader, propagatorCustomizer);

    MeterProvider meterProvider = configureMeterProvider(resource, config, serviceClassLoader);

    SdkTracerProvider tracerProvider =
        TracerProviderConfiguration.configureTracerProvider(
            resource,
            config,
            serviceClassLoader,
            meterProvider,
            spanExporterCustomizer,
            samplerCustomizer);

    OpenTelemetrySdkBuilder sdkBuilder =
        OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).setPropagators(propagators);

    if (meterProvider instanceof SdkMeterProvider) {
      sdkBuilder.setMeterProvider((SdkMeterProvider) meterProvider);
    }

    OpenTelemetrySdk openTelemetrySdk = sdkBuilder.build();
    if (setResultAsGlobal) {
      GlobalOpenTelemetry.set(openTelemetrySdk);
    }
    return openTelemetrySdk;
  }

  private static MeterProvider configureMeterProvider(
      Resource resource, ConfigProperties config, ClassLoader serviceClassLoader) {
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
        ServiceLoader.load(SdkMeterProviderConfigurer.class, serviceClassLoader)) {
      configurer.configure(meterProviderBuilder, config);
    }

    String exporterName = config.getString("otel.metrics.exporter");
    if (exporterName == null || exporterName.equals("none")) {
      // In the event no exporters are configured set a noop exporter
      return MeterProvider.noop();
    }
    MetricExporterConfiguration.configureExporter(
        exporterName, config, serviceClassLoader, meterProviderBuilder);

    SdkMeterProvider meterProvider = meterProviderBuilder.build();

    // Make sure metrics shut down when JVM shuts down.
    Runtime.getRuntime().addShutdownHook(new Thread(meterProvider::close));

    return meterProvider;
  }

  private OpenTelemetrySdkAutoConfiguration() {}
}
