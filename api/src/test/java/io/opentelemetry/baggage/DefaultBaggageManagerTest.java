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

package io.opentelemetry.baggage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DefaultBaggageManagerTest {
  private static final BaggageManager DEFAULT_BAGGAGE_MANAGER = DefaultBaggageManager.getInstance();
  private static final String KEY = "key";
  private static final String VALUE = "value";

  private static final Baggage DIST_CONTEXT =
      new Baggage() {

        @Override
        public Collection<Entry> getEntries() {
          return Collections.singletonList(
              Entry.create(KEY, VALUE, Entry.METADATA_UNLIMITED_PROPAGATION));
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
    Context orig = BaggageUtils.withBaggage(null, Context.current()).attach();
    try {
      Baggage distContext = DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage();
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  void withContext() {
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
    try (Scope wtm = DEFAULT_BAGGAGE_MANAGER.withContext(DIST_CONTEXT)) {
      assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(DIST_CONTEXT);
    }
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void withContext_nullContext() {
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
    try (Scope wtm = DEFAULT_BAGGAGE_MANAGER.withContext(null)) {
      assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
    }
    assertThat(DEFAULT_BAGGAGE_MANAGER.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void withContextUsingWrap() {
    Runnable runnable;
    try (Scope wtm = DEFAULT_BAGGAGE_MANAGER.withContext(DIST_CONTEXT)) {
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
  void noopContextBuilder_SetParent_DisallowsNullParent() {
    Baggage.Builder noopBuilder = DEFAULT_BAGGAGE_MANAGER.baggageBuilder();
    assertThrows(NullPointerException.class, () -> noopBuilder.setParent((Baggage) null));
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
    assertThrows(
        NullPointerException.class,
        () -> noopBuilder.put(null, VALUE, Entry.METADATA_UNLIMITED_PROPAGATION));
  }

  @Test
  void noopContextBuilder_Put_DisallowsNullValue() {
    Baggage.Builder noopBuilder = DEFAULT_BAGGAGE_MANAGER.baggageBuilder();
    assertThrows(
        NullPointerException.class,
        () -> noopBuilder.put(KEY, null, Entry.METADATA_UNLIMITED_PROPAGATION));
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
