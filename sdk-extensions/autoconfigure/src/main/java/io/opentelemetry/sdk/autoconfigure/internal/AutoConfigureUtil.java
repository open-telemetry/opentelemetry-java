/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class AutoConfigureUtil {

  private AutoConfigureUtil() {}

  /**
   * Returns the {@link ConfigProperties} used for auto-configuration.
   *
   * @return the config properties, or {@code null} if declarative configuration is used
   */
  @Nullable
  public static ConfigProperties getConfig(
      AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk) {
    try {
      Method method = AutoConfiguredOpenTelemetrySdk.class.getDeclaredMethod("getConfig");
      method.setAccessible(true);
      return (ConfigProperties) method.invoke(autoConfiguredOpenTelemetrySdk);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalStateException(
          "Error calling getConfig on AutoConfiguredOpenTelemetrySdk", e);
    }
  }

  /** Sets the {@link ConfigProperties} customizer to be used in the auto-configuration process. */
  public static AutoConfiguredOpenTelemetrySdkBuilder setConfigPropertiesCustomizer(
      AutoConfiguredOpenTelemetrySdkBuilder builder,
      Function<ConfigProperties, ConfigProperties> customizer) {
    try {
      Method method =
          AutoConfiguredOpenTelemetrySdkBuilder.class.getDeclaredMethod(
              "setConfigPropertiesCustomizer", Function.class);
      method.setAccessible(true);
      method.invoke(builder, customizer);
      return builder;
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalStateException(
          "Error calling setConfigPropertiesCustomizer on AutoConfiguredOpenTelemetrySdkBuilder",
          e);
    }
  }
}
