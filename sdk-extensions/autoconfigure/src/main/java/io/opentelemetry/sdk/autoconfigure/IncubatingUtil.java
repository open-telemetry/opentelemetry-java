/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.GlobalConfigProvider;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.resources.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

  // Visible for testing
  interface Factory {
    @Nullable
    AutoConfiguredOpenTelemetrySdk create()
        throws ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException;
  }

  static AutoConfiguredOpenTelemetrySdk configureFromFile(
      Logger logger, String configurationFile, ComponentLoader componentLoader) {
    logger.fine("Autoconfiguring from configuration file: " + configurationFile);
    try (FileInputStream fis = new FileInputStream(configurationFile)) {
      return requireNonNull(
          createWithFactory(
              "file",
              () ->
                  getOpenTelemetrySdk(
                      Class.forName(
                              "io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration")
                          .getMethod("parse", InputStream.class)
                          .invoke(null, fis),
                      componentLoader)));
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
    return createWithFactory(
        "SPI",
        () -> {
          Class<?> providerClass =
              Class.forName(
                  "io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfigurationProvider");
          Method getConfigurationModel = providerClass.getMethod("getConfigurationModel");

          for (Object configProvider : componentLoader.load(providerClass)) {
            Object model = getConfigurationModel.invoke(configProvider);
            if (model != null) {
              return getOpenTelemetrySdk(model, componentLoader);
            }
          }
          return null;
        });
  }

  private static AutoConfiguredOpenTelemetrySdk getOpenTelemetrySdk(
      Object model, ComponentLoader componentLoader)
      throws IllegalAccessException,
          InvocationTargetException,
          ClassNotFoundException,
          NoSuchMethodException {

    Class<?> openTelemetryConfiguration =
        Class.forName(
            "io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel");
    Class<?> declarativeConfiguration =
        Class.forName(
            "io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration");
    Method create =
        declarativeConfiguration.getMethod(
            "create", openTelemetryConfiguration, ComponentLoader.class);

    OpenTelemetrySdk sdk = (OpenTelemetrySdk) create.invoke(null, model, componentLoader);
    Class<?> sdkConfigProvider =
        Class.forName("io.opentelemetry.sdk.extension.incubator.fileconfig.SdkConfigProvider");
    Method createFileConfigProvider =
        sdkConfigProvider.getMethod("create", openTelemetryConfiguration, ComponentLoader.class);
    ConfigProvider configProvider =
        (ConfigProvider) createFileConfigProvider.invoke(null, model, componentLoader);
    // Note: can't access file configuration resource without reflection so setting a dummy
    // resource
    return AutoConfiguredOpenTelemetrySdk.create(sdk, Resource.getDefault(), null, configProvider);
  }

  // Visible for testing
  @Nullable
  static AutoConfiguredOpenTelemetrySdk createWithFactory(String name, Factory factory) {
    try {
      return factory.create();
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
      throw new ConfigurationException(
          String.format(
              "Error configuring from %s. Is opentelemetry-sdk-extension-incubator on the classpath?",
              name),
          e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof DeclarativeConfigException) {
        throw toConfigurationException((DeclarativeConfigException) cause);
      }
      throw new ConfigurationException("Unexpected error configuring from " + name, e);
    }
  }

  private static ConfigurationException toConfigurationException(
      DeclarativeConfigException exception) {
    String message = requireNonNull(exception.getMessage());
    return new ConfigurationException(message, exception);
  }

  static void setGlobalConfigProvider(Object configProvider) {
    GlobalConfigProvider.set((ConfigProvider) configProvider);
  }
}
