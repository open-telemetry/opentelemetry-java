/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.builder.internal;

import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import java.lang.reflect.Method;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class OpenTelemetrySdkBuilderUtil {

  private OpenTelemetrySdkBuilderUtil() {}

  public static OpenTelemetrySdkBuilder setSdkConfigProvider(
      OpenTelemetrySdkBuilder builder, Object sdkConfigProvider) {
    try {
      Method method =
          OpenTelemetrySdkBuilder.class.getDeclaredMethod("setSdkConfigProvider", Object.class);
      method.setAccessible(true);
      method.invoke(builder, sdkConfigProvider);
      return builder;
    } catch (NoSuchMethodException
        | IllegalAccessException
        | java.lang.reflect.InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setSdkConfigProvider on OpenTelemetrySdkBuilder", e);
    }
  }
}
