/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.autoconfigure.spi.internal.AutoConfigureListener;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configure {@link OpenTelemetrySdk} using <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/configuration#declarative-configuration">declarative
 * configuration</a>.
 *
 * <p>This class handles SDK configuration from an already-parsed {@link
 * OpenTelemetryConfigurationModel}. It has no dependency on YAML parsing libraries at runtime.
 *
 * <p>For most users, calling {@link
 * DeclarativeConfigurationParser#parseAndCreate(java.io.InputStream)} with a <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/data-model.md#yaml-file-format">YAML
 * configuration file</a> is the simplest entry point.
 *
 * <p>Use this class directly when building the {@link OpenTelemetryConfigurationModel}
 * programmatically, without incurring any dependency on {@code snakeyaml-engine} or {@code
 * jackson-databind}.
 */
public final class DeclarativeConfiguration {

  private static final Logger logger = Logger.getLogger(DeclarativeConfiguration.class.getName());
  private static final ComponentLoader DEFAULT_COMPONENT_LOADER =
      ComponentLoader.forClassLoader(DeclarativeConfigProperties.class.getClassLoader());

  private DeclarativeConfiguration() {}

  /**
   * Interpret the {@code configurationModel} to create {@link OpenTelemetrySdk} instance
   * corresponding to the configuration.
   *
   * @param configurationModel the configuration model
   * @return the {@link DeclarativeConfigResult}
   * @throws DeclarativeConfigException if unable to interpret
   */
  public static DeclarativeConfigResult create(OpenTelemetryConfigurationModel configurationModel) {
    return create(configurationModel, DEFAULT_COMPONENT_LOADER);
  }

  /**
   * Interpret the {@code configurationModel} to create {@link OpenTelemetrySdk} instance
   * corresponding to the configuration.
   *
   * @param configurationModel the configuration model
   * @param componentLoader the component loader used to load {@link ComponentProvider}
   *     implementations
   * @return the {@link DeclarativeConfigResult}
   * @throws DeclarativeConfigException if unable to interpret
   */
  public static DeclarativeConfigResult create(
      OpenTelemetryConfigurationModel configurationModel, ComponentLoader componentLoader) {
    return create(configurationModel, new DeclarativeConfigContext(componentLoader));
  }

  private static DeclarativeConfigResult create(
      OpenTelemetryConfigurationModel configurationModel, DeclarativeConfigContext context) {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();
    context.setBuilder(builder);

    for (DeclarativeConfigurationCustomizerProvider provider :
        Ordered.loadOrderedList(context, DeclarativeConfigurationCustomizerProvider.class)) {
      provider.customize(builder);
    }

    DeclarativeConfigResult result =
        createAndMaybeCleanup(
            OpenTelemetryConfigurationFactory.getInstance(),
            context,
            builder.customizeModel(configurationModel));
    callAutoConfigureListeners(context, result.getSdk());
    return result;
  }

  static <M, R> R createAndMaybeCleanup(
      Factory<M, R> factory, DeclarativeConfigContext context, M model) {
    try {
      return factory.create(model, context);
    } catch (RuntimeException e) {
      logger.info("Error encountered interpreting model. Closing partially configured components.");
      for (Closeable closeable : context.getCloseables()) {
        try {
          logger.fine("Closing " + closeable.getClass().getName());
          closeable.close();
        } catch (IOException ex) {
          logger.warning(
              "Error closing " + closeable.getClass().getName() + ": " + ex.getMessage());
        }
      }
      if (e instanceof DeclarativeConfigException) {
        throw e;
      }
      throw new DeclarativeConfigException("Unexpected configuration error", e);
    }
  }

  // Visible for testing
  static void callAutoConfigureListeners(
      DeclarativeConfigContext context, OpenTelemetrySdk openTelemetrySdk) {
    for (AutoConfigureListener listener : context.getListeners()) {
      try {
        listener.afterAutoConfigure(openTelemetrySdk);
      } catch (Throwable throwable) {
        logger.log(
            Level.WARNING, "Error invoking listener " + listener.getClass().getName(), throwable);
      }
    }
  }
}
