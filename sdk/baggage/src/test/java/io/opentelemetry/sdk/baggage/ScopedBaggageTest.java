/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.EmptyBaggage;
import io.opentelemetry.baggage.Entry;
import io.opentelemetry.baggage.EntryMetadata;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the methods in {@link BaggageManagerSdk} and {@link BaggageSdk.Builder} that
 * interact with the current {@link BaggageSdk}.
 */
class ScopedBaggageTest {

  private static final String KEY_1 = "key 1";
  private static final String KEY_2 = "key 2";
  private static final String KEY_3 = "key 3";

  private static final String VALUE_1 = "value 1";
  private static final String VALUE_2 = "value 2";
  private static final String VALUE_3 = "value 3";
  private static final String VALUE_4 = "value 4";

  private static final EntryMetadata METADATA_UNLIMITED_PROPAGATION =
      EntryMetadata.create("unlimited");
  private static final EntryMetadata METADATA_NO_PROPAGATION = EntryMetadata.create("noprop");

  private final BaggageManager contextManager = new BaggageManagerSdk();

  @Test
  void emptyBaggage() {
    Baggage defaultBaggage = contextManager.getCurrentBaggage();
    assertThat(defaultBaggage.getEntries()).isEmpty();
    assertThat(defaultBaggage).isInstanceOf(EmptyBaggage.class);
  }

  @Test
  void withContext() {
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
    Baggage scopedEntries =
        contextManager.baggageBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withBaggage(scopedEntries)) {
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedEntries);
    }
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
  }

  @Test
  void createBuilderFromCurrentEntries() {
    Baggage scopedBaggage =
        contextManager.baggageBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withBaggage(scopedBaggage)) {
      Baggage newEntries =
          contextManager
              .baggageBuilder()
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      assertThat(newEntries.getEntries())
          .containsExactlyInAnyOrder(
              Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
              Entry.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedBaggage);
    }
  }

  @Test
  void setCurrentEntriesWithBuilder() {
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
    Baggage scopedBaggage =
        contextManager.baggageBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withBaggage(scopedBaggage)) {
      assertThat(contextManager.getCurrentBaggage().getEntries())
          .containsExactly(Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION));
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedBaggage);
    }
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
  }

  @Test
  void addToCurrentEntriesWithBuilder() {
    Baggage scopedBaggage =
        contextManager.baggageBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope1 = contextManager.withBaggage(scopedBaggage)) {
      Baggage innerBaggage =
          contextManager
              .baggageBuilder()
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      try (Scope scope2 = contextManager.withBaggage(innerBaggage)) {
        assertThat(contextManager.getCurrentBaggage().getEntries())
            .containsExactlyInAnyOrder(
                Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Entry.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
        assertThat(contextManager.getCurrentBaggage()).isSameAs(innerBaggage);
      }
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedBaggage);
    }
  }

  @Test
  void multiScopeBaggageWithMetadata() {
    Baggage scopedBaggage =
        contextManager
            .baggageBuilder()
            .put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION)
            .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
            .build();
    try (Scope scope1 = contextManager.withBaggage(scopedBaggage)) {
      Baggage innerBaggage =
          contextManager
              .baggageBuilder()
              .put(KEY_3, VALUE_3, METADATA_NO_PROPAGATION)
              .put(KEY_2, VALUE_4, METADATA_NO_PROPAGATION)
              .build();
      try (Scope scope2 = contextManager.withBaggage(innerBaggage)) {
        assertThat(contextManager.getCurrentBaggage().getEntries())
            .containsExactlyInAnyOrder(
                Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Entry.create(KEY_2, VALUE_4, METADATA_NO_PROPAGATION),
                Entry.create(KEY_3, VALUE_3, METADATA_NO_PROPAGATION));
        assertThat(contextManager.getCurrentBaggage()).isSameAs(innerBaggage);
      }
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedBaggage);
    }
  }

  @Test
  void setNoParent_doesNotInheritContext() {
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
    Baggage scopedBaggage =
        contextManager.baggageBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withBaggage(scopedBaggage)) {
      Baggage innerBaggage =
          contextManager
              .baggageBuilder()
              .setNoParent()
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      assertThat(innerBaggage.getEntries())
          .containsExactly(Entry.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
    }
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
  }
}
