/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.BaggageUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link Baggage} and {@link BaggageUtils}. TODO: move these where appropriate */
class BaggageManagerSdkTest {

  @Mock private Baggage baggage;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testGetCurrentContext_DefaultContext() {
    assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(Baggage.empty());
  }

  @Test
  void testGetCurrentContext_ContextSetToNull() {
    try (Scope ignored = Context.root().makeCurrent()) {
      Baggage baggage = BaggageUtils.getCurrentBaggage();
      assertThat(baggage).isNotNull();
      assertThat(baggage.getEntries()).isEmpty();
    }
  }

  @Test
  void testWithBaggage() {
    assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(Baggage.empty());
    try (Scope wtm = BaggageUtils.currentContextWith(baggage)) {
      assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(baggage);
    }
    assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(Baggage.empty());
  }

  @Test
  void testWithBaggageUsingWrap() {
    Runnable runnable;
    try (Scope wtm = BaggageUtils.currentContextWith(baggage)) {
      assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(baggage);
      runnable =
          Context.current()
              .wrap(
                  () -> {
                    assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(baggage);
                  });
    }
    assertThat(BaggageUtils.getCurrentBaggage()).isSameAs(Baggage.empty());
    // When we run the runnable we will have the Baggage in the current Context.
    runnable.run();
  }
}
