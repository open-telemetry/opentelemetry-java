/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
      EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION);
  private static final EntryMetadata METADATA_NO_PROPAGATION =
      EntryMetadata.create(EntryMetadata.EntryTtl.NO_PROPAGATION);

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
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withContext(scopedEntries)) {
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedEntries);
    }
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
  }

  @Test
  void createBuilderFromCurrentEntries() {
    Baggage scopedDistContext =
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withContext(scopedDistContext)) {
      Baggage newEntries =
          contextManager
              .contextBuilder()
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      assertThat(newEntries.getEntries())
          .containsExactlyInAnyOrder(
              Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
              Entry.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedDistContext);
    }
  }

  @Test
  void setCurrentEntriesWithBuilder() {
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
    Baggage scopedDistContext =
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withContext(scopedDistContext)) {
      assertThat(contextManager.getCurrentBaggage().getEntries())
          .containsExactly(Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION));
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedDistContext);
    }
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
  }

  @Test
  void addToCurrentEntriesWithBuilder() {
    Baggage scopedDistContext =
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope1 = contextManager.withContext(scopedDistContext)) {
      Baggage innerDistContext =
          contextManager
              .contextBuilder()
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      try (Scope scope2 = contextManager.withContext(innerDistContext)) {
        assertThat(contextManager.getCurrentBaggage().getEntries())
            .containsExactlyInAnyOrder(
                Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Entry.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
        assertThat(contextManager.getCurrentBaggage()).isSameAs(innerDistContext);
      }
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedDistContext);
    }
  }

  @Test
  void multiScopeBaggageWithMetadata() {
    Baggage scopedDistContext =
        contextManager
            .contextBuilder()
            .put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION)
            .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
            .build();
    try (Scope scope1 = contextManager.withContext(scopedDistContext)) {
      Baggage innerDistContext =
          contextManager
              .contextBuilder()
              .put(KEY_3, VALUE_3, METADATA_NO_PROPAGATION)
              .put(KEY_2, VALUE_4, METADATA_NO_PROPAGATION)
              .build();
      try (Scope scope2 = contextManager.withContext(innerDistContext)) {
        assertThat(contextManager.getCurrentBaggage().getEntries())
            .containsExactlyInAnyOrder(
                Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Entry.create(KEY_2, VALUE_4, METADATA_NO_PROPAGATION),
                Entry.create(KEY_3, VALUE_3, METADATA_NO_PROPAGATION));
        assertThat(contextManager.getCurrentBaggage()).isSameAs(innerDistContext);
      }
      assertThat(contextManager.getCurrentBaggage()).isSameAs(scopedDistContext);
    }
  }

  @Test
  void setNoParent_doesNotInheritContext() {
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
    Baggage scopedDistContext =
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withContext(scopedDistContext)) {
      Baggage innerDistContext =
          contextManager
              .contextBuilder()
              .setNoParent()
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      assertThat(innerDistContext.getEntries())
          .containsExactly(Entry.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
    }
    assertThat(contextManager.getCurrentBaggage().getEntries()).isEmpty();
  }
}
