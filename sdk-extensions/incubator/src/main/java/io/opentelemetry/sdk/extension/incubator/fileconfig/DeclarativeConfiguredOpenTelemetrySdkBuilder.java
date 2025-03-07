/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.incubator.events.GlobalEventLoggerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.internal.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class DeclarativeConfiguredOpenTelemetrySdkBuilder
    implements DeclarativeConfigurationCustomizer {

  private static final Logger logger = Logger.getLogger(FileConfiguration.class.getName());

  private static final boolean INCUBATOR_AVAILABLE;
  private static final ComponentLoader DEFAULT_COMPONENT_LOADER =
      SpiHelper.serviceComponentLoader(
          DeclarativeConfiguredOpenTelemetrySdkBuilder.class.getClassLoader());
  private static final SpiHelper SPI_HELPER = SpiHelper.create(DEFAULT_COMPONENT_LOADER);

  static {
    boolean incubatorAvailable = false;

    try {
      Class.forName("io.opentelemetry.api.incubator.events.GlobalEventLoggerProvider");
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }

    INCUBATOR_AVAILABLE = incubatorAvailable;
  }

  private BiFunction<SdkTracerProviderBuilder, StructuredConfigProperties, SdkTracerProviderBuilder>
      tracerProviderCustomizer = (a, unused) -> a;
  private BiFunction<SdkMeterProviderBuilder, StructuredConfigProperties, SdkMeterProviderBuilder>
      meterProviderCustomizer = (a, unused) -> a;
  private BiFunction<SdkLoggerProviderBuilder, StructuredConfigProperties, SdkLoggerProviderBuilder>
      loggerProviderCustomizer = (a, unused) -> a;
  private BiFunction<
          ? super TextMapPropagator, StructuredConfigProperties, ? extends TextMapPropagator>
      propagatorCustomizer = (a, unused) -> a;

  @Nullable private String configurationFilePath;
  private boolean registerShutdownHook;
  private boolean setResultAsGlobal = false;

  DeclarativeConfiguredOpenTelemetrySdkBuilder() {}

  @Override
  public DeclarativeConfigurationCustomizer addTraceProviderCustomizer(
      BiFunction<
              ? super SdkTracerProviderBuilder,
              StructuredConfigProperties,
              ? extends SdkTracerProviderBuilder>
          traceProviderCustomizer) {
    requireNonNull(traceProviderCustomizer);
    this.tracerProviderCustomizer =
        mergeCustomizer(this.tracerProviderCustomizer, traceProviderCustomizer);
    return this;
  }

  @Override
  public DeclarativeConfigurationCustomizer addMeterProviderCustomizer(
      BiFunction<
              ? super SdkMeterProviderBuilder,
              StructuredConfigProperties,
              ? extends SdkMeterProviderBuilder>
          meterProviderCustomizer) {
    requireNonNull(propagatorCustomizer, "meterProviderCustomizer");
    this.meterProviderCustomizer =
        mergeCustomizer(this.meterProviderCustomizer, meterProviderCustomizer);
    return this;
  }

  @Override
  public DeclarativeConfigurationCustomizer addLoggerProviderCustomizer(
      BiFunction<
              ? super SdkLoggerProviderBuilder,
              StructuredConfigProperties,
              ? extends SdkLoggerProviderBuilder>
          loggerProviderCustomizer) {
    requireNonNull(loggerProviderCustomizer, "loggerProviderCustomizer");
    this.loggerProviderCustomizer =
        mergeCustomizer(this.loggerProviderCustomizer, loggerProviderCustomizer);
    return this;
  }

  @Override
  public DeclarativeConfigurationCustomizer addPropagatorCustomizer(
      BiFunction<? super TextMapPropagator, StructuredConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer) {
    requireNonNull(propagatorCustomizer, "propagatorCustomizer");
    this.propagatorCustomizer = mergeCustomizer(this.propagatorCustomizer, propagatorCustomizer);
    return this;
  }

  public DeclarativeConfiguredOpenTelemetrySdkBuilder setConfigurationFilePath(
      String configurationFilePath) {
    requireNonNull(configurationFilePath, "configurationFilePath");
    this.configurationFilePath = configurationFilePath;
    return this;
  }

  /**
   * Disable the registration of a shutdown hook to shut down the SDK when appropriate. By default,
   * the shutdown hook is registered.
   *
   * <p>Skipping the registration of the shutdown hook may cause unexpected behavior. This
   * configuration is for SDK consumers that require control over the SDK lifecycle. In this case,
   * alternatives must be provided by the SDK consumer to shut down the SDK.
   */
  public DeclarativeConfiguredOpenTelemetrySdkBuilder disableShutdownHook() {
    this.registerShutdownHook = false;
    return this;
  }

  /**
   * Sets whether the configured {@link OpenTelemetrySdk} should be set as the application's
   * {@linkplain io.opentelemetry.api.GlobalOpenTelemetry global} instance.
   *
   * <p>By default, {@link GlobalOpenTelemetry} is not set.
   */
  public DeclarativeConfiguredOpenTelemetrySdkBuilder setResultAsGlobal() {
    this.setResultAsGlobal = true;
    return this;
  }

  /**
   * Returns a new {@link DeclarativeConfiguredOpenTelemetrySdk} holding components declaratively
   * configured using the settings of this {@link DeclarativeConfiguredOpenTelemetrySdk}.
   */
  public DeclarativeConfiguredOpenTelemetrySdk build() {
    String configurationFilePath = maybeExtractConfigurationFilePath();
    if (configurationFilePath == null) {
      throw new ConfigurationException("Configuration file path must be set!");
    }
    InputStream is = maybeExtractConfigurationFileInputStream(configurationFilePath);
    OpenTelemetryConfigurationModel model = FileConfiguration.parse(is);
    if (!"0.3".equals(model.getFileFormat())) {
      throw new ConfigurationException("Unsupported file format. Supported formats include: 0.3");
    }

    OpenTelemetrySdkBuilder builder = OpenTelemetrySdk.builder();
    if (Objects.equals(Boolean.TRUE, model.getDisabled())) {
      return DeclarativeConfiguredOpenTelemetrySdk.create(builder.build());
    }

    List<Closeable> closeables = new ArrayList<>();
    try {
      StructuredConfigProperties properties = FileConfiguration.toConfigProperties(is);
      Resource resource = createResource(model, closeables);
      maybeSetTraceProvider(builder, model, properties, resource, closeables);
      maybeSetMeterProvider(builder, model, properties, resource, closeables);
      maybeSetLoggerProvider(builder, model, properties, resource, closeables);
      maybeSetPropagators(builder, model, properties, closeables);
      OpenTelemetrySdk sdk = builder.build();

      maybeRegisterShutdownHook(sdk);
      maybeSetAsGlobal(sdk);
      // TODO: callDeclarativeConfigureListeners

      return DeclarativeConfiguredOpenTelemetrySdk.create(sdk);
    } catch (RuntimeException e) {
      logger.info(
          "Exception occured during interpreting the model. Closing partially configured components.");
      for (Closeable closeable : closeables) {
        try {
          logger.fine("Closing " + closeable.getClass().getName());
          closeable.close();
        } catch (IOException ex) {
          logger.warning(
              "Exception  during closing "
                  + closeable.getClass().getName()
                  + ": "
                  + ex.getMessage());
        }
      }

      if (e instanceof ConfigurationException) {
        throw e;
      }
      throw new ConfigurationException(
          "Unexpected exception during declarative OpenTelemetry SDK build!", e);
    }
  }

  private String maybeExtractConfigurationFilePath() {
    String configurationFilePath = this.configurationFilePath;
    if (configurationFilePath == null) {
      configurationFilePath = System.getProperty("otel.experimental.config.file");
    }
    if (configurationFilePath == null) {
      configurationFilePath = System.getenv("OTEL_EXPERIMENTAL_CONFIG_FILE");
    }
    return configurationFilePath;
  }

  private void maybeSetTraceProvider(
      OpenTelemetrySdkBuilder builder,
      OpenTelemetryConfigurationModel model,
      StructuredConfigProperties properties,
      Resource resource,
      List<Closeable> closeables) {
    if (model.getTracerProvider() == null) {
      return;
    }

    SdkTracerProviderBuilder tracerProviderBuilder =
        TracerProviderFactory.getInstance()
            .create(
                TracerProviderAndAttributeLimits.create(
                    model.getAttributeLimits(), model.getTracerProvider()),
                SPI_HELPER,
                closeables)
            .setResource(resource);
    tracerProviderBuilder = tracerProviderCustomizer.apply(tracerProviderBuilder, properties);
    SdkTracerProvider tracerProvider = tracerProviderBuilder.build();

    closeables.add(tracerProvider);
    builder.setTracerProvider(tracerProvider);
  }

  private void maybeSetMeterProvider(
      OpenTelemetrySdkBuilder builder,
      OpenTelemetryConfigurationModel model,
      StructuredConfigProperties properties,
      Resource resource,
      List<Closeable> closeables) {
    if (model.getMeterProvider() == null) {
      return;
    }

    SdkMeterProviderBuilder meterProviderBuilder =
        MeterProviderFactory.getInstance()
            .create(model.getMeterProvider(), SPI_HELPER, closeables)
            .setResource(resource);
    meterProviderBuilder = meterProviderCustomizer.apply(meterProviderBuilder, properties);
    SdkMeterProvider meterProvider = meterProviderBuilder.build();

    closeables.add(meterProvider);
    builder.setMeterProvider(meterProvider);
  }

  private void maybeSetLoggerProvider(
      OpenTelemetrySdkBuilder builder,
      OpenTelemetryConfigurationModel model,
      StructuredConfigProperties properties,
      Resource resource,
      List<Closeable> closeables) {
    if (model.getLoggerProvider() == null) {
      return;
    }

    SdkLoggerProviderBuilder loggerProviderBuilder =
        LoggerProviderFactory.getInstance()
            .create(
                LoggerProviderAndAttributeLimits.create(
                    model.getAttributeLimits(), model.getLoggerProvider()),
                SPI_HELPER,
                closeables)
            .setResource(resource);
    loggerProviderBuilder = loggerProviderCustomizer.apply(loggerProviderBuilder, properties);
    SdkLoggerProvider loggerProvider = loggerProviderBuilder.build();

    closeables.add(loggerProvider);
    builder.setLoggerProvider(loggerProvider);
  }

  private void maybeSetPropagators(
      OpenTelemetrySdkBuilder builder,
      OpenTelemetryConfigurationModel model,
      StructuredConfigProperties properties,
      List<Closeable> closeables) {
    if (model.getPropagator() == null) {
      return;
    }
    if (model.getPropagator().getComposite() == null) {
      throw new ConfigurationException("composite propagator is required but it is null!");
    }

    TextMapPropagator textMapPropagator =
        TextMapPropagatorFactory.getInstance()
            .create(model.getPropagator().getComposite(), SPI_HELPER, closeables);
    textMapPropagator = propagatorCustomizer.apply(textMapPropagator, properties);
    ContextPropagators propagators = ContextPropagators.create(textMapPropagator);

    builder.setPropagators(propagators);
  }

  private void maybeRegisterShutdownHook(OpenTelemetrySdk sdk) {
    if (!registerShutdownHook) {
      return;
    }
    Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));
  }

  private void maybeSetAsGlobal(OpenTelemetrySdk sdk) {
    if (!setResultAsGlobal) {
      return;
    }

    GlobalOpenTelemetry.set(sdk);
    logger.log(Level.FINE, "Global OpenTelemetry set to {0} by declarative configuration", sdk);

    if (INCUBATOR_AVAILABLE) {
      GlobalEventLoggerProvider.set(SdkEventLoggerProvider.create(sdk.getSdkLoggerProvider()));
    }
  }

  private static InputStream maybeExtractConfigurationFileInputStream(
      String configurationFilePath) {
    try {
      return Files.newInputStream(Paths.get(configurationFilePath));
    } catch (IOException e) {
      throw new ConfigurationException("Unable to extract configuration input stream!", e);
    }
  }

  private static Resource createResource(
      OpenTelemetryConfigurationModel model, List<Closeable> closeables) {
    if (model.getResource() != null) {
      return ResourceFactory.getInstance().create(model.getResource(), SPI_HELPER, closeables);
    } else {
      return Resource.getDefault();
    }
  }

  private static <I, O1, O2> BiFunction<I, StructuredConfigProperties, O2> mergeCustomizer(
      BiFunction<? super I, StructuredConfigProperties, ? extends O1> first,
      BiFunction<? super O1, StructuredConfigProperties, ? extends O2> second) {
    return (I configured, StructuredConfigProperties config) -> {
      O1 firstResult = first.apply(configured, config);
      return second.apply(firstResult, config);
    };
  }
}
