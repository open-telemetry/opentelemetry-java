/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal.copied;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import java.util.Objects;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class InstrumentationUtil {
  private static final ContextKey<Boolean> SUPPRESS_INSTRUMENTATION_KEY =
      ContextKey.named("suppress_instrumentation");

  private InstrumentationUtil() {}

  /**
   * Adds a Context boolean key that will allow to identify HTTP calls coming from OTel exporters.
   * The key later be checked by an automatic instrumentation to avoid tracing OTel exporter's
   * calls.
   */
  public static void suppressInstrumentation(Runnable runnable) {
    Context.current().with(SUPPRESS_INSTRUMENTATION_KEY, true).wrap(runnable).run();
  }

  /**
   * Checks if an automatic instrumentation should be suppressed with the provided Context.
   *
   * @return TRUE to suppress the automatic instrumentation, FALSE to continue with the
   *     instrumentation.
   */
  public static boolean shouldSuppressInstrumentation(Context context) {
    return Objects.equals(context.get(SUPPRESS_INSTRUMENTATION_KEY), true);
  }
}
