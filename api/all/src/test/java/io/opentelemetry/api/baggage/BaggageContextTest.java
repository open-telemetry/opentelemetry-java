/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

@SuppressLogger(loggerName = "io.opentelemetry.usage")
class BaggageContextTest {

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForLogger("io.opentelemetry.usage", Level.TRACE);

  @Test
  void testGetCurrentBaggage_Default() {
    try (Scope s = Context.root().makeCurrent()) {
      Baggage baggage = Baggage.current();
      assertThat(baggage).isSameAs(Baggage.empty());
    }
  }

  @Test
  void testGetCurrentBaggage_SetCorrContext() {
    Baggage baggage = Baggage.empty();
    try (Scope ignored = Context.root().with(baggage).makeCurrent()) {
      assertThat(Baggage.current()).isSameAs(baggage);
    }
  }

  @Test
  void testGetBaggage_DefaultContext() {
    Baggage baggage = Baggage.fromContext(Context.root());
    assertThat(baggage).isSameAs(Baggage.empty());
  }

  @Test
  void testGetBaggage_ExplicitContext() {
    Baggage baggage = Baggage.empty();
    Context context = Context.root().with(baggage);
    assertThat(Baggage.fromContext(context)).isSameAs(baggage);
  }

  @Test
  void fromContext_null() {
    Baggage result = Baggage.fromContext(null);
    assertThat(result).isEqualTo(Baggage.empty());
    logCapturer.assertContains("context is null");
  }

  @Test
  void fromContextOrNull_null() {
    Baggage result = Baggage.fromContextOrNull(null);
    assertThat(result).isNull();
    logCapturer.assertContains("context is null");
  }

  @Test
  void testGetBaggageWithoutDefault_DefaultContext() {
    Baggage baggage = Baggage.fromContextOrNull(Context.root());
    assertThat(baggage).isNull();
  }

  @Test
  void testGetBaggageWithoutDefault_ExplicitContext() {
    Baggage baggage = Baggage.empty();
    Context context = Context.root().with(baggage);
    assertThat(Baggage.fromContextOrNull(context)).isSameAs(baggage);
  }
}
