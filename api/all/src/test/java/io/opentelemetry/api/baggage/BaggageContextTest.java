/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

class BaggageContextTest {

  @Test
  void testGetCurrentBaggage_Default() {
    try (Scope s = Context.groot().makeCurrent()) {
      Baggage baggage = Baggage.current();
      assertThat(baggage).isSameAs(Baggage.empty());
    }
  }

  @Test
  void testGetCurrentBaggage_SetCorrContext() {
    Baggage baggage = Baggage.empty();
    try (Scope ignored = Context.groot().with(baggage).makeCurrent()) {
      assertThat(Baggage.current()).isSameAs(baggage);
    }
  }

  @Test
  void testGetBaggage_DefaultContext() {
    Baggage baggage = Baggage.fromContext(Context.groot());
    assertThat(baggage).isSameAs(Baggage.empty());
  }

  @Test
  void testGetBaggage_ExplicitContext() {
    Baggage baggage = Baggage.empty();
    Context context = Context.groot().with(baggage);
    assertThat(Baggage.fromContext(context)).isSameAs(baggage);
  }

  @Test
  void testGetBaggageWithoutDefault_DefaultContext() {
    Baggage baggage = Baggage.fromContextOrNull(Context.groot());
    assertThat(baggage).isNull();
  }

  @Test
  void testGetBaggageWithoutDefault_ExplicitContext() {
    Baggage baggage = Baggage.empty();
    Context context = Context.groot().with(baggage);
    assertThat(Baggage.fromContextOrNull(context)).isSameAs(baggage);
  }
}
