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
    try (Scope scope = Context.groot().makeCurrent()) {
      assertThat(Baggage.current()).isEqualTo(Baggage.empty());
    }
  }

  @Test
  void current() {
    try (Scope scope =
        Context.groot().with(Baggage.builder().put("foo", "bar").build()).makeCurrent()) {
      Baggage result = Baggage.current();
      assertThat(result.getEntryValue("foo")).isEqualTo("bar");
    }
  }
}
