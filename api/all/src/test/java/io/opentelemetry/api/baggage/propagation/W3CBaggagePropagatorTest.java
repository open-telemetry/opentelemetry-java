/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class W3CBaggagePropagatorTest {

  private static final TextMapGetter<Map<String, String>> getter =
      new TextMapGetter<Map<String, String>>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  @Test
  void fields() {
    assertThat(W3CBaggagePropagator.getInstance().fields()).containsExactly("baggage");
  }

  @Test
  void extract_noBaggageHeader() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result = propagator.extract(Context.root(), ImmutableMap.of(), getter);

    assertThat(result).isEqualTo(Context.root());
  }

  @Test
  void extract_emptyBaggageHeader() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result = propagator.extract(Context.root(), ImmutableMap.of("baggage", ""), getter);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_singleEntry() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key=value"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key", "value").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_multiEntry() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key1=value1,key2=value2"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key1", "value1").put("key2", "value2").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_duplicateKeys() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key=value1,key=value2"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key", "value2").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_withMetadata() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of("baggage", "key=value;metadata-key=value;othermetadata"),
            getter);

    Baggage expectedBaggage =
        Baggage.builder()
            .put("key", "value", BaggageEntryMetadata.create("metadata-key=value;othermetadata"))
            .build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_fullComplexities() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of(
                "baggage",
                "key1= value1; metadata-key = value; othermetadata, "
                    + "key2 =value2 , key3 =\tvalue3 ; "),
            getter);

    Baggage expectedBaggage =
        Baggage.builder()
            .put(
                "key1",
                "value1",
                BaggageEntryMetadata.create("metadata-key = value; othermetadata"))
            .put("key2", "value2", BaggageEntryMetadata.empty())
            .put("key3", "value3")
            .build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_invalidHeader() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context input = Context.current();
    Context result =
        propagator.extract(
            input,
            ImmutableMap.of(
                "baggage",
                "key1= v;alsdf;-asdflkjasdf===asdlfkjadsf ,,a sdf9asdf-alue1; metadata-key = "
                    + "value; othermetadata, key2 =value2 , key3 =\tvalue3 ; "),
            getter);
    assertThat(result).isSameAs(input);
    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_nullContext() {
    assertThat(W3CBaggagePropagator.getInstance().extract(null, Collections.emptyMap(), getter))
        .isSameAs(Context.root());
  }

  @Test
  void extract_nullGetter() {
    Context context = Context.current().with(Baggage.builder().put("cat", "meow").build());
    assertThat(W3CBaggagePropagator.getInstance().extract(context, Collections.emptyMap(), null))
        .isSameAs(context);
  }

  @Test
  void inject_noBaggage() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    Map<String, String> carrier = new HashMap<>();
    propagator.inject(Context.root(), carrier, Map::put);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_emptyBaggage() {
    Baggage baggage = Baggage.empty();
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    Map<String, String> carrier = new HashMap<>();
    propagator.inject(Context.root().with(baggage), carrier, Map::put);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject() {
    Baggage baggage =
        Baggage.builder()
            .put("nometa", "nometa-value")
            .put("meta", "meta-value", BaggageEntryMetadata.create("somemetadata; someother=foo"))
            .build();
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    Map<String, String> carrier = new HashMap<>();
    propagator.inject(Context.root().with(baggage), carrier, Map::put);
    assertThat(carrier)
        .containsExactlyInAnyOrderEntriesOf(
            singletonMap(
                "baggage", "meta=meta-value;somemetadata; someother=foo,nometa=nometa-value"));
  }

  @Test
  void inject_nullContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    W3CBaggagePropagator.getInstance().inject(null, carrier, Map::put);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_nullSetter() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context = Context.current().with(Baggage.builder().put("cat", "meow").build());
    W3CBaggagePropagator.getInstance().inject(context, carrier, null);
    assertThat(carrier).isEmpty();
  }
}
