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

import static io.opentelemetry.sdk.correlationcontext.CorrelationContextTestUtil.listToCorrelationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationContextManager;
import io.opentelemetry.correlationcontext.CorrelationsContextUtils;
import io.opentelemetry.correlationcontext.Entry;
import io.opentelemetry.correlationcontext.EntryMetadata;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CorrelationContextSdk} and {@link CorrelationContextSdk.Builder}.
 *
 * <p>Tests for scope management with {@link CorrelationContextManagerSdk} are in {@link
 * ScopedCorrelationContextTest}.
 */
class CorrelationContextSdkTest {
  private final CorrelationContextManager contextManager = new CorrelationContextManagerSdk();

  private static final EntryMetadata TMD =
      EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION);

  private static final String K1 = "k1";
  private static final String K2 = "k2";

  private static final String V1 = "v1";
  private static final String V2 = "v2";

  private static final Entry T1 = Entry.create(K1, V1, TMD);
  private static final Entry T2 = Entry.create(K2, V2, TMD);

  @Test
  void getEntries_empty() {
    CorrelationContextSdk distContext = new CorrelationContextSdk.Builder().build();
    assertThat(distContext.getEntries()).isEmpty();
  }

  @Test
  void getEntries_nonEmpty() {
    CorrelationContextSdk distContext = listToCorrelationContext(T1, T2);
    assertThat(distContext.getEntries()).containsExactly(T1, T2);
  }

  @Test
  void getEntries_chain() {
    Entry t1alt = Entry.create(K1, V2, TMD);
    CorrelationContextSdk parent = listToCorrelationContext(T1, T2);
    CorrelationContext distContext =
        contextManager
            .contextBuilder()
            .setParent(parent)
            .put(t1alt.getKey(), t1alt.getValue(), t1alt.getEntryMetadata())
            .build();
    assertThat(distContext.getEntries()).containsExactly(t1alt, T2);
  }

  @Test
  void put_newKey() {
    CorrelationContextSdk distContext = listToCorrelationContext(T1);
    assertThat(
            contextManager
                .contextBuilder()
                .setParent(distContext)
                .put(K2, V2, TMD)
                .build()
                .getEntries())
        .containsExactly(T1, T2);
  }

  @Test
  void put_existingKey() {
    CorrelationContextSdk distContext = listToCorrelationContext(T1);
    assertThat(
            contextManager
                .contextBuilder()
                .setParent(distContext)
                .put(K1, V2, TMD)
                .build()
                .getEntries())
        .containsExactly(Entry.create(K1, V2, TMD));
  }

  @Test
  void put_nullKey() {
    CorrelationContextSdk distContext = listToCorrelationContext(T1);
    CorrelationContext.Builder builder = contextManager.contextBuilder().setParent(distContext);
    assertThrows(NullPointerException.class, () -> builder.put(null, V2, TMD), "key");
  }

  @Test
  void put_nullValue() {
    CorrelationContextSdk distContext = listToCorrelationContext(T1);
    CorrelationContext.Builder builder = contextManager.contextBuilder().setParent(distContext);
    assertThrows(NullPointerException.class, () -> builder.put(K2, null, TMD), "value");
  }

  @Test
  void setParent_nullValue() {
    CorrelationContextSdk parent = listToCorrelationContext(T1);
    assertThrows(
        NullPointerException.class,
        () ->
            contextManager
                .contextBuilder()
                .setParent(parent)
                .setParent((CorrelationContext) null)
                .build());
  }

  @Test
  void setParent_nullContext() {
    assertThrows(
        NullPointerException.class,
        () -> contextManager.contextBuilder().setParent((Context) null));
  }

  @Test
  void setParent_fromContext() {
    CorrelationContextSdk parent = listToCorrelationContext(T1);
    Context context =
        CorrelationsContextUtils.withCorrelationContext(
            listToCorrelationContext(T2), Context.current());
    CorrelationContext corrContext =
        contextManager.contextBuilder().setParent(parent).setParent(context).build();
    assertThat(corrContext.getEntries()).containsExactly(T2);
  }

  @Test
  void setParent_fromEmptyContext() {
    Context emptyContext = Context.current();
    CorrelationContextSdk parent = listToCorrelationContext(T1);
    try (Scope scope = CorrelationsContextUtils.currentContextWith(parent)) {
      CorrelationContext corrContext =
          contextManager.contextBuilder().setParent(emptyContext).build();
      assertThat(corrContext.getEntries()).isEmpty();
    }
  }

  @Test
  void setParent_setNoParent() {
    CorrelationContextSdk parent = listToCorrelationContext(T1);
    CorrelationContext distContext =
        contextManager.contextBuilder().setParent(parent).setNoParent().build();
    assertThat(distContext.getEntries()).isEmpty();
  }

  @Test
  void remove_existingKey() {
    CorrelationContextSdk.Builder builder = new CorrelationContextSdk.Builder();
    builder.put(T1.getKey(), T1.getValue(), T1.getEntryMetadata());
    builder.put(T2.getKey(), T2.getValue(), T2.getEntryMetadata());

    assertThat(builder.remove(K1).build().getEntries()).containsExactly(T2);
  }

  @Test
  void remove_differentKey() {
    CorrelationContextSdk.Builder builder = new CorrelationContextSdk.Builder();
    builder.put(T1.getKey(), T1.getValue(), T1.getEntryMetadata());
    builder.put(T2.getKey(), T2.getValue(), T2.getEntryMetadata());

    assertThat(builder.remove(K2).build().getEntries()).containsExactly(T1);
  }

  @Test
  void remove_keyFromParent() {
    CorrelationContextSdk distContext = listToCorrelationContext(T1, T2);
    assertThat(
            contextManager.contextBuilder().setParent(distContext).remove(K1).build().getEntries())
        .containsExactly(T2);
  }

  @Test
  void remove_nullKey() {
    CorrelationContext.Builder builder = contextManager.contextBuilder();
    assertThrows(NullPointerException.class, () -> builder.remove(null), "key");
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            contextManager.contextBuilder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            contextManager.contextBuilder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            contextManager.contextBuilder().put(K2, V2, TMD).put(K1, V1, TMD).build())
        .addEqualityGroup(contextManager.contextBuilder().put(K1, V1, TMD).put(K2, V1, TMD).build())
        .addEqualityGroup(contextManager.contextBuilder().put(K1, V2, TMD).put(K2, V1, TMD).build())
        .testEquals();
  }
}
