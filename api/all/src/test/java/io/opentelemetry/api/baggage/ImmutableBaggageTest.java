/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Baggage} and {@link BaggageBuilder}.
 *
 * <p>Tests for scope management with {@link Baggage} are in {@link ScopedBaggageTest}.
 */
class ImmutableBaggageTest {

  private static final BaggageEntryMetadata TMD = BaggageEntryMetadata.create("tmd");

  private static final String K1 = "k1";
  private static final String K2 = "k2";

  private static final String V1 = "v1";
  private static final String V2 = "v2";

  private static final Baggage ONE_ENTRY = Baggage.builder().put(K1, V1, TMD).build();
  private static final Baggage TWO_ENTRIES =
      Baggage.builder().put(K1, V1, TMD).put(K2, V2, TMD).build();

  @Test
  void getEntries_empty() {
    Baggage baggage = Baggage.empty();
    assertThat(baggage.size()).isZero();
    assertThat(baggage.isEmpty()).isTrue();
  }

  @Test
  void getEntries_nonEmpty() {
    Baggage baggage = TWO_ENTRIES;
    assertThat(baggage.asMap())
        .containsOnly(entry(K1, Entry.create(V1, TMD)), entry(K2, Entry.create(V2, TMD)));
    assertThat(baggage.size()).isEqualTo(2);
    assertThat(baggage.isEmpty()).isFalse();
  }

  @Test
  void getEntries_chain() {
    Context parentContext = Context.root().with(TWO_ENTRIES);
    Baggage baggage = Baggage.builder().setParent(parentContext).put(K1, V2, TMD).build();
    assertThat(baggage.asMap())
        .containsOnly(entry(K1, Entry.create(V2, TMD)), entry(K2, Entry.create(V2, TMD)));
  }

  @Test
  void put_newKey() {
    Context parentContext = Context.root().with(ONE_ENTRY);
    assertThat(Baggage.builder().setParent(parentContext).put(K2, V2, TMD).build().asMap())
        .containsOnly(entry(K1, Entry.create(V1, TMD)), entry(K2, Entry.create(V2, TMD)));
  }

  @Test
  void put_existingKey() {
    Context parentContext = Context.root().with(ONE_ENTRY);
    assertThat(Baggage.builder().setParent(parentContext).put(K1, V2, TMD).build().asMap())
        .containsOnly(entry(K1, Entry.create(V2, TMD)));
  }

  @Test
  void put_nullKey() {
    Context parentContext = Context.root().with(ONE_ENTRY);
    BaggageBuilder builder = Baggage.builder().setParent(parentContext);
    Baggage built = builder.build();
    builder.put(null, V2, TMD);
    assertThat(builder.build()).isEqualTo(built);
  }

  @Test
  void put_nullValue() {
    Context parentContext = Context.root().with(ONE_ENTRY);
    BaggageBuilder builder = Baggage.builder().setParent(parentContext);
    Baggage built = builder.build();
    builder.put(K2, null, TMD);
    assertThat(builder.build()).isEqualTo(built);
  }

  @Test
  void put_nullMetadata() {
    Context parentContext = Context.root().with(ONE_ENTRY);
    BaggageBuilder builder = Baggage.builder().setParent(parentContext);
    Baggage built = builder.build();
    builder.put(K2, V2, null);
    assertThat(builder.build()).isEqualTo(built);
  }

  @Test
  void put_keyUnprintableChars() {
    Context parentContext = Context.root().with(ONE_ENTRY);
    BaggageBuilder builder = Baggage.builder().setParent(parentContext);
    Baggage built = builder.build();
    builder.put("\2ab\3cd", "value");
    assertThat(builder.build()).isEqualTo(built);
  }

  @Test
  void put_keyEmpty() {
    Context parentContext = Context.root().with(ONE_ENTRY);
    BaggageBuilder builder = Baggage.builder().setParent(parentContext);
    Baggage built = builder.build();
    builder.put("", "value");
    assertThat(builder.build()).isEqualTo(built);
  }

  @Test
  void put_valueUnprintableChars() {
    Context parentContext = Context.root().with(ONE_ENTRY);
    BaggageBuilder builder = Baggage.builder().setParent(parentContext);
    Baggage built = builder.build();
    builder.put(K2, "\2ab\3cd");
    assertThat(builder.build()).isEqualTo(built);
  }

  @Test
  void setParent_nullContext() {
    assertThrows(NullPointerException.class, () -> Baggage.builder().setParent(null));
  }

