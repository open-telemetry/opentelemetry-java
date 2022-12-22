/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.debug;

import io.opentelemetry.api.internal.ConfigUtil;

/**
 * Determines if the SDK is in debugging mode (captures stack traces) or not.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DebugConfig {
  private static final String ENABLE_METRICS_DEBUG_PROPERTY = "otel.experimental.sdk.metrics.debug";
  private static boolean enabled;

  private DebugConfig() {}

  static {
    enabled = Boolean.parseBoolean(ConfigUtil.getString(ENABLE_METRICS_DEBUG_PROPERTY, "false"));
  }

  /**
   * Returns true if metrics debugging is enabled.
   *
   * <p>This will grab stack traces on instrument/view registration.
   */
  public static boolean isMetricsDebugEnabled() {
    return enabled;
  }

  /** Returns the message we send for how to enable better metrics debugging. */
  public static String getHowToEnableMessage() {
    return "To enable better debugging, run your JVM with -D"
        + ENABLE_METRICS_DEBUG_PROPERTY
        + "=true";
  }

  /** A mechanism to enable debugging for testing without having to recompile. */
  // Visible for testing
  public static void enableForTesting(boolean value) {
    enabled = value;
  }
}
