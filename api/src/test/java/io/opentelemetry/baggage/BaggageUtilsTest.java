/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

class BaggageUtilsTest {

  @Test
  void testGetCurrentBaggage_Default() {
    Baggage baggage = BaggageUtils.getCurrentBaggage();
    assertThat(baggage).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void testGetCurrentBaggage_SetCorrContext() {
    Baggage baggage = DefaultBaggageManager.getInstance().baggageBuilder().build();
    try (Scope ignored = BaggageUtils.withBaggage(baggage, Context.current()).makeCurrent()) {
      assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(baggage);
    }
  }

  @Test
  void testGetBaggage_DefaultContext() {
    Baggage baggage = BaggageUtils.getBaggage(Context.current());
    assertThat(baggage).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void testGetBaggage_ExplicitContext() {
    Baggage baggage = DefaultBaggageManager.getInstance().baggageBuilder().build();
    Context context = BaggageUtils.withBaggage(baggage, Context.current());
    assertThat(BaggageUtils.getBaggage(context)).isSameAs(baggage);
  }

  @Test
  void testGetBaggageWithoutDefault_DefaultContext() {
    Baggage baggage = BaggageUtils.getBaggageWithoutDefault(Context.current());
    assertThat(baggage).isNull();
  }

  @Test
  void testGetBaggageWithoutDefault_ExplicitContext() {
    Baggage baggage = DefaultBaggageManager.getInstance().baggageBuilder().build();
    Context context = BaggageUtils.withBaggage(baggage, Context.current());
    assertThat(BaggageUtils.getBaggage(context)).isSameAs(baggage);
  }
}
