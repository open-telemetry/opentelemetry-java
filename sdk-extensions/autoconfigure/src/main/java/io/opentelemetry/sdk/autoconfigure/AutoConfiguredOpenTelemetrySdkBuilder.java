/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.events.GlobalEventEmitterProvider;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.SdkEventEmitterProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A builder for configuring auto-configuration of the OpenTelemetry SDK. Notably, auto-configured
 * components can be customized, for example by delegating to them from a wrapper that tweaks
 * behavior such as filtering out telemetry attributes.
 */
public final class AutoConfiguredOpenTelemetrySdkBuilder implements AutoConfigurationCustomizer {

  private static final Logger logger =
      Logger.getLogger(AutoConfiguredOpenTelemetrySdkBuilder.class.getName());

  @Nullable private ConfigProperties config;

  private BiFunction<SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder>
      tracerProviderCustomizer = (a, unused) -> a;
  private BiFunction<? super TextMapPropagator, ConfigProperties, ? extends TextMapPropagator>
      propagatorCustomizer = (a, unused) -> a;
  private BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
      spanExporterCustomizer = (a, unused) -> a;
  private BiFunction<? super Sampler, ConfigProperties, ? extends Sampler> samplerCustomizer =
      (a, unused) -> a;

  private BiFunction<SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>
      meterProviderCustomizer = (a, unused) -> a;
  private BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
      metricExporterCustomizer = (a, unused) -> a;

  private BiFunction<SdkLoggerProviderBuilder, ConfigProperties, SdkLoggerProviderBuilder>
      loggerProviderCustomizer = (a, unused) -> a;
  private BiFunction<? super LogRecordExporter, ConfigProperties, ? extends LogRecordExporter>
      logRecordExporterCustomizer = (a, unused) -> a;

  private BiFunction<? super Resource, ConfigProperties, ? extends Resource> resourceCustomizer =
      (a, unused) -> a;

  private Supplier<Map<String, String>> propertiesSupplier = Collections::emptyMap;

  private final List<Function<ConfigProperties, Map<String, String>>> propertiesCustomizers =
      new ArrayList<>();

  private ClassLoader serviceClassLoader =
      AutoConfiguredOpenTelemetrySdkBuilder.class.getClassLoader();

  private boolean registerShutdownHook = true;

  private boolean setResultAsGlobal = true;

  private boolean customized;

  AutoConfiguredOpenTelemetrySdkBuilder() {}

