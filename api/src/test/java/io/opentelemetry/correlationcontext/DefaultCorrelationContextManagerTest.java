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

package io.opentelemetry.correlationcontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DefaultCorrelationContextManagerTest {
  private static final CorrelationContextManager defaultCorrelationContextManager =
      DefaultCorrelationContextManager.getInstance();
  private static final String KEY = "key";
  private static final String VALUE = "value";

  private static final CorrelationContext DIST_CONTEXT =
      new CorrelationContext() {

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
    assertThat(defaultCorrelationContextManager.contextBuilder().build().getEntries()).isEmpty();
  }

  @Test
  void getCurrentContext_DefaultContext() {
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  void getCurrentContext_ContextSetToNull() {
    Context orig =
        CorrelationsContextUtils.withCorrelationContext(null, Context.current()).attach();
    try {
      CorrelationContext distContext = defaultCorrelationContextManager.getCurrentContext();
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  void withContext() {
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameAs(EmptyCorrelationContext.getInstance());
    try (Scope wtm = defaultCorrelationContextManager.withContext(DIST_CONTEXT)) {
      assertThat(defaultCorrelationContextManager.getCurrentContext()).isSameAs(DIST_CONTEXT);
    }
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  void withContext_nullContext() {
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameAs(EmptyCorrelationContext.getInstance());
    try (Scope wtm = defaultCorrelationContextManager.withContext(null)) {
      assertThat(defaultCorrelationContextManager.getCurrentContext())
          .isSameAs(EmptyCorrelationContext.getInstance());
    }
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  void withContextUsingWrap() {
    Runnable runnable;
    try (Scope wtm = defaultCorrelationContextManager.withContext(DIST_CONTEXT)) {
      assertThat(defaultCorrelationContextManager.getCurrentContext()).isSameAs(DIST_CONTEXT);
      runnable =
          Context.current()
              .wrap(
                  () -> {
                    assertThat(defaultCorrelationContextManager.getCurrentContext())
                        .isSameAs(DIST_CONTEXT);
                  });
    }
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameAs(EmptyCorrelationContext.getInstance());
    // When we run the runnable we will have the CorrelationContext in the current Context.
    runnable.run();
  }

  @Test
  void noopContextBuilder_SetParent_DisallowsNullParent() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    assertThrows(
        NullPointerException.class, () -> noopBuilder.setParent((CorrelationContext) null));
  }

  @Test
  void noopContextBuilder_SetParent_DisallowsNullContext() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    assertThrows(NullPointerException.class, () -> noopBuilder.setParent((Context) null));
    ;
  }

  @Test
  void noopContextBuilder_SetParent_fromContext() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    noopBuilder.setParent(Context.current()); // No error.
  }

  @Test
  void noopContextBuilder_Put_DisallowsNullKey() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    assertThrows(
        NullPointerException.class,
        () -> noopBuilder.put(null, VALUE, Entry.METADATA_UNLIMITED_PROPAGATION));
  }

  @Test
  void noopContextBuilder_Put_DisallowsNullValue() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    assertThrows(
        NullPointerException.class,
        () -> noopBuilder.put(KEY, null, Entry.METADATA_UNLIMITED_PROPAGATION));
  }

  @Test
  void noopContextBuilder_Put_DisallowsNullEntryMetadata() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    assertThrows(NullPointerException.class, () -> noopBuilder.put(KEY, VALUE, null));
  }

  @Test
  void noopContextBuilder_Remove_DisallowsNullKey() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    assertThrows(NullPointerException.class, () -> noopBuilder.remove(null));
  }
}
