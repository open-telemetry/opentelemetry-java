/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  private static final TextMapGetter<Map<String, List<String>>> multiGetter =
      new TextMapGetter<Map<String, List<String>>>() {
        @Override
        public Iterable<String> keys(Map<String, List<String>> carrier) {
          return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(Map<String, List<String>> carrier, String key) {
          return carrier.getOrDefault(key, Collections.emptyList()).stream()
              .findFirst()
              .orElse(null);
        }

        @Override
        public Iterator<String> getAll(Map<String, List<String>> carrier, String key) {
          List<String> values = carrier.get(key);
          return values == null ? Collections.emptyIterator() : values.iterator();
        }
      };

  @Test
  void fields() {
    assertThat(W3CBaggagePropagator.getInstance().fields()).containsExactly("baggage");
  }

  @Test
  void extract_key_duplicate() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key=value1,key=value2"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key", "value2").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_key_leadingSpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "  key=value1"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key", "value1").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_key_trailingSpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key     =value1"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key", "value1").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_key_onlySpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "   =value1"), getter);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_key_withInnerSpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "ke y=value1"), getter);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_key_withSeparators() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "ke?y=value1"), getter);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_key_singleValid_multipleInvalid() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of(
                "baggage",
                "ke<y=value1, ;sss,key=value;meta1=value1;meta2=value2,ke(y=value;meta=val "),
            getter);

    Baggage expectedBaggage =
        Baggage.builder()
            .put("key", "value", BaggageEntryMetadata.create("meta1=value1;meta2=value2"))
            .build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_leadingSpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "  key=  value1"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key", "value1").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_urlEncoding() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key=value%201"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key", "value 1").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_trailingSpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key=value1      "), getter);

    Baggage expectedBaggage = Baggage.builder().put("key", "value1").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_trailingSpaces_withMetadata() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key=value1      ;meta1=meta2"), getter);

    Baggage expectedBaggage =
        Baggage.builder().put("key", "value1", BaggageEntryMetadata.create("meta1=meta2")).build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_empty() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key1="), getter);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_value_empty_withMetadata() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key1=;metakey=metaval"), getter);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_value_onlySpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key1=     "), getter);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_value_withInnerSpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key=valu e1"), getter);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_value_withSeparators() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key=val\\ue1"), getter);

    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_value_multiple_leadingSpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key=  value1,key1=val"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key1", "val").put("key", "value1").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_multiple_trailingSpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key=value1      ,key1=val"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key1", "val").put("key", "value1").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_multiple_empty_withMeEtadata() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key1=;metakey=metaval,key1=val"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key1", "val").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_multiple_empty() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key1=,key1=val"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key1", "val").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_multiple_onlySpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key1=     ,key1=val"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key1", "val").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_multiple_withInnerSpaces() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key=valu e1,key1=val"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key1", "val").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_value_multiple_withSeparators() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key=val\\ue1,key1=val"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key1", "val").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_header_missing() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result = propagator.extract(Context.root(), ImmutableMap.of(), getter);
    assertThat(result).isEqualTo(Context.root());
  }

  @Test
  void extract_header_empty() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result = propagator.extract(Context.root(), ImmutableMap.of("baggage", ""), getter);
    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_member_single() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(Context.root(), ImmutableMap.of("baggage", "key=value"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key", "value").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_member_multi() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", "key1=value1,key2=value2"), getter);

    Baggage expectedBaggage = Baggage.builder().put("key1", "value1").put("key2", "value2").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_member_single_withMetadata() {
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
  void extract_member_fullComplexities() {
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
  void extract_member_someInvalid() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context input = Context.current();
    Context result =
        propagator.extract(
            input,
            ImmutableMap.of(
                "baggage",
                "key1= v;alsdf;-asdflkjasdf===asdlfkjadsf ,,a sdf9asdf-alue1; metadata-key = "
                    + "value; othermetadata, key2 =value2, key3 =\tvalue3 ; "),
            getter);

    Baggage expectedBaggage =
        Baggage.builder()
            .put("key1", "v", BaggageEntryMetadata.create("alsdf;-asdflkjasdf===asdlfkjadsf"))
            .put("key2", "value2")
            .put("key3", "value3")
            .build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
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
  void extract_multiple_headers() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of("baggage", ImmutableList.of("k1=v1", "k2=v2")),
            multiGetter);

    Baggage expectedBaggage = Baggage.builder().put("k1", "v1").put("k2", "v2").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_multiple_headers_duplicate_key() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of("baggage", ImmutableList.of("k1=v1", "k1=v2")),
            multiGetter);

    Baggage expectedBaggage = Baggage.builder().put("k1", "v2").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_multiple_headers_mixed_duplicates_non_duplicates() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of("baggage", ImmutableList.of("k1=v1,k2=v0", "k2=v2,k3=v3")),
            multiGetter);

    Baggage expectedBaggage =
        Baggage.builder().put("k1", "v1").put("k2", "v2").put("k3", "v3").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_multiple_headers_all_empty() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", ImmutableList.of("", "")), multiGetter);

    Baggage expectedBaggage = Baggage.builder().build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_multiple_headers_some_empty() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(), ImmutableMap.of("baggage", ImmutableList.of("", "k=v")), multiGetter);

    Baggage expectedBaggage = Baggage.builder().put("k", "v").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_multiple_headers_all_invalid() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of("baggage", ImmutableList.of("!@#$%^", "key=va%lue")),
            multiGetter);

    Baggage expectedBaggage = Baggage.builder().build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_multiple_headers_some_invalid() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();

    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of("baggage", ImmutableList.of("k1=v1", "key=va%lue", "k2=v2")),
            multiGetter);

    Baggage expectedBaggage = Baggage.builder().put("k1", "v1").put("k2", "v2").build();
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
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
            .put("needsEncoding", "blah blah blah")
            .put("meta", "meta-value", BaggageEntryMetadata.create("somemetadata; someother=foo"))
            .put("\2ab\3cd", "wacky key nonprintable")
            .put(null, "null key")
            .put("nullvalue", null)
            .put(" ", "key is only space")
            .build();
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    Map<String, String> carrier = new HashMap<>();
    propagator.inject(Context.root().with(baggage), carrier, Map::put);
    assertThat(carrier)
        .containsExactlyInAnyOrderEntriesOf(
            singletonMap(
                "baggage",
                "meta=meta-value;somemetadata%3B%20someother%3Dfoo,needsEncoding=blah%20blah%20blah,nometa=nometa-value"));
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

  @ParameterizedTest
  @MethodSource
  void extract_limit_maxEntries(List<String> headers, Baggage expectedBaggage) {
    Context result =
        W3CBaggagePropagator.getInstance()
            .extract(Context.root(), ImmutableMap.of("baggage", headers), multiGetter);
    assertThat(Baggage.fromContext(result)).isEqualTo(expectedBaggage);
  }

  static Stream<Arguments> extract_limit_maxEntries() {
    return Stream.of(
        // Exactly at the limit — all 64 entries extracted
        Arguments.of(ImmutableList.of(baggageHeader(0, 64)), baggageWithEntries(0, 64)),
        // One over the limit — only the first 64 extracted
        Arguments.of(ImmutableList.of(baggageHeader(0, 65)), baggageWithEntries(0, 64)),
        // Split across two headers — only the first 64 total extracted
        Arguments.of(
            ImmutableList.of(baggageHeader(0, 32), baggageHeader(32, 33)),
            baggageWithEntries(0, 64)));
  }

  /**
   * Builds a {@link Baggage} with entries {@code k{start}=v{start}} through {@code
   * k{start+count-1}=v{start+count-1}}.
   */
  private static Baggage baggageWithEntries(int start, int count) {
    BaggageBuilder builder = Baggage.builder();
    for (int i = start; i < start + count; i++) {
      builder.put("k" + i, "v" + i);
    }
    return builder.build();
  }

  /** Builds {@code "k{start}=v{start},...,k{start+count-1}=v{start+count-1}"}. */
  private static String baggageHeader(int start, int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = start; i < start + count; i++) {
      if (i > start) {
        sb.append(",");
      }
      sb.append("k").append(i).append("=v").append(i);
    }
    return sb.toString();
  }

  @Test
  void extract_limit_maxBytes_exceedsLimit() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    // Single header over 8192 bytes — dropped entirely; partial values must not be extracted
    String header = "k=" + fillChars('v', 8192); // 8194 bytes
    Context result = propagator.extract(Context.root(), ImmutableMap.of("baggage", header), getter);
    assertThat(Baggage.fromContext(result)).isEqualTo(Baggage.empty());
  }

  @Test
  void extract_limit_maxBytes_acrossMultipleHeaders() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    // First header just under 8192 bytes is extracted; second header pushes total over the limit
    String almostMax = "k=" + fillChars('v', 8189); // "k=vvv..."
    String second = "k2=v2";
    Context result =
        propagator.extract(
            Context.root(),
            ImmutableMap.of("baggage", ImmutableList.of(almostMax, second)),
            multiGetter);
    // Only the first header should have been extracted
    assertThat(Baggage.fromContext(result).size()).isEqualTo(1);
    assertThat(Baggage.fromContext(result).getEntryValue("k2")).isNull();
  }

  @Test
  void inject_limit_maxEntries() {
    Map<String, String> carrier = new HashMap<>();
    W3CBaggagePropagator.getInstance()
        .inject(Context.root().with(baggageWithEntries(0, 74)), carrier, Map::put);
    String header = carrier.get("baggage");
    assertThat(header).isNotNull();
    long count = header.chars().filter(c -> c == '=').count();
    assertThat(count).isEqualTo(64);
  }

  @Test
  void inject_limit_maxBytes() {
    W3CBaggagePropagator propagator = W3CBaggagePropagator.getInstance();
    // One entry whose encoded form alone exceeds the byte limit — should produce empty header
    Baggage baggage = Baggage.builder().put("k", fillChars('v', 8192)).build();
    Map<String, String> carrier = new HashMap<>();
    propagator.inject(Context.root().with(baggage), carrier, Map::put);
    assertThat(carrier).doesNotContainKey("baggage");
  }

  @Test
  void inject_limit_maxBytes_metadata() {
    // Value alone fits easily (k=v is 3 bytes), but k=v;{metadata} exceeds 8192 bytes.
    // Verifies that metadata length is included in the byte limit check.
    Baggage baggage =
        Baggage.builder().put("k", "v", BaggageEntryMetadata.create(fillChars('x', 8190))).build();
    Map<String, String> carrier = new HashMap<>();
    W3CBaggagePropagator.getInstance().inject(Context.root().with(baggage), carrier, Map::put);
    assertThat(carrier).doesNotContainKey("baggage");
  }

  private static String fillChars(char c, int count) {
    char[] chars = new char[count];
    Arrays.fill(chars, c);
    return new String(chars);
  }

  @Test
  void toString_Valid() {
    assertThat(W3CBaggagePropagator.getInstance().toString()).isEqualTo("W3CBaggagePropagator");
  }
}
