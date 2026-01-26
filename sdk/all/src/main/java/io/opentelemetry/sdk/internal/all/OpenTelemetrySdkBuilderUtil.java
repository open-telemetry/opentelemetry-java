/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal.all;

import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A collection of methods that allow use of experimental features prior to availability in public
 * APIs.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class OpenTelemetrySdkBuilderUtil {

  private OpenTelemetrySdkBuilderUtil() {}

  /** Reflectively set the {@link ScopeConfigurator} to the {@link SdkTracerProvider}. */
  public static OpenTelemetrySdkBuilder setConfigProvider(
      OpenTelemetrySdkBuilder builder, SdkConfigProvider configProvider) {
    try {
      Method method =
          OpenTelemetrySdkBuilder.class.getDeclaredMethod(
              "setConfigProvider", SdkConfigProvider.class);
      method.setAccessible(true);
      method.invoke(builder, configProvider);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setConfigProvider on OpenTelemetrySdkBuilder", e);
    }
    return builder;
  }
}
