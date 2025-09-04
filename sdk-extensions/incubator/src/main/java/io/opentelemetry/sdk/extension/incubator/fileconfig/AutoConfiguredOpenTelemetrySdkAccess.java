/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.resources.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

class AutoConfiguredOpenTelemetrySdkAccess {

  private AutoConfiguredOpenTelemetrySdkAccess() {}

  static AutoConfiguredOpenTelemetrySdk create(
      OpenTelemetrySdk sdk, Resource resource, SdkConfigProvider provider) {
    return createWithFactory(
        () -> {
          Method method =
              Class.forName(AutoConfiguredOpenTelemetrySdk.class.getName())
                  .getDeclaredMethod(
                      "create",
                      OpenTelemetrySdk.class,
                      Resource.class,
                      ConfigProperties.class,
                      Object.class);
          method.setAccessible(true);
          return (AutoConfiguredOpenTelemetrySdk)
              method.invoke(null, sdk, resource, null, provider);
        });
  }

  // Visible for testing
  interface Factory {
    AutoConfiguredOpenTelemetrySdk create()
        throws ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException;
  }

  // Visible for testing
  static AutoConfiguredOpenTelemetrySdk createWithFactory(Factory factory) {
    try {
      return factory.create();
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
    }
  }

  private static ConfigurationException toConfigurationException(
      DeclarativeConfigException exception) {
    String message = Objects.requireNonNull(exception.getMessage());
    return new ConfigurationException(message, exception);
  }
}
