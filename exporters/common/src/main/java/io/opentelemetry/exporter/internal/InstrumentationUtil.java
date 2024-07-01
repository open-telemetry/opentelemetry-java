/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.context.Context;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time
 *
 * @deprecated use {@link io.opentelemetry.context.internal.InstrumentationUtil} instead
 */
@Deprecated
public final class InstrumentationUtil {

  private InstrumentationUtil() {}

  /**
   * Adds a Context boolean key that will allow to identify HTTP calls coming from OTel exporters.
   * The key later be checked by an automatic instrumentation to avoid tracing OTel exporter's
   * calls.
   */
  public static void suppressInstrumentation(Runnable runnable) {
    io.opentelemetry.context.internal.InstrumentationUtil.suppressInstrumentation(runnable);
  }

  /**
   * Checks if an automatic instrumentation should be suppressed with the provided Context.
   *
   * @return TRUE to suppress the automatic instrumentation, FALSE to continue with the
   *     instrumentation.
   */
  public static boolean shouldSuppressInstrumentation(Context context) {
    return io.opentelemetry.context.internal.InstrumentationUtil.shouldSuppressInstrumentation(
        context);
  }
}
