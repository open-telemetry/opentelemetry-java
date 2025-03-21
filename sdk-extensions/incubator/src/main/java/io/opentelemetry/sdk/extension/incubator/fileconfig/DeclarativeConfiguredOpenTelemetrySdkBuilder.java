/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.incubator.config.GlobalConfigProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.internal.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
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

  private static final Logger logger =
      Logger.getLogger(DeclarativeConfiguredOpenTelemetrySdkBuilder.class.getName());

  private static final String SUPPORTED_CONFIG_FILE_FORMAT = "0.3";
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

  private BiFunction<? super Resource, DeclarativeConfigProperties, ? extends Resource>
      resourceCustomizer = (a, unused) -> a;
  private BiFunction<
          ? super TextMapPropagator, DeclarativeConfigProperties, ? extends TextMapPropagator>
      propagatorCustomizer = (a, unused) -> a;
  private BiFunction<SdkMeterProviderBuilder, DeclarativeConfigProperties, SdkMeterProviderBuilder>
      meterProviderCustomizer = (a, unused) -> a;
  private BiFunction<
          SdkTracerProviderBuilder, DeclarativeConfigProperties, SdkTracerProviderBuilder>
      tracerProviderCustomizer = (a, unused) -> a;
  private BiFunction<
          SdkLoggerProviderBuilder, DeclarativeConfigProperties, SdkLoggerProviderBuilder>
      loggerProviderCustomizer = (a, unused) -> a;

  @Nullable private String configurationFilePath;
  private boolean registerShutdownHook;
  private boolean setResultAsGlobal = false;

  DeclarativeConfiguredOpenTelemetrySdkBuilder() {}

  @Override
  public DeclarativeConfiguredOpenTelemetrySdkBuilder addResourceCustomizer(
      BiFunction<? super Resource, DeclarativeConfigProperties, ? extends Resource>
          resourceCustomizer) {
    requireNonNull(resourceCustomizer);
    this.resourceCustomizer = mergeCustomizer(this.resourceCustomizer, resourceCustomizer);
    return this;
  }

  @Override
  public DeclarativeConfiguredOpenTelemetrySdkBuilder addPropagatorCustomizer(
      BiFunction<
              ? super TextMapPropagator, DeclarativeConfigProperties, ? extends TextMapPropagator>
          propagatorCustomizer) {
    requireNonNull(propagatorCustomizer, "propagatorCustomizer");
    this.propagatorCustomizer = mergeCustomizer(this.propagatorCustomizer, propagatorCustomizer);
    return this;
  }

  @Override
  public DeclarativeConfiguredOpenTelemetrySdkBuilder addMeterProviderCustomizer(
      BiFunction<
              ? super SdkMeterProviderBuilder,
              DeclarativeConfigProperties,
              ? extends SdkMeterProviderBuilder>
          meterProviderCustomizer) {
    requireNonNull(propagatorCustomizer, "meterProviderCustomizer");
    this.meterProviderCustomizer =
        mergeCustomizer(this.meterProviderCustomizer, meterProviderCustomizer);
    return this;
  }

  @Override
  public DeclarativeConfiguredOpenTelemetrySdkBuilder addTraceProviderCustomizer(
      BiFunction<
              ? super SdkTracerProviderBuilder,
              DeclarativeConfigProperties,
              ? extends SdkTracerProviderBuilder>
          traceProviderCustomizer) {
    requireNonNull(traceProviderCustomizer);
    this.tracerProviderCustomizer =
        mergeCustomizer(this.tracerProviderCustomizer, traceProviderCustomizer);
    return this;
  }

  @Override
  public DeclarativeConfiguredOpenTelemetrySdkBuilder addLoggerProviderCustomizer(
      BiFunction<
              ? super SdkLoggerProviderBuilder,
              DeclarativeConfigProperties,
              ? extends SdkLoggerProviderBuilder>
          loggerProviderCustomizer) {
    requireNonNull(loggerProviderCustomizer, "loggerProviderCustomizer");
    this.loggerProviderCustomizer =
        mergeCustomizer(this.loggerProviderCustomizer, loggerProviderCustomizer);
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
      throw new DeclarativeConfigException("Configuration file path must be set!");
    }
    InputStream is = maybeExtractConfigurationFileInputStream(configurationFilePath);
    OpenTelemetryConfigurationModel model = DeclarativeConfiguration.parse(is);
    if (!SUPPORTED_CONFIG_FILE_FORMAT.equals(model.getFileFormat())) {
      throw new DeclarativeConfigException(
          "Unsupported file format. Supported formats include: " + SUPPORTED_CONFIG_FILE_FORMAT);
    }

    OpenTelemetrySdkBuilder builder = OpenTelemetrySdk.builder();
    if (Objects.equals(Boolean.TRUE, model.getDisabled())) {
      return DeclarativeConfiguredOpenTelemetrySdk.create(builder.build());
    }

    List<Closeable> closeables = new ArrayList<>();
    try {
      DeclarativeConfigProperties properties = DeclarativeConfiguration.toConfigProperties(is);
      Resource resource = createResource(model, closeables, properties);
      maybeSetPropagators(builder, model, properties, closeables);
      maybeSetMeterProvider(builder, model, properties, resource, closeables);
      maybeSetTraceProvider(builder, model, properties, resource, closeables);
      maybeSetLoggerProvider(builder, model, properties, resource, closeables);
      OpenTelemetrySdk sdk = builder.build();

      maybeRegisterShutdownHook(sdk);
      maybeSetAsGlobal(sdk, model);
      // TODO: callDeclarativeConfigureListeners??

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

      if (e instanceof DeclarativeConfigException) {
        throw e;
      }
      throw new DeclarativeConfigException(
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

  private Resource createResource(
      OpenTelemetryConfigurationModel model,
      List<Closeable> closeables,
      DeclarativeConfigProperties properties) {
    Resource resource = Resource.getDefault();
    if (model.getResource() != null) {
      resource = ResourceFactory.getInstance().create(model.getResource(), SPI_HELPER, closeables);
    }
    resource = resourceCustomizer.apply(resource, properties);
    return resource;
  }

  private void maybeSetPropagators(
      OpenTelemetrySdkBuilder builder,
      OpenTelemetryConfigurationModel model,
      DeclarativeConfigProperties properties,
      List<Closeable> closeables) {
    if (model.getPropagator() == null) {
      return;
    }
    if (model.getPropagator().getComposite() == null) {
      throw new DeclarativeConfigException("composite propagator is required but it is null!");
    }

    TextMapPropagator textMapPropagator =
        TextMapPropagatorFactory.getInstance()
            .create(model.getPropagator().getComposite(), SPI_HELPER, closeables);
    textMapPropagator = propagatorCustomizer.apply(textMapPropagator, properties);
    ContextPropagators propagators = ContextPropagators.create(textMapPropagator);

    builder.setPropagators(propagators);
  }

  private void maybeSetMeterProvider(
      OpenTelemetrySdkBuilder builder,
      OpenTelemetryConfigurationModel model,
      DeclarativeConfigProperties properties,
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

  private void maybeSetTraceProvider(
      OpenTelemetrySdkBuilder builder,
      OpenTelemetryConfigurationModel model,
      DeclarativeConfigProperties properties,
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

  private void maybeSetLoggerProvider(
      OpenTelemetrySdkBuilder builder,
      OpenTelemetryConfigurationModel model,
      DeclarativeConfigProperties properties,
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

  private void maybeRegisterShutdownHook(OpenTelemetrySdk sdk) {
    if (!registerShutdownHook) {
      return;
    }
    Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));
  }

  private void maybeSetAsGlobal(OpenTelemetrySdk sdk, OpenTelemetryConfigurationModel model) {
    if (!setResultAsGlobal) {
      return;
    }

    GlobalOpenTelemetry.set(sdk);
    logger.log(Level.FINE, "Global OpenTelemetry set to {0} by declarative configuration", sdk);

    if (INCUBATOR_AVAILABLE) {
      GlobalConfigProvider.set(SdkConfigProvider.create(model));
    }
  }

  private static InputStream maybeExtractConfigurationFileInputStream(
      String configurationFilePath) {
    try {
      return Files.newInputStream(Paths.get(configurationFilePath));
    } catch (IOException e) {
      throw new DeclarativeConfigException("Unable to extract configuration input stream!", e);
    }
  }

  private static <I, O1, O2> BiFunction<I, DeclarativeConfigProperties, O2> mergeCustomizer(
      BiFunction<? super I, DeclarativeConfigProperties, ? extends O1> first,
      BiFunction<? super O1, DeclarativeConfigProperties, ? extends O2> second) {
    return (I configured, DeclarativeConfigProperties config) -> {
      O1 firstResult = first.apply(configured, config);
      return second.apply(firstResult, config);
    };
  }
}
