/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.EntryMetadata;
import io.opentelemetry.context.Context;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class W3CBaggagePropagatorTest {

  @Test
  void fields() {
    assertThat(W3CBaggagePropagator.getInstance().fields()).containsExactly("baggage");
  }

  @Test
  void extract_noBaggageHeader() {
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.<String, String>of(), Map::get);

    assertThat(result).isEqualTo(Context.root());
  }

  @Test
  void extract_emptyBaggageHeader() {
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", ""), ImmutableMap::get);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_singleEntry() {
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key=value"), ImmutableMap::get);

    Baggage expectedBaggage = Baggage.builder().put("key", "value").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_multiEntry() {
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of("baggage", "key1=value1,key2=value2"),
            ImmutableMap::get);

    Baggage expectedBaggage = Baggage.builder().put("key1", "value1").put("key2", "value2").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_duplicateKeys() {
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key=value1,key=value2"), ImmutableMap::get);

    Baggage expectedBaggage = Baggage.builder().put("key", "value2").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_withMetadata() {
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of("baggage", "key=value;metadata-key=value;othermetadata"),
            ImmutableMap::get);

    Baggage expectedBaggage =
        Baggage.builder()
            .put("key", "value", EntryMetadata.create("metadata-key=value;othermetadata"))
            .build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_fullComplexities() {
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of(
                "baggage",
                "key1= value1; metadata-key = value; othermetadata, "
                    + "key2 =value2 , key3 =\tvalue3 ; "),
            ImmutableMap::get);

    Baggage expectedBaggage =
        Baggage.builder()
            .put("key1", "value1", EntryMetadata.create("metadata-key = value; othermetadata"))
            .put("key2", "value2", EntryMetadata.empty())
            .put("key3", "value3")
            .build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  /**
   * It would be cool if we could replace this with a fuzzer to generate tons of crud data, to make
   * sure we don't blow up with it.
   */
  @Test
  void extract_invalidHeader() {
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of(
                "baggage",
                "key1= v;alsdf;-asdflkjasdf===asdlfkjadsf ,,a sdf9asdf-alue1; metadata-key = "
                    + "value; othermetadata, key2 =value2 , key3 =\tvalue3 ; "),
            ImmutableMap::get);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void inject_noBaggage() {
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();
    Map<String, String> carrier = new HashMap<>();
    propagator.inject(Context.root(), carrier, Map::put);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_emptyBaggage() {
    Baggage baggage = Baggage.empty();
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();
    Map<String, String> carrier = new HashMap<>();
    propagator.inject(Context.root().with(baggage), carrier, Map::put);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject() {
    Baggage baggage =
        Baggage.builder()
            .put("nometa", "nometa-value")
            .put("meta", "meta-value", EntryMetadata.create("somemetadata; someother=foo"))
            .build();
    W3CBaggagePropagator propagator = new W3CBaggagePropagator();
    Map<String, String> carrier = new HashMap<>();
    propagator.inject(Context.root().with(baggage), carrier, Map::put);
    assertThat(carrier)
        .containsExactlyInAnyOrderEntriesOf(
            singletonMap(
                "baggage", "nometa=nometa-value,meta=meta-value;somemetadata; someother=foo"));
  }
}
