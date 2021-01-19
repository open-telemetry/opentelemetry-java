/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

/** Utilities for use from tests related to OpenTelemetry. */
public final class OpenTelemetryTesting {

  private OpenTelemetryTesting() {}

  /**
   * Unsets the global {@link OpenTelemetry}. This is only meant to be used from tests which need to
   * reconfigure {@link OpenTelemetry}.
   */
  public static void resetGlobalForTest() {
    GlobalOpenTelemetry.resetForTest();
  }
}