  /**
   * Sets the {@link ConfigProperties} to use when resolving properties for auto-configuration.
   * {@link #addPropertiesSupplier(Supplier)} and {@link #addPropertiesCustomizer(Function)} will
   * have no effect if this method is used.
   */
  AutoConfiguredOpenTelemetrySdkBuilder setConfig(ConfigProperties config) {
    requireNonNull(config, "config");
    this.config = config;
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke the with the {@link SdkTracerProviderBuilder} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addTracerProviderCustomizer(
      BiFunction<SdkTracerProviderBuilder, ConfigProperties, SdkTracerProviderBuilder>
          tracerProviderCustomizer) {
    requireNonNull(tracerProviderCustomizer, "tracerProviderCustomizer");
    this.tracerProviderCustomizer =
        mergeCustomizer(this.tracerProviderCustomizer, tracerProviderCustomizer);
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link TextMapPropagator}
   * to allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addPropagatorCustomizer(
      BiFunction<? super TextMapPropagator, ConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer) {
    requireNonNull(propagatorCustomizer, "propagatorCustomizer");
    this.propagatorCustomizer = mergeCustomizer(this.propagatorCustomizer, propagatorCustomizer);
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link Resource} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addResourceCustomizer(
      BiFunction<? super Resource, ConfigProperties, ? extends Resource> resourceCustomizer) {
    requireNonNull(resourceCustomizer, "resourceCustomizer");
    this.resourceCustomizer = mergeCustomizer(this.resourceCustomizer, resourceCustomizer);
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link Sampler} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addSamplerCustomizer(
      BiFunction<? super Sampler, ConfigProperties, ? extends Sampler> samplerCustomizer) {
    requireNonNull(samplerCustomizer, "samplerCustomizer");
    this.samplerCustomizer = mergeCustomizer(this.samplerCustomizer, samplerCustomizer);
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link SpanExporter} to
   * allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addSpanExporterCustomizer(
      BiFunction<? super SpanExporter, ConfigProperties, ? extends SpanExporter>
          spanExporterCustomizer) {
    requireNonNull(spanExporterCustomizer, "spanExporterCustomizer");
    this.spanExporterCustomizer =
        mergeCustomizer(this.spanExporterCustomizer, spanExporterCustomizer);
    return this;
  }

  /**
   * Adds a {@link Supplier} of a map of property names and values to use as defaults for the {@link
   * ConfigProperties} used during auto-configuration. The order of precedence of properties is
   * system properties > environment variables > the suppliers registered with this method.
   *
   * <p>Multiple calls will cause properties to be merged in order, with later ones overwriting
   * duplicate keys in earlier ones.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addPropertiesSupplier(
      Supplier<Map<String, String>> propertiesSupplier) {
    requireNonNull(propertiesSupplier, "propertiesSupplier");
    this.propertiesSupplier = mergeProperties(this.propertiesSupplier, propertiesSupplier);
    return this;
  }

  /**
   * Adds a {@link Function} to invoke the with the {@link ConfigProperties} to allow customization.
   * The return value of the {@link Function} will be merged into the {@link ConfigProperties}
   * before it is used for auto-configuration, overwriting the properties that are already there.
   *
   * <p>Multiple calls will cause properties to be merged in order, with later ones overwriting
   * duplicate keys in earlier ones.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addPropertiesCustomizer(
      Function<ConfigProperties, Map<String, String>> propertiesCustomizer) {
    requireNonNull(propertiesCustomizer, "propertiesCustomizer");
    this.propertiesCustomizers.add(propertiesCustomizer);
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke the with the {@link SdkMeterProviderBuilder} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addMeterProviderCustomizer(
      BiFunction<SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>
          meterProviderCustomizer) {
    requireNonNull(meterProviderCustomizer, "meterProviderCustomizer");
    this.meterProviderCustomizer =
        mergeCustomizer(this.meterProviderCustomizer, meterProviderCustomizer);
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link SpanExporter} to
   * allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addMetricExporterCustomizer(
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          metricExporterCustomizer) {
    requireNonNull(metricExporterCustomizer, "metricExporterCustomizer");
    this.metricExporterCustomizer =
        mergeCustomizer(this.metricExporterCustomizer, metricExporterCustomizer);
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke the with the {@link SdkLoggerProviderBuilder} to allow
   * customization. The return value of the {@link BiFunction} will replace the passed-in argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addLoggerProviderCustomizer(
      BiFunction<SdkLoggerProviderBuilder, ConfigProperties, SdkLoggerProviderBuilder>
          loggerProviderCustomizer) {
    requireNonNull(loggerProviderCustomizer, "loggerProviderCustomizer");
    this.loggerProviderCustomizer =
        mergeCustomizer(this.loggerProviderCustomizer, loggerProviderCustomizer);
    return this;
  }

  /**
   * Adds a {@link BiFunction} to invoke with the default autoconfigured {@link LogRecordExporter}
   * to allow customization. The return value of the {@link BiFunction} will replace the passed-in
   * argument.
   *
   * <p>Multiple calls will execute the customizers in order.
   */
  @Override
  public AutoConfiguredOpenTelemetrySdkBuilder addLogRecordExporterCustomizer(
      BiFunction<? super LogRecordExporter, ConfigProperties, ? extends LogRecordExporter>
          logRecordExporterCustomizer) {
    requireNonNull(logRecordExporterCustomizer, "logRecordExporterCustomizer");
    this.logRecordExporterCustomizer =
        mergeCustomizer(this.logRecordExporterCustomizer, logRecordExporterCustomizer);
    return this;
  }

  /**
   * Control the registration of a shutdown hook to shut down the SDK when appropriate. By default,
   * the shutdown hook is registered.
   *
   * <p>Skipping the registration of the shutdown hook may cause unexpected behavior. This
   * configuration is for SDK consumers that require control over the SDK lifecycle. In this case,
   * alternatives must be provided by the SDK consumer to shut down the SDK.
   *
   * @param registerShutdownHook a boolean <code>true</code> will register the hook, otherwise
   *     <code>false</code> will skip registration.
   */
  public AutoConfiguredOpenTelemetrySdkBuilder registerShutdownHook(boolean registerShutdownHook) {
    this.registerShutdownHook = registerShutdownHook;
    return this;
  }

  /**
   * Sets whether the configured {@link OpenTelemetrySdk} should be set as the application's
   * {@linkplain io.opentelemetry.api.GlobalOpenTelemetry global} instance.
   */
  public AutoConfiguredOpenTelemetrySdkBuilder setResultAsGlobal(boolean setResultAsGlobal) {
    this.setResultAsGlobal = setResultAsGlobal;
    return this;
  }

  /** Sets the {@link ClassLoader} to be used to load SPI implementations. */
  public AutoConfiguredOpenTelemetrySdkBuilder setServiceClassLoader(
      ClassLoader serviceClassLoader) {
    requireNonNull(serviceClassLoader, "serviceClassLoader");
    this.serviceClassLoader = serviceClassLoader;
    return this;
  }

  /**
   * Returns a new {@link AutoConfiguredOpenTelemetrySdk} holding components auto-configured using
   * the settings of this {@link AutoConfiguredOpenTelemetrySdkBuilder}.
   */
  public AutoConfiguredOpenTelemetrySdk build() {
    if (!customized) {
      customized = true;
      mergeSdkTracerProviderConfigurer();
      for (AutoConfigurationCustomizerProvider customizer :
          SpiUtil.loadOrdered(AutoConfigurationCustomizerProvider.class, serviceClassLoader)) {
        customizer.customize(this);
      }
    }

    ConfigProperties config = getConfig();

    Resource resource =
        ResourceConfiguration.configureResource(config, serviceClassLoader, resourceCustomizer);

    // Track any closeable resources created throughout configuration. If an exception short
    // circuits configuration, partially configured components will be closed.
    List<Closeable> closeables = new ArrayList<>();

    try {
      OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder().build();
      boolean sdkEnabled = !config.getBoolean("otel.sdk.disabled", false);

      if (sdkEnabled) {
        SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder();
        meterProviderBuilder.setResource(resource);
        MeterProviderConfiguration.configureMeterProvider(
            meterProviderBuilder, config, serviceClassLoader, metricExporterCustomizer, closeables);
        meterProviderBuilder = meterProviderCustomizer.apply(meterProviderBuilder, config);
        SdkMeterProvider meterProvider = meterProviderBuilder.build();
        closeables.add(meterProvider);

        SdkTracerProviderBuilder tracerProviderBuilder = SdkTracerProvider.builder();
        tracerProviderBuilder.setResource(resource);
        TracerProviderConfiguration.configureTracerProvider(
            tracerProviderBuilder,
            config,
            serviceClassLoader,
            meterProvider,
            spanExporterCustomizer,
            samplerCustomizer,
            closeables);
        tracerProviderBuilder = tracerProviderCustomizer.apply(tracerProviderBuilder, config);
        SdkTracerProvider tracerProvider = tracerProviderBuilder.build();
        closeables.add(tracerProvider);

        SdkLoggerProviderBuilder loggerProviderBuilder = SdkLoggerProvider.builder();
        loggerProviderBuilder.setResource(resource);
        LoggerProviderConfiguration.configureLoggerProvider(
            loggerProviderBuilder,
            config,
            serviceClassLoader,
            meterProvider,
            logRecordExporterCustomizer,
            closeables);
        loggerProviderBuilder = loggerProviderCustomizer.apply(loggerProviderBuilder, config);
        SdkLoggerProvider loggerProvider = loggerProviderBuilder.build();
        closeables.add(loggerProvider);

        ContextPropagators propagators =
            PropagatorConfiguration.configurePropagators(
                config, serviceClassLoader, propagatorCustomizer);

        OpenTelemetrySdkBuilder sdkBuilder =
            OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setLoggerProvider(loggerProvider)
                .setMeterProvider(meterProvider)
                .setPropagators(propagators);

        openTelemetrySdk = sdkBuilder.build();
      }

      // NOTE: Shutdown hook registration is untested. Modify with caution.
      if (registerShutdownHook) {
        Runtime.getRuntime().addShutdownHook(shutdownHook(openTelemetrySdk));
      }

      if (setResultAsGlobal) {
        GlobalOpenTelemetry.set(openTelemetrySdk);
        GlobalLoggerProvider.set(openTelemetrySdk.getSdkLoggerProvider());
        GlobalEventEmitterProvider.set(
            SdkEventEmitterProvider.create(openTelemetrySdk.getSdkLoggerProvider()));
        logger.log(
            Level.FINE, "Global OpenTelemetry set to {0} by autoconfiguration", openTelemetrySdk);
      }

      return AutoConfiguredOpenTelemetrySdk.create(openTelemetrySdk, resource, config);
    } catch (RuntimeException e) {
      logger.info(
          "Error encountered during autoconfiguration. Closing partially configured components.");
      for (Closeable closeable : closeables) {
        try {
          logger.fine("Closing " + closeable.getClass().getName());
          closeable.close();
        } catch (IOException ex) {
          logger.warning(
              "Error closing " + closeable.getClass().getName() + ": " + ex.getMessage());
        }
      }
      if (e instanceof ConfigurationException) {
        throw e;
      }
      throw new ConfigurationException("Unexpected configuration error", e);
    }
  }

  @SuppressWarnings("deprecation") // Support deprecated SdkTracerProviderConfigurer
  private void mergeSdkTracerProviderConfigurer() {
    for (io.opentelemetry.sdk.autoconfigure.spi.traces.SdkTracerProviderConfigurer configurer :
        ServiceLoader.load(
            io.opentelemetry.sdk.autoconfigure.spi.traces.SdkTracerProviderConfigurer.class,
            serviceClassLoader)) {
      addTracerProviderCustomizer(
          (builder, config) -> {
            configurer.configure(builder, config);
            return builder;
          });
    }
  }

  private ConfigProperties getConfig() {
    ConfigProperties config = this.config;
    if (config == null) {
      config = computeConfigProperties();
    }
    return config;
  }

  private ConfigProperties computeConfigProperties() {
    DefaultConfigProperties properties = DefaultConfigProperties.create(propertiesSupplier.get());
    for (Function<ConfigProperties, Map<String, String>> customizer : propertiesCustomizers) {
      Map<String, String> overrides = customizer.apply(properties);
      properties = properties.withOverrides(overrides);
    }
    return properties;
  }

  // Visible for testing
  Thread shutdownHook(OpenTelemetrySdk sdk) {
    return new Thread(sdk::close);
  }

  private static <I, O1, O2> BiFunction<I, ConfigProperties, O2> mergeCustomizer(
      BiFunction<? super I, ConfigProperties, ? extends O1> first,
      BiFunction<? super O1, ConfigProperties, ? extends O2> second) {
    return (I configured, ConfigProperties config) -> {
      O1 firstResult = first.apply(configured, config);
      return second.apply(firstResult, config);
    };
  }

  private static Supplier<Map<String, String>> mergeProperties(
      Supplier<Map<String, String>> first, Supplier<Map<String, String>> second) {
    return () -> {
      Map<String, String> merged = new HashMap<>();
      merged.putAll(first.get());
      merged.putAll(second.get());
      return merged;
    };
  }
}
