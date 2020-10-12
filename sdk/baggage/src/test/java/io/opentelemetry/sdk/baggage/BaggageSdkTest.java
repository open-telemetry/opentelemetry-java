/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.baggage;

import static io.opentelemetry.sdk.baggage.BaggageTestUtil.listToBaggage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.BaggageManager;
import io.opentelemetry.baggage.BaggageUtils;
import io.opentelemetry.baggage.Entry;
import io.opentelemetry.baggage.EntryMetadata;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BaggageSdk} and {@link BaggageSdk.Builder}.
 *
 * <p>Tests for scope management with {@link BaggageManagerSdk} are in {@link ScopedBaggageTest}.
 */
class BaggageSdkTest {

  private final BaggageManager contextManager = new BaggageManagerSdk();

  private static final EntryMetadata TMD = EntryMetadata.create("tmd");

  private static final String K1 = "k1";
  private static final String K2 = "k2";

  private static final String V1 = "v1";
  private static final String V2 = "v2";

  private static final Entry T1 = Entry.create(K1, V1, TMD);
  private static final Entry T2 = Entry.create(K2, V2, TMD);

  @Test
  void getEntries_empty() {
    BaggageSdk baggage = new BaggageSdk.Builder().build();
    assertThat(baggage.getEntries()).isEmpty();
  }

  @Test
  void getEntries_nonEmpty() {
    BaggageSdk baggage = listToBaggage(T1, T2);
    assertThat(baggage.getEntries()).containsExactly(T1, T2);
  }

  @Test
  void getEntries_chain() {
    Entry t1alt = Entry.create(K1, V2, TMD);
    Baggage parent = listToBaggage(T1, T2);
    Context parentContext = BaggageUtils.withBaggage(parent, Context.root());
    Baggage baggage =
        contextManager
            .baggageBuilder()
            .setParent(parentContext)
            .put(t1alt.getKey(), t1alt.getValue(), t1alt.getEntryMetadata())
            .build();
    assertThat(baggage.getEntries()).containsExactly(t1alt, T2);
  }

  @Test
  void put_newKey() {
    Baggage parent = listToBaggage(T1);
    Context parentContext = BaggageUtils.withBaggage(parent, Context.root());
    assertThat(
            contextManager
                .baggageBuilder()
                .setParent(parentContext)
                .put(K2, V2, TMD)
                .build()
                .getEntries())
        .containsExactly(T1, T2);
  }

  @Test
  void put_existingKey() {
    BaggageSdk parent = listToBaggage(T1);
    Context parentContext = BaggageUtils.withBaggage(parent, Context.root());
    assertThat(
            contextManager
                .baggageBuilder()
                .setParent(parentContext)
                .put(K1, V2, TMD)
                .build()
                .getEntries())
        .containsExactly(Entry.create(K1, V2, TMD));
  }

  @Test
  void put_nullKey() {
    Baggage parent = listToBaggage(T1);
    Context parentContext = BaggageUtils.withBaggage(parent, Context.root());
    Baggage.Builder builder = contextManager.baggageBuilder().setParent(parentContext);
    assertThrows(NullPointerException.class, () -> builder.put(null, V2, TMD), "key");
  }

  @Test
  void put_nullValue() {
    BaggageSdk parent = listToBaggage(T1);
    Context parentContext = BaggageUtils.withBaggage(parent, Context.root());
    Baggage.Builder builder = contextManager.baggageBuilder().setParent(parentContext);
    assertThrows(NullPointerException.class, () -> builder.put(K2, null, TMD), "value");
  }

  @Test
  void setParent_nullContext() {
    assertThrows(
        NullPointerException.class,
        () -> contextManager.baggageBuilder().setParent((Context) null));
  }

  @Test
  void setParent_fromContext() {
    Context context = BaggageUtils.withBaggage(listToBaggage(T2), Context.current());
    Baggage baggage = contextManager.baggageBuilder().setParent(context).build();
    assertThat(baggage.getEntries()).containsExactly(T2);
  }

  @Test
  void setParent_fromEmptyContext() {
    Context emptyContext = Context.current();
    BaggageSdk parent = listToBaggage(T1);
    try (Scope scope = BaggageUtils.currentContextWith(parent)) {
      Baggage baggage = contextManager.baggageBuilder().setParent(emptyContext).build();
      assertThat(baggage.getEntries()).isEmpty();
    }
  }

  @Test
  void setParent_setNoParent() {
    BaggageSdk parent = listToBaggage(T1);
    Context parentContext = BaggageUtils.withBaggage(parent, Context.root());
    Baggage baggage =
        contextManager.baggageBuilder().setParent(parentContext).setNoParent().build();
    assertThat(baggage.getEntries()).isEmpty();
  }

  @Test
  void remove_existingKey() {
    BaggageSdk.Builder builder = new BaggageSdk.Builder();
    builder.put(T1.getKey(), T1.getValue(), T1.getEntryMetadata());
    builder.put(T2.getKey(), T2.getValue(), T2.getEntryMetadata());

    assertThat(builder.remove(K1).build().getEntries()).containsExactly(T2);
  }

  @Test
  void remove_differentKey() {
    BaggageSdk.Builder builder = new BaggageSdk.Builder();
    builder.put(T1.getKey(), T1.getValue(), T1.getEntryMetadata());
    builder.put(T2.getKey(), T2.getValue(), T2.getEntryMetadata());

    assertThat(builder.remove(K2).build().getEntries()).containsExactly(T1);
  }

  @Test
  void remove_keyFromParent() {
    BaggageSdk parent = listToBaggage(T1, T2);
    Context parentContext = BaggageUtils.withBaggage(parent, Context.root());
    assertThat(
            contextManager
                .baggageBuilder()
                .setParent(parentContext)
                .remove(K1)
                .build()
                .getEntries())
        .containsExactly(T2);
  }

  @Test
  void remove_nullKey() {
    Baggage.Builder builder = contextManager.baggageBuilder();
    assertThrows(NullPointerException.class, () -> builder.remove(null), "key");
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            contextManager.baggageBuilder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            contextManager.baggageBuilder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            contextManager.baggageBuilder().put(K2, V2, TMD).put(K1, V1, TMD).build())
        .addEqualityGroup(contextManager.baggageBuilder().put(K1, V1, TMD).put(K2, V1, TMD).build())
        .addEqualityGroup(contextManager.baggageBuilder().put(K1, V2, TMD).put(K2, V1, TMD).build())
        .testEquals();
  }
}