  @Test
  void setParent_fromContext() {
    Baggage baggage = Baggage.builder().put(K2, V2, TMD).build();
    Context context = Context.root().with(baggage);
    baggage = Baggage.builder().setParent(context).build();
    assertThat(baggage.asMap()).containsOnly(entry(K2, Entry.create(V2, TMD)));
  }

  @Test
  void setParent_fromEmptyContext() {
    Context emptyContext = Context.root();
    try (Scope ignored = ONE_ENTRY.makeCurrent()) {
      Baggage baggage = Baggage.builder().setParent(emptyContext).build();
      assertThat(baggage.isEmpty()).isTrue();
    }
  }

  @Test
  void setParent_setNoParent() {
    Context parentContext = Context.root().with(ONE_ENTRY);
    Baggage baggage = Baggage.builder().setParent(parentContext).setNoParent().build();
    assertThat(baggage.isEmpty()).isTrue();
  }

  @Test
  void remove_existingKey() {
    BaggageBuilder builder = Baggage.builder();
    builder.put(K1, V1, TMD);
    builder.put(K2, V2, TMD);

    assertThat(builder.remove(K1).build().asMap()).containsOnly(entry(K2, Entry.create(V2, TMD)));
  }

  @Test
  void remove_differentKey() {
    BaggageBuilder builder = Baggage.builder();
    builder.put(K1, V1, TMD);
    builder.put(K2, V2, TMD);

    assertThat(builder.remove(K2).build().asMap()).containsOnly(entry(K1, Entry.create(V1, TMD)));
  }

  @Test
  void remove_keyFromParent() {
    Context parentContext = Context.root().with(TWO_ENTRIES);
    assertThat(Baggage.builder().setParent(parentContext).remove(K1).build().asMap())
        .containsOnly(entry(K2, Entry.create(V2, TMD)));
  }

  @Test
  void remove_nullKey() {
    BaggageBuilder builder = Baggage.builder();
    builder.put(K2, V2);
    Baggage built = builder.build();
    builder.remove(null);
    assertThat(builder.build()).isEqualTo(built);
  }

  @Test
  void toBuilder_keepsOriginalState() {
    assertThat(Baggage.empty().toBuilder().build()).isEqualTo(Baggage.empty());

    Baggage originalBaggage = Baggage.builder().put("key", "value").build();
    assertThat(originalBaggage.toBuilder().build()).isEqualTo(originalBaggage);

    Baggage parentedBaggage =
        Baggage.builder().setParent(Context.root().with(originalBaggage)).build();
    assertThat(parentedBaggage.toBuilder().build()).isEqualTo(parentedBaggage);
  }

  @Test
  void toBuilder_allowChanges() {
    Baggage singleItemNoParent = Baggage.builder().put("key1", "value1").setNoParent().build();
    Baggage singleItemWithParent =
        Baggage.builder()
            .setParent(Context.root().with(Baggage.empty()))
            .put("key1", "value1")
            .build();

    assertThat(Baggage.empty().toBuilder().put("key1", "value1").build())
        .isEqualTo(singleItemNoParent);
    assertThat(singleItemNoParent.toBuilder().put("key2", "value2").build())
        .isEqualTo(
            Baggage.builder().put("key1", "value1").put("key2", "value2").setNoParent().build());
    assertThat(singleItemNoParent.toBuilder().put("key1", "value2").build())
        .isEqualTo(Baggage.builder().put("key1", "value2").setNoParent().build());

    assertThat(singleItemWithParent.toBuilder().put("key1", "value2").build())
        .isEqualTo(
            Baggage.builder()
                .put("key1", "value2")
                .setParent(Context.root().with(Baggage.empty()))
                .build());
  }

  @Test
  void testEquals() {
    Baggage baggage1 = Baggage.builder().put(K1, V1).build();
    Baggage baggage2 =
        Baggage.builder().setParent(Context.current().with(baggage1)).put(K1, V2).build();
    Baggage baggage3 = Baggage.builder().put(K1, V2).build();
    new EqualsTester()
        .addEqualityGroup(
            Baggage.builder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            Baggage.builder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            Baggage.builder().put(K2, V2, TMD).put(K1, V1, TMD).build())
        .addEqualityGroup(Baggage.builder().put(K1, V1, TMD).put(K2, V1, TMD).build())
        .addEqualityGroup(Baggage.builder().put(K1, V2, TMD).put(K2, V1, TMD).build())
        .addEqualityGroup(baggage2, baggage3)
        .testEquals();
  }
}
