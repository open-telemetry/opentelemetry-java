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

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DefaultCorrelationContextManager}. */
@RunWith(JUnit4.class)
public final class DefaultCorrelationContextManagerTest {
  private static final CorrelationContextManager defaultCorrelationContextManager =
      DefaultCorrelationContextManager.getInstance();
  private static final EntryKey KEY = EntryKey.create("key");
  private static final EntryValue VALUE = EntryValue.create("value");

  private static final CorrelationContext DIST_CONTEXT =
      new CorrelationContext() {

        @Override
        public Collection<Entry> getEntries() {
          return Arrays.asList(Entry.create(KEY, VALUE, Entry.METADATA_UNLIMITED_PROPAGATION));
        }

        @Nullable
        @Override
        public EntryValue getEntryValue(EntryKey entryKey) {
          return VALUE;
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void builderMethod() {
    assertThat(defaultCorrelationContextManager.contextBuilder().build().getEntries()).isEmpty();
  }

  @Test
  public void getCurrentContext_DefaultContext() {
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void getCurrentContext_ContextSetToNull() {
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
  public void withContext() {
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    Scope wtm = defaultCorrelationContextManager.withContext(DIST_CONTEXT);
    try {
      assertThat(defaultCorrelationContextManager.getCurrentContext())
          .isSameInstanceAs(DIST_CONTEXT);
    } finally {
      wtm.close();
    }
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void withContext_nullContext() {
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    Scope wtm = defaultCorrelationContextManager.withContext(null);
    try {
      assertThat(defaultCorrelationContextManager.getCurrentContext())
          .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    } finally {
      wtm.close();
    }
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void withContextUsingWrap() {
    Runnable runnable;
    Scope wtm = defaultCorrelationContextManager.withContext(DIST_CONTEXT);
    try {
      assertThat(defaultCorrelationContextManager.getCurrentContext())
          .isSameInstanceAs(DIST_CONTEXT);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(defaultCorrelationContextManager.getCurrentContext())
                          .isSameInstanceAs(DIST_CONTEXT);
                    }
                  });
    } finally {
      wtm.close();
    }
    assertThat(defaultCorrelationContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    // When we run the runnable we will have the CorrelationContext in the current Context.
    runnable.run();
  }

  @Test
  public void noopContextBuilder_SetParent_DisallowsNullParent() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.setParent((CorrelationContext) null);
  }

  @Test
  public void noopContextBuilder_Put_DisallowsNullKey() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(null, VALUE, Entry.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopContextBuilder_Put_DisallowsNullValue() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, null, Entry.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopContextBuilder_Put_DisallowsNullEntryMetadata() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, VALUE, null);
  }

  @Test
  public void noopContextBuilder_Remove_DisallowsNullKey() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.remove(null);
  }
}
