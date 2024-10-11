/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
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
        Context.root().with(Baggage.builder().put("foo", "bar").build()).makeCurrent()) {
      Baggage result = Baggage.current();
      assertThat(result.getEntryValue("foo")).isEqualTo("bar");
    }
  }

  @Test
  void getEntryDefault() {
    BaggageEntryMetadata metadata = BaggageEntryMetadata.create("flib");
    Map<String, BaggageEntry> result = new HashMap<>();
    result.put("a", ImmutableEntry.create("b", metadata));
    // Implementation that only implements asMap() which is used by getEntry()
    Baggage baggage =
        new Baggage() {

          @Override
          public Map<String, BaggageEntry> asMap() {
            return result;
          }

          @Override
          public int size() {
            return 0;
          }

          @Override
          public void forEach(BiConsumer<? super String, ? super BaggageEntry> consumer) {
            result.forEach(consumer);
          }

          @Nullable
          @Override
          public String getEntryValue(String entryKey) {
            return null;
          }

          @Override
          public BaggageBuilder toBuilder() {
            return null;
          }
        };

    BaggageEntry entry = baggage.getEntry("a");
    assertThat(entry.getValue()).isEqualTo("b");
    assertThat(entry.getMetadata().getValue()).isEqualTo("flib");
  }
}
