/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;

class InstrumentationUtilTest {
  @Test
  void verifySuppressInstrumentation() {
    // Should be false by default.
    assertFalse(InstrumentationUtil.shouldSuppressInstrumentation(Context.current()));

    // Should be true inside the Runnable passed to InstrumentationUtil.suppressInstrumentation.
    InstrumentationUtil.suppressInstrumentation(
        () -> assertTrue(InstrumentationUtil.shouldSuppressInstrumentation(Context.current())));

    // Should be false after the runnable finishes.
    assertFalse(InstrumentationUtil.shouldSuppressInstrumentation(Context.current()));
  }
}
