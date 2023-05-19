/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class AutoConfigureUtil {

  private AutoConfigureUtil() {}

  /** Returns the {@link Resource} that was auto-configured. */
  public static Resource getResource(
      AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk) {
    try {
      Method method = AutoConfiguredOpenTelemetrySdk.class.getDeclaredMethod("getResource");
      method.setAccessible(true);
      return (Resource) method.invoke(autoConfiguredOpenTelemetrySdk);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalStateException(
          "Error calling getResource on AutoConfiguredOpenTelemetrySdk", e);
    }
  }

  /** Returns the {@link ConfigProperties} used for auto-configuration. */
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
}
