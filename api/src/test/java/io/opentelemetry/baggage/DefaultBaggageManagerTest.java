/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DefaultBaggageManagerTest {
  private static final BaggageManager DEFAULT_BAGGAGE_MANAGER = DefaultBaggageManager.getInstance();
  private static final String KEY = "key";
  private static final String VALUE = "value";
  private static final EntryMetadata SAMPLE_METADATA = EntryMetadata.create("sample");

  private static final Baggage DIST_CONTEXT =
      new Baggage() {

        @Override
        public Collection<Entry> getEntries() {
          return Collections.singletonList(Entry.create(KEY, VALUE, SAMPLE_METADATA));
        }

        @Override
        public String getEntryValue(String entryKey) {
          return VALUE;
        }
      };

  @Test
  void builderMethod() {
    assertThat(DEFAULT_BAGGAGE_MANAGER.baggageBuilder().build().getEntries()).isEmpty();
  }

  @Test
  void getCurrentContext_DefaultContext() {
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void getCurrentContext_ContextSetToNull() {
    try (Scope ignored = BaggageUtils.withBaggage(null, Context.current()).makeCurrent()) {
      Baggage baggage = DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage();
      assertThat(baggage).isNotNull();
      assertThat(baggage.getEntries()).isEmpty();
    }
  }

  @Test
  void withContext() {
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
    try (Scope wtm = DEFAULT_BAGGAGE_MANAGER.withBaggage(DIST_CONTEXT)) {
      assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(DIST_CONTEXT);
    }
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void withContext_nullContext() {
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
    try (Scope wtm = DEFAULT_BAGGAGE_MANAGER.withBaggage(null)) {
      assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
    }
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void withContextUsingWrap() {
    Runnable runnable;
    try (Scope wtm = DEFAULT_BAGGAGE_MANAGER.withBaggage(DIST_CONTEXT)) {
      assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(DIST_CONTEXT);
      runnable =
          Context.current()
              .wrap(
                  () -> {
                    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(DIST_CONTEXT);
                  });
    }
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
    // When we run the runnable we will have the Baggage in the current Context.
    runnable.run();
  }

  @Test
  void noopContextBuilder_SetParent_DisallowsNullContext() {
    Baggage.Builder noopBuilder = DEFAULT_BAGGAGE_MANAGER.baggageBuilder();
    assertThrows(NullPointerException.class, () -> noopBuilder.setParent((Context) null));
    ;
  }

  @Test
  void noopContextBuilder_SetParent_fromContext() {
    Baggage.Builder noopBuilder = DEFAULT_BAGGAGE_MANAGER.baggageBuilder();
    noopBuilder.setParent(Context.current()); // No error.
  }

  @Test
  void noopContextBuilder_Put_DisallowsNullKey() {
    Baggage.Builder noopBuilder = DEFAULT_BAGGAGE_MANAGER.baggageBuilder();
    assertThrows(NullPointerException.class, () -> noopBuilder.put(null, VALUE, SAMPLE_METADATA));
  }

  @Test
  void noopContextBuilder_Put_DisallowsNullValue() {
    Baggage.Builder noopBuilder = DEFAULT_BAGGAGE_MANAGER.baggageBuilder();
    assertThrows(NullPointerException.class, () -> noopBuilder.put(KEY, null, SAMPLE_METADATA));
  }

  @Test
  void noopContextBuilder_Put_DisallowsNullEntryMetadata() {
    Baggage.Builder noopBuilder = DEFAULT_BAGGAGE_MANAGER.baggageBuilder();
    assertThrows(NullPointerException.class, () -> noopBuilder.put(KEY, VALUE, null));
  }

  @Test
  void noopContextBuilder_Remove_DisallowsNullKey() {
    Baggage.Builder noopBuilder = DEFAULT_BAGGAGE_MANAGER.baggageBuilder();
    assertThrows(NullPointerException.class, () -> noopBuilder.remove(null));
  }
}
