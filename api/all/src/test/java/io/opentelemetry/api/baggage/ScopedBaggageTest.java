/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

/** Unit tests for the methods in {@link Baggage} that interact with the current {@link Baggage}. */
class ScopedBaggageTest {

  private static final String KEY_1 = "key 1";
  private static final String KEY_2 = "key 2";
  private static final String KEY_3 = "key 3";

  private static final String VALUE_1 = "value 1";
  private static final String VALUE_2 = "value 2";
  private static final String VALUE_3 = "value 3";
  private static final String VALUE_4 = "value 4";

  private static final BaggageEntryMetadata METADATA_UNLIMITED_PROPAGATION =
      BaggageEntryMetadata.create("unlimited");
  private static final BaggageEntryMetadata METADATA_NO_PROPAGATION =
      BaggageEntryMetadata.create("noprop");

  @Test
  void emptyBaggage() {
    Baggage defaultBaggage = Baggage.current();
    assertThat(defaultBaggage.isEmpty()).isTrue();
  }

  @Test
  void withContext() {
    assertThat(Baggage.current().isEmpty()).isTrue();
    Baggage scopedEntries =
        Baggage.builder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = scopedEntries.makeCurrent()) {
      assertThat(Baggage.current()).isSameAs(scopedEntries);
    }
    assertThat(Baggage.current().isEmpty()).isTrue();
  }

  @Test
  void createBuilderFromCurrentEntries() {
    Baggage scopedBaggage =
        Baggage.builder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = scopedBaggage.makeCurrent()) {
      Baggage newEntries =
          Baggage.current().toBuilder().put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION).build();
      assertThat(newEntries.asMap())
          .containsOnly(
              entry(KEY_1, ImmutableEntry.create(VALUE_1, METADATA_UNLIMITED_PROPAGATION)),
              entry(KEY_2, ImmutableEntry.create(VALUE_2, METADATA_UNLIMITED_PROPAGATION)));
      assertThat(Baggage.current()).isSameAs(scopedBaggage);
    }
  }

  @Test
  void setCurrentEntriesWithBuilder() {
    assertThat(Baggage.current().isEmpty()).isTrue();
    Baggage scopedBaggage =
        Baggage.builder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = scopedBaggage.makeCurrent()) {
      assertThat(Baggage.current().asMap())
          .containsOnly(
              entry(KEY_1, ImmutableEntry.create(VALUE_1, METADATA_UNLIMITED_PROPAGATION)));
      assertThat(Baggage.current()).isSameAs(scopedBaggage);
    }
    assertThat(Baggage.current().isEmpty()).isTrue();
  }

  @Test
  void addToCurrentEntriesWithBuilder() {
    Baggage scopedBaggage =
        Baggage.builder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope1 = scopedBaggage.makeCurrent()) {
      Baggage innerBaggage =
          Baggage.current().toBuilder().put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION).build();
      try (Scope scope2 = innerBaggage.makeCurrent()) {
        assertThat(Baggage.current().asMap())
            .containsOnly(
                entry(KEY_1, ImmutableEntry.create(VALUE_1, METADATA_UNLIMITED_PROPAGATION)),
                entry(KEY_2, ImmutableEntry.create(VALUE_2, METADATA_UNLIMITED_PROPAGATION)));
        assertThat(Baggage.current()).isSameAs(innerBaggage);
      }
      assertThat(Baggage.current()).isSameAs(scopedBaggage);
    }
  }

  @Test
  void multiScopeBaggageWithMetadata() {
    Baggage scopedBaggage =
        Baggage.builder()
            .put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION)
            .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
            .build();
    try (Scope scope1 = scopedBaggage.makeCurrent()) {
      Baggage innerBaggage =
          Baggage.current().toBuilder()
              .put(KEY_3, VALUE_3, METADATA_NO_PROPAGATION)
              .put(KEY_2, VALUE_4, METADATA_NO_PROPAGATION)
              .build();
      try (Scope scope2 = innerBaggage.makeCurrent()) {
        assertThat(Baggage.current().asMap())
            .containsOnly(
                entry(KEY_1, ImmutableEntry.create(VALUE_1, METADATA_UNLIMITED_PROPAGATION)),
                entry(KEY_2, ImmutableEntry.create(VALUE_4, METADATA_NO_PROPAGATION)),
                entry(KEY_3, ImmutableEntry.create(VALUE_3, METADATA_NO_PROPAGATION)));
        assertThat(Baggage.current()).isSameAs(innerBaggage);
      }
      assertThat(Baggage.current()).isSameAs(scopedBaggage);
    }
  }
}
