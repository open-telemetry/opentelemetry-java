package io.opentelemetry.context.internal;

import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
