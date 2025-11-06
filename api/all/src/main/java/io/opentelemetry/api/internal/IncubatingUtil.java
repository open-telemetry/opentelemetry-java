/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import io.opentelemetry.api.OpenTelemetry;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

/**
 * Incubating utilities.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class IncubatingUtil {
  private IncubatingUtil() {}

  @SuppressWarnings("unchecked")
  public static <T> T incubatingApiIfAvailable(T stableApi, String incubatingClassName) {
    try {
      Class<?> incubatingClass = Class.forName(incubatingClassName);
      Method getInstance = incubatingClass.getDeclaredMethod("getNoop");
      return (T) getInstance.invoke(null);
    } catch (Exception e) {
      return stableApi;
    }
  }

  @Nullable
  public static OpenTelemetry obfuscatedOpenTelemetryIfIncubating(OpenTelemetry openTelemetry) {
    try {
      Class<?> extendedClass =
          Class.forName("io.opentelemetry.api.incubator.ExtendedOpenTelemetry");
      if (extendedClass.isInstance(openTelemetry)) {
        Class<?> incubatingClass =
            Class.forName(
                "io.opentelemetry.api.incubator.internal.ObfuscatedExtendedOpenTelemetry");
        return (OpenTelemetry)
            incubatingClass
                .getDeclaredConstructor(extendedClass)
                .newInstance(extendedClass.cast(openTelemetry));
      }
    } catch (Exception e) {
      // incubator not available
    }
    return null;
  }
}
