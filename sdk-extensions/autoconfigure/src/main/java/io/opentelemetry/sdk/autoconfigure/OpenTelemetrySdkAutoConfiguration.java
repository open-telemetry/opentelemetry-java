/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.SdkMeterProviderConfigurer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Auto-configuration for the OpenTelemetry SDK. As an alternative to programmatically configuring
 * the SDK using {@link OpenTelemetrySdk#builder()}, this package can be used to automatically
 * configure the SDK using environment properties specified by OpenTelemetry.
 */
public final class OpenTelemetrySdkAutoConfiguration {

  private static volatile Resource resource = Resource.empty();

  /**
   * Returns the automatically configured {@link Resource} after one of {@code initialize()} methods
   * has been called. Prior to that, this method will always return an {@linkplain Resource#empty()
   * empty resource}.
   */
  public static Resource getResource() {
    return resource;
  }

  /**
   * Returns an {@link OpenTelemetrySdk} automatically initialized through recognized properties
   * contained in passed {@code config}.
   *
   * @param setResultAsGlobal Whether to automatically set the configured SDK as the {@link
   *     GlobalOpenTelemetry} instance.
   * @param config A {@link ConfigProperties} instance that contains properties that are to be used
   *     to auto-configure the returned {@link OpenTelemetrySdk}.
   */
  public static OpenTelemetrySdk initialize(boolean setResultAsGlobal, ConfigProperties config) {
    ContextPropagators propagators = PropagatorConfiguration.configurePropagators(config);

    resource = buildResource(config);

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
   * Returns an {@link OpenTelemetrySdk} automatically initialized through recognized system
   * properties and environment variables.
   *
   * <p>This will automatically set the resulting SDK as the {@link
   * io.opentelemetry.api.GlobalOpenTelemetry} instance.
   */
  public static OpenTelemetrySdk initialize() {
    return initialize(true);
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

  private static Resource buildResource(ConfigProperties config) {
    Resource result = Resource.getDefault();

    // TODO(anuraaga): We use a hyphen only once in this artifact, for
    // otel.java.disabled.resource-providers. But fetching by the dot version is the simplest way
    // to implement it for now.
    Set<String> disabledProviders =
        new HashSet<>(config.getCommaSeparatedValues("otel.java.disabled.resource.providers"));
    for (ResourceProvider resourceProvider : ServiceLoader.load(ResourceProvider.class)) {
      if (disabledProviders.contains(resourceProvider.getClass().getName())) {
        continue;
      }
      result = result.merge(resourceProvider.createResource(config));
    }

    result = result.merge(EnvironmentResource.create(config));

    return result;
  }

  private OpenTelemetrySdkAutoConfiguration() {}
}
