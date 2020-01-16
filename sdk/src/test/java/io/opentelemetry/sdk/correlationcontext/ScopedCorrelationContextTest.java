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

package io.opentelemetry.sdk.correlationcontext;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.context.Scope;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.EmptyCorrelationContext;
import io.opentelemetry.correlationcontext.Entry;
import io.opentelemetry.correlationcontext.EntryKey;
import io.opentelemetry.correlationcontext.EntryMetadata;
import io.opentelemetry.correlationcontext.EntryValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for the methods in {@link CorrelationContextManagerSdk} and {@link
 * CorrelationContextSdk.Builder} that interact with the current {@link CorrelationContextSdk}.
 */
@RunWith(JUnit4.class)
public class ScopedCorrelationContextTest {
  private static final EntryKey KEY_1 = EntryKey.create("key 1");
  private static final EntryKey KEY_2 = EntryKey.create("key 2");
  private static final EntryKey KEY_3 = EntryKey.create("key 3");

  private static final EntryValue VALUE_1 = EntryValue.create("value 1");
  private static final EntryValue VALUE_2 = EntryValue.create("value 2");
  private static final EntryValue VALUE_3 = EntryValue.create("value 3");
  private static final EntryValue VALUE_4 = EntryValue.create("value 4");

  private static final EntryMetadata METADATA_UNLIMITED_PROPAGATION =
      EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION);
  private static final EntryMetadata METADATA_NO_PROPAGATION =
      EntryMetadata.create(EntryMetadata.EntryTtl.NO_PROPAGATION);

  private final CorrelationContextManager contextManager = new CorrelationContextManagerSdk();

  @Test
  public void emptyCorrelationContext() {
    CorrelationContext defaultCorrelationContext = contextManager.getCurrentContext();
    assertThat(defaultCorrelationContext.getEntries()).isEmpty();
    assertThat(defaultCorrelationContext).isInstanceOf(EmptyCorrelationContext.class);
  }

  @Test
  public void withContext() {
    assertThat(contextManager.getCurrentContext().getEntries()).isEmpty();
    CorrelationContext scopedEntries =
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withContext(scopedEntries)) {
      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(scopedEntries);
    }
    assertThat(contextManager.getCurrentContext().getEntries()).isEmpty();
  }

  @Test
  public void createBuilderFromCurrentEntries() {
    CorrelationContext scopedDistContext =
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withContext(scopedDistContext)) {
      CorrelationContext newEntries =
          contextManager
              .contextBuilder()
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      assertThat(newEntries.getEntries())
          .containsExactly(
              Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
              Entry.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(scopedDistContext);
    }
  }

  @Test
  public void setCurrentEntriesWithBuilder() {
    assertThat(contextManager.getCurrentContext().getEntries()).isEmpty();
    CorrelationContext scopedDistContext =
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withContext(scopedDistContext)) {
      assertThat(contextManager.getCurrentContext().getEntries())
          .containsExactly(Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION));
      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(scopedDistContext);
    }
    assertThat(contextManager.getCurrentContext().getEntries()).isEmpty();
  }

  @Test
  public void addToCurrentEntriesWithBuilder() {
    CorrelationContext scopedDistContext =
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope1 = contextManager.withContext(scopedDistContext)) {
      CorrelationContext innerDistContext =
          contextManager
              .contextBuilder()
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      try (Scope scope2 = contextManager.withContext(innerDistContext)) {
        assertThat(contextManager.getCurrentContext().getEntries())
            .containsExactly(
                Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Entry.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
        assertThat(contextManager.getCurrentContext()).isSameInstanceAs(innerDistContext);
      }
      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(scopedDistContext);
    }
  }

  @Test
  public void multiScopeCorrelationContextWithMetadata() {
    CorrelationContext scopedDistContext =
        contextManager
            .contextBuilder()
            .put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION)
            .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
            .build();
    try (Scope scope1 = contextManager.withContext(scopedDistContext)) {
      CorrelationContext innerDistContext =
          contextManager
              .contextBuilder()
              .put(KEY_3, VALUE_3, METADATA_NO_PROPAGATION)
              .put(KEY_2, VALUE_4, METADATA_NO_PROPAGATION)
              .build();
      try (Scope scope2 = contextManager.withContext(innerDistContext)) {
        assertThat(contextManager.getCurrentContext().getEntries())
            .containsExactly(
                Entry.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Entry.create(KEY_2, VALUE_4, METADATA_NO_PROPAGATION),
                Entry.create(KEY_3, VALUE_3, METADATA_NO_PROPAGATION));
        assertThat(contextManager.getCurrentContext()).isSameInstanceAs(innerDistContext);
      }
      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(scopedDistContext);
    }
  }

  @Test
  public void setNoParent_doesNotInheritContext() {
    assertThat(contextManager.getCurrentContext().getEntries()).isEmpty();
    CorrelationContext scopedDistContext =
        contextManager.contextBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = contextManager.withContext(scopedDistContext)) {
      CorrelationContext innerDistContext =
          contextManager
              .contextBuilder()
              .setNoParent()
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      assertThat(innerDistContext.getEntries())
          .containsExactly(Entry.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
    }
    assertThat(contextManager.getCurrentContext().getEntries()).isEmpty();
  }
}
