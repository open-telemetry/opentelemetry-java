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

import io.opentelemetry.scope.DefaultScopeManager;
import io.opentelemetry.scope.Scope;
import io.opentelemetry.scope.ScopeManager;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DefaultCorrelationContextManager}. */
// TODO (trask) delete tests here that were designed to test CorrelationContextManager methods that
//      are now removed
@RunWith(JUnit4.class)
public final class DefaultCorrelationContextManagerTest {
  private static final ScopeManager defaultScopeManager = DefaultScopeManager.getInstance();
  private static final CorrelationContextManager defaultCorrelationContextManager =
      DefaultCorrelationContextManager.getInstance();
  private static final EntryKey TEST_KEY = EntryKey.create("key");
  private static final EntryValue TEST_VALUE = EntryValue.create("value");

  private static final CorrelationContext DIST_CONTEXT =
      new CorrelationContext() {

        @Override
        public Collection<Entry> getEntries() {
          return Arrays.asList(
              Entry.create(TEST_KEY, TEST_VALUE, Entry.METADATA_UNLIMITED_PROPAGATION));
        }

        @Nullable
        @Override
        public EntryValue getEntryValue(EntryKey entryKey) {
          return TEST_VALUE;
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void builderMethod() {
    assertThat(defaultCorrelationContextManager.contextBuilder().build().getEntries()).isEmpty();
  }

  @Test
  public void getCurrentContext_DefaultContext() {
    assertThat(defaultScopeManager.getCorrelationContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void getCurrentContext_ContextSetToNull() {
    Scope scope = defaultScopeManager.withCorrelationContext(null);
    try {
      CorrelationContext distContext = defaultScopeManager.getCorrelationContext();
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    } finally {
      scope.close();
    }
  }

  @Test
  public void withContext() {
    assertThat(defaultScopeManager.getCorrelationContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    Scope wtm = defaultScopeManager.withCorrelationContext(DIST_CONTEXT);
    try {
      assertThat(defaultScopeManager.getCorrelationContext()).isSameInstanceAs(DIST_CONTEXT);
    } finally {
      wtm.close();
    }
    assertThat(defaultScopeManager.getCorrelationContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void withContext_nullContext() {
    assertThat(defaultScopeManager.getCorrelationContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    Scope wtm = defaultScopeManager.withCorrelationContext(null);
    try {
      assertThat(defaultScopeManager.getCorrelationContext())
          .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    } finally {
      wtm.close();
    }
    assertThat(defaultScopeManager.getCorrelationContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
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
    noopBuilder.put(null, TEST_VALUE, Entry.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopContextBuilder_Put_DisallowsNullValue() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(TEST_KEY, null, Entry.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopContextBuilder_Put_DisallowsNullEntryMetadata() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(TEST_KEY, TEST_VALUE, null);
  }

  @Test
  public void noopContextBuilder_Remove_DisallowsNullKey() {
    CorrelationContext.Builder noopBuilder = defaultCorrelationContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.remove(null);
  }
}
