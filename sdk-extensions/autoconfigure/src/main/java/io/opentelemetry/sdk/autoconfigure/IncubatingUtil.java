/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.GlobalConfigProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.resources.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Logger;

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
      Class<?> declarativeConfiguration =
          Class.forName(
              "io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration");
      Method parse = declarativeConfiguration.getMethod("parse", InputStream.class);
      Object model = parse.invoke(null, fis);
      Class<?> openTelemetryConfiguration =
          Class.forName(
              "io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel");
      Method create =
          declarativeConfiguration.getMethod(
              "create", openTelemetryConfiguration, ComponentLoader.class);
      OpenTelemetrySdk sdk = (OpenTelemetrySdk) create.invoke(null, model, componentLoader);

      Class<?> sdkConfigProvider =
          Class.forName("io.opentelemetry.sdk.extension.incubator.fileconfig.SdkConfigProvider");
      Method createFileConfigProvider =
          sdkConfigProvider.getMethod("create", openTelemetryConfiguration);
      ConfigProvider configProvider = (ConfigProvider) createFileConfigProvider.invoke(null, model);

      Resource configuredResource = createResourceFromModel(model, componentLoader);

      return AutoConfiguredOpenTelemetrySdk.create(sdk, configuredResource, null, configProvider);
    } catch (FileNotFoundException e) {
      throw new ConfigurationException("Configuration file not found", e);
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
      throw new ConfigurationException(
          "Error configuring from file. Is opentelemetry-sdk-extension-incubator on the classpath?",
          e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof DeclarativeConfigException) {
        throw toConfigurationException((DeclarativeConfigException) cause);
      }
      throw new ConfigurationException("Unexpected error configuring from file", e);
    } catch (IOException e) {
      // IOException (other than FileNotFoundException which is caught above) is only thrown
      // above by FileInputStream.close()
      throw new ConfigurationException("Error closing file", e);
    }
  }

  private static Resource createResourceFromModel(
      Object openTelemetryConfigurationModel, ComponentLoader componentLoader)
      throws NoSuchMethodException,
          InvocationTargetException,
          IllegalAccessException,
          ClassNotFoundException {
    Class<?> declarativeConfigurationClass =
        Class.forName(
            "io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration");
    Class<?> configurationModelClass =
        Class.forName(
            "io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel");

    Method createResource =
        declarativeConfigurationClass.getMethod(
            "createResource", configurationModelClass, ComponentLoader.class);
    return (Resource) createResource.invoke(null, openTelemetryConfigurationModel, componentLoader);
  }

  private static ConfigurationException toConfigurationException(
      DeclarativeConfigException exception) {
    String message = Objects.requireNonNull(exception.getMessage());
    return new ConfigurationException(message, exception);
  }

  static void setGlobalConfigProvider(Object configProvider) {
    GlobalConfigProvider.set((ConfigProvider) configProvider);
  }
}
