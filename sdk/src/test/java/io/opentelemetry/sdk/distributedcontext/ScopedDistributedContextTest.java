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

package io.opentelemetry.sdk.distributedcontext;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.context.Scope;
import io.opentelemetry.distributedcontext.CorrelationContext;
import io.opentelemetry.distributedcontext.CorrelationContextManager;
import io.opentelemetry.distributedcontext.EmptyCorrelationContext;
import io.opentelemetry.distributedcontext.Label;
import io.opentelemetry.distributedcontext.LabelKey;
import io.opentelemetry.distributedcontext.LabelMetadata;
import io.opentelemetry.distributedcontext.LabelValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for the methods in {@link CorrelationContextManagerSdk} and {@link
 * CorrelationContextSdk.Builder} that interact with the current {@link CorrelationContextSdk}.
 */
@RunWith(JUnit4.class)
public class ScopedDistributedContextTest {
  private static final LabelKey KEY_1 = LabelKey.create("key 1");
  private static final LabelKey KEY_2 = LabelKey.create("key 2");
  private static final LabelKey KEY_3 = LabelKey.create("key 3");

  private static final LabelValue VALUE_1 = LabelValue.create("value 1");
  private static final LabelValue VALUE_2 = LabelValue.create("value 2");
  private static final LabelValue VALUE_3 = LabelValue.create("value 3");
  private static final LabelValue VALUE_4 = LabelValue.create("value 4");

  private static final LabelMetadata METADATA_UNLIMITED_PROPAGATION =
      LabelMetadata.create(LabelMetadata.HopLimit.UNLIMITED_PROPAGATION);
  private static final LabelMetadata METADATA_NO_PROPAGATION =
      LabelMetadata.create(LabelMetadata.HopLimit.NO_PROPAGATION);

  private final CorrelationContextManager contextManager = new CorrelationContextManagerSdk();

  @Test
  public void emptyDistributedContext() {
    CorrelationContext defaultDistributedContext = contextManager.getCurrentContext();
    assertThat(defaultDistributedContext.getEntries()).isEmpty();
    assertThat(defaultDistributedContext).isInstanceOf(EmptyCorrelationContext.class);
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
              Label.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
              Label.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
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
          .containsExactly(Label.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION));
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
                Label.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Label.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
        assertThat(contextManager.getCurrentContext()).isSameInstanceAs(innerDistContext);
      }
      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(scopedDistContext);
    }
  }

  @Test
  public void multiScopeDistributedContextWithMetadata() {
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
                Label.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Label.create(KEY_2, VALUE_4, METADATA_NO_PROPAGATION),
                Label.create(KEY_3, VALUE_3, METADATA_NO_PROPAGATION));
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
          .containsExactly(Label.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
    }
    assertThat(contextManager.getCurrentContext().getEntries()).isEmpty();
  }
}
