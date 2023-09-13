/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Parses YAML configuration files conforming to the schema in <a
 * href="https://github.com/open-telemetry/opentelemetry-configuration">open-telemetry/opentelemetry-configuration</a>
 * to a {@link OpenTelemetryConfiguration} in-memory representation. Interprets the in-memory
 * representation to produce an {@link OpenTelemetrySdk}.
 *
 * @see #parseAndInterpret(InputStream)
 */
public final class ConfigurationFactory {

  private static final Logger logger = Logger.getLogger(ConfigurationFactory.class.getName());

  private ConfigurationFactory() {}

  /**
   * Parse the {@code inputStream} YAML to {@link OpenTelemetryConfiguration} and interpret the
   * model to create {@link OpenTelemetrySdk} instance corresponding to the configuration.
   *
   * @param inputStream the configuration YAML
   * @return the {@link OpenTelemetrySdk}
   */
  public static OpenTelemetrySdk parseAndInterpret(InputStream inputStream) {
    OpenTelemetryConfiguration model;
    try {
      model = ConfigurationReader.parse(inputStream);
    } catch (RuntimeException e) {
      throw new ConfigurationException("Unable to parse inputStream", e);
    }

    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetrySdk sdk;
    try {
      sdk =
          OpenTelemetryConfigurationFactory.getInstance()
              .create(
                  model, SpiHelper.create(ConfigurationFactory.class.getClassLoader()), closeables);
    } catch (RuntimeException e) {
      logger.info(
          "Error encountered interpreting configuration. Closing partially configured components.");
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

    // Register hook to shutdown SDK when runtime shuts down
    // TODO(jack-berg): Should we omit this and instead document that users should register their
    // own shutdown hook?
    Runtime.getRuntime().addShutdownHook(shutdownHook(sdk));
    return sdk;
  }

  // Visible for testing
  static Thread shutdownHook(OpenTelemetrySdk sdk) {
    return new Thread(sdk::close);
  }
}
