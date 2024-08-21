/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class AccessUtil {
  private AccessUtil() {}

  public static void ensureLoaded(Class<?> clazz) {
    // just to ensure the class is loaded
    try {
      Class.forName(clazz.getName(), true, clazz.getClassLoader());
    } catch (ClassNotFoundException e) {
      throw new AssertionError(e); // Can't happen
    }
  }
}
