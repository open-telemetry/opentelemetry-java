/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

class BaggageUtilsTest {

  @Test
  void testGetCurrentBaggage_Default() {
    try (Scope s = Context.root().makeCurrent()) {
      Baggage baggage = BaggageUtils.getCurrentBaggage();
      assertThat(baggage).isSameAs(Baggage.empty());
    }
  }

  @Test
  void testGetCurrentBaggage_SetCorrContext() {
    Baggage baggage = Baggage.empty();
    try (Scope ignored = BaggageUtils.withBaggage(baggage, Context.root()).makeCurrent()) {
      assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(baggage);
    }
  }

  @Test
  void testGetBaggage_DefaultContext() {
    Baggage baggage = BaggageUtils.getBaggage(Context.root());
    assertThat(baggage).isSameAs(Baggage.empty());
  }

  @Test
  void testGetBaggage_ExplicitContext() {
    Baggage baggage = Baggage.empty();
    Context context = BaggageUtils.withBaggage(baggage, Context.root());
    assertThat(BaggageUtils.getBaggage(context)).isSameAs(baggage);
  }

  @Test
  void testGetBaggageWithoutDefault_DefaultContext() {
    Baggage baggage = BaggageUtils.getBaggageWithoutDefault(Context.root());
    assertThat(baggage).isNull();
  }

  @Test
  void testGetBaggageWithoutDefault_ExplicitContext() {
    Baggage baggage = Baggage.empty();
    Context context = BaggageUtils.withBaggage(baggage, Context.root());
    assertThat(BaggageUtils.getBaggage(context)).isSameAs(baggage);
  }
}
