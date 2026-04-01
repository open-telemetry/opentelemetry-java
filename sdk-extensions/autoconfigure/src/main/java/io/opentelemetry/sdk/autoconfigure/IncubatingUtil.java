/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfigurationProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.internal.ExtendedOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.resources.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Utilities for interacting with incubating components ({@code
 * io.opentelemetry:opentelemetry-api-incubator} and {@code
 * io.opentelemetry:opentelemetry-sdk-extension-incubator}), which are not guaranteed to be present
 * on the classpath. For all methods, callers MUST first separately reflectively confirm that the
 * incubator is available on the classpath.
 */
final class IncubatingUtil {

  private IncubatingUtil() {}

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
    ExtendedOpenTelemetrySdk sdk;
    try {
      sdk = DeclarativeConfiguration.create(model, componentLoader);
    } catch (DeclarativeConfigException e) {
      throw toConfigurationException(e);
    }

    try {
      Field sharedState = SdkMeterProvider.class.getDeclaredField("sharedState");
      sharedState.setAccessible(true);
      Resource resource =
          ((MeterProviderSharedState) sharedState.get(sdk.getSdkMeterProvider())).getResource();

      return AutoConfiguredOpenTelemetrySdk.create(sdk, resource, null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new ConfigurationException("Error resolving resource from ExtendedOpenTelemetrySdk", e);
    }
  }

  private static ConfigurationException toConfigurationException(
      DeclarativeConfigException exception) {
    String message = requireNonNull(exception.getMessage());
    return new ConfigurationException(message, exception);
  }
}
