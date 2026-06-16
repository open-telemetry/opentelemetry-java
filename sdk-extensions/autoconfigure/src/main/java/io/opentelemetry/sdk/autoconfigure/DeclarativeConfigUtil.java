/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.DeclarativeConfigResult;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.DeclarativeConfiguration;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.DeclarativeConfigurationProvider;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Utilities for interacting with ({@code
 * io.opentelemetry:opentelemetry-sdk-extension-declarative-config}, which is not guaranteed to be
 * present on the classpath. For all methods, callers MUST first separately reflectively confirm
 * that declarative config is available on the classpath.
 */
final class DeclarativeConfigUtil {

  private DeclarativeConfigUtil() {}

  static AutoConfiguredOpenTelemetrySdk configureFromFile(
      Logger logger, String configurationFile, ComponentLoader componentLoader) {
    logger.fine("Autoconfiguring from configuration file: " + configurationFile);
    try (FileInputStream fis = new FileInputStream(configurationFile)) {
      OpenTelemetryConfigurationModel model = DeclarativeConfiguration.parse(fis);
      return create(model, componentLoader);
    } catch (DeclarativeConfigException e) {
      throw toConfigurationException(e);
    } catch (FileNotFoundException e) {
      throw new ConfigurationException("Configuration file not found", e);
    } catch (IOException e) {
      // IOException (other than FileNotFoundException which is caught above) is only thrown
      // above by FileInputStream.close()
      throw new ConfigurationException("Error closing file", e);
    }
  }

  @Nullable
  public static AutoConfiguredOpenTelemetrySdk configureFromSpi(ComponentLoader componentLoader) {
    for (DeclarativeConfigurationProvider provider :
        componentLoader.load(DeclarativeConfigurationProvider.class)) {
      OpenTelemetryConfigurationModel model = provider.getConfigurationModel();
      if (model != null) {
        return create(model, componentLoader);
      }
    }
    return null;
  }

  private static AutoConfiguredOpenTelemetrySdk create(
      OpenTelemetryConfigurationModel model, ComponentLoader componentLoader) {
    try {
      DeclarativeConfigResult result = DeclarativeConfiguration.create(model, componentLoader);
      return AutoConfiguredOpenTelemetrySdk.create(result.getSdk(), result.getResource(), null);
    } catch (DeclarativeConfigException e) {
      throw toConfigurationException(e);
    }
  }

  private static ConfigurationException toConfigurationException(
      DeclarativeConfigException exception) {
    String message = requireNonNull(exception.getMessage());
    return new ConfigurationException(message, exception);
  }
}
