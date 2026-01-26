/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utilities for interacting with incubating components ({@code
 * io.opentelemetry:opentelemetry-api-incubator} and {@code
 * io.opentelemetry:opentelemetry-sdk-extension-incubator}), which are not guaranteed to be present
 * on the classpath. For all methods, callers MUST first separately reflectively confirm that the
 * incubator is available on the classpath.
 */
final class IncubatingUtil {

  private IncubatingUtil() {}

  static Object noopSdkConfigProvider() {
    try {
      Class<?> sdkConfigProviderClass =
          Class.forName("io.opentelemetry.sdk.extension.incubator.fileconfig.SdkConfigProvider");
      Method defaultProviderMethod = sdkConfigProviderClass.getMethod("noop");
      return defaultProviderMethod.invoke(null);
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new IllegalStateException(
          "Failed to create default SdkConfigProvider from incubator", e);
    }
  }

  static OpenTelemetrySdk createExtendedOpenTelemetrySdk(
      OpenTelemetrySdk openTelemetrySdk, Object sdkConfigProvider) {
    try {
      Class<?> extendedSdkClass =
          Class.forName("io.opentelemetry.sdk.extension.incubator.ExtendedOpenTelemetrySdk");
      Class<?> sdkConfigProviderClass =
          Class.forName("io.opentelemetry.sdk.extension.incubator.fileconfig.SdkConfigProvider");
      Method createMethod =
          extendedSdkClass.getMethod("create", OpenTelemetrySdk.class, sdkConfigProviderClass);
      return (OpenTelemetrySdk)
          createMethod.invoke(
              null, openTelemetrySdk, sdkConfigProviderClass.cast(sdkConfigProvider));
    } catch (ClassNotFoundException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new IllegalStateException(
          "Failed to create ExtendedOpenTelemetrySdk from incubator", e);
    }
  }
}
