/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

class BaggageTest {
  @Test
  void current_empty() {
    try (Scope scope = Context.root().makeCurrent()) {
      assertThat(Baggage.current()).isEqualTo(Baggage.empty());
    }
  }

  @Test
  void current() {
    try (Scope scope =
        Context.root()
            .with(Baggage.builder().put("foo", "bar").setNoParent().build())
            .makeCurrent()) {
      Baggage result = Baggage.current();
      assertThat(result.getEntryValue("foo")).isEqualTo("bar");
      assertThat(result).isEqualTo(Baggage.builder().setNoParent().put("foo", "bar").build());
    }
  }
}
