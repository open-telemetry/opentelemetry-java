/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.api.common.AttributeKey.valueKey;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Exercises the limits-enforcing builder returned by {@link Attributes#builder(AttributeLimits)}.
 */
class BoundedAttributesBuilderTest {

  // ---- count limit ----

  @Test
  void noLimits_equivalentToDefaultBuilder() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.noLimits())
            .put(stringKey("k"), "v")
            .put(longKey("n"), 1L)
            .build();
    assertThat(attrs.size()).isEqualTo(2);
  }

  @Test
  void sameNameDifferentType_lastValueWins() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setCountLimit(10).build())
            .put(stringKey("k"), "hello")
            .put(booleanKey("k"), true)
            .build();
    assertThat(attrs.size()).isEqualTo(1);
    assertThat(attrs.get(booleanKey("k"))).isEqualTo(true);
    assertThat(attrs.get(stringKey("k"))).isNull();
  }

  @Test
  void sameNameDifferentType_doesNotConsumeExtraCapacity() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setCountLimit(2).build())
            .put(stringKey("a"), "v1")
            .put(booleanKey("a"), false)
            .put(longKey("b"), 42L)
            .build();
    assertThat(attrs.size()).isEqualTo(2);
    assertThat(attrs.get(booleanKey("a"))).isEqualTo(false);
    assertThat(attrs.get(longKey("b"))).isEqualTo(42L);
  }

  @Test
  void countLimit_dropsOverflow() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setCountLimit(2).build())
            .put(stringKey("a"), "v1")
            .put(stringKey("b"), "v2")
            .put(stringKey("c"), "v3")
            .build();
    assertThat(attrs.size()).isEqualTo(2);
    assertThat(attrs.get(stringKey("c"))).isNull();
  }

  // ---- value length limit ----

  @Test
  void valueLengthLimit_truncatesStringValues() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setValueLengthLimit(3).build())
            .put(stringKey("k"), "hello")
            .build();
    assertThat(attrs.get(stringKey("k"))).isEqualTo("hel");
  }

  @Test
  void valueLengthLimit_atExactBoundary_notTruncated() {
    // Regression: length == limit should not allocate/truncate (spec says "at most equal to").
    String input = "hel";
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setValueLengthLimit(3).build())
            .put(stringKey("k"), input)
            .build();
    assertThat(attrs.get(stringKey("k"))).isSameAs(input);
  }

  @Test
  void valueLengthLimit_stringArrayTruncatesEntries() {
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setValueLengthLimit(2).build())
            .put(stringArrayKey("k"), Arrays.asList("aaa", "bb", "cccc"))
            .build();
    assertThat(attrs.get(stringArrayKey("k"))).containsExactly("aa", "bb", "cc");
  }

  @Test
  void valueLengthLimit_numericArrayUntouched() {
    // Length limit only applies to strings and byte arrays; numeric arrays must not allocate.
    List<Long> input = Arrays.asList(1L, 2L, 3L);
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setValueLengthLimit(1).build())
            .put(longArrayKey("k"), input)
            .build();
    assertThat(attrs.get(longArrayKey("k"))).isSameAs(input);
  }

  // ---- value depth limit ----

  @Test
  void valueDepthLimit_topLevelListAtDepthOne_kept() {
    // depth=1 means depth > 1 is replaced. Top-level list IS depth 1, so kept unchanged.
    List<String> input = Arrays.asList("a", "b");
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setValueDepthLimit(1).build())
            .put(stringArrayKey("k"), input)
            .build();
    assertThat(attrs.get(stringArrayKey("k"))).isSameAs(input);
  }

  @Test
  void valueDepthLimit_nestedArrayReplacedWithEmpty() {
    // Value<ARRAY> containing Value<ARRAY>. Outer at depth 1, inner at depth 2. Limit=1 →
    // inner replaced with empty array.
    Value<?> nested = Value.of(Value.of("a"), Value.of("b"));
    Value<?> outer = Value.of(Value.of("x"), nested);
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setValueDepthLimit(1).build())
            .put(valueKey("k"), outer)
            .build();
    Value<?> result = attrs.get(valueKey("k"));
    assertThat(result).isNotNull();
    assertThat(result.getType()).isEqualTo(ValueType.ARRAY);
    @SuppressWarnings("unchecked")
    List<Value<?>> elements = (List<Value<?>>) result.getValue();
    assertThat(elements).hasSize(2);
    assertThat(elements.get(0).getValue()).isEqualTo("x");
    // Inner (at depth 2) replaced with empty array.
    assertThat(elements.get(1).getType()).isEqualTo(ValueType.ARRAY);
    @SuppressWarnings("unchecked")
    List<Value<?>> innerElements = (List<Value<?>>) elements.get(1).getValue();
    assertThat(innerElements).isEmpty();
  }

  @Test
  void valueDepthLimit_nestedMapReplacedWithEmpty() {
    Value<?> innerMap = Value.of(KeyValue.of("inner", Value.of("v")));
    Value<?> outerMap = Value.of(KeyValue.of("nested", innerMap));
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setValueDepthLimit(1).build())
            .put(valueKey("k"), outerMap)
            .build();
    Value<?> result = attrs.get(valueKey("k"));
    assertThat(result).isNotNull();
    assertThat(result.getType()).isEqualTo(ValueType.KEY_VALUE_LIST);
    @SuppressWarnings("unchecked")
    List<KeyValue> kvList = (List<KeyValue>) result.getValue();
    assertThat(kvList).hasSize(1);
    assertThat(kvList.get(0).getKey()).isEqualTo("nested");
    Value<?> innerResult = kvList.get(0).getValue();
    assertThat(innerResult.getType()).isEqualTo(ValueType.KEY_VALUE_LIST);
    @SuppressWarnings("unchecked")
    List<KeyValue> innerList = (List<KeyValue>) innerResult.getValue();
    assertThat(innerList).isEmpty();
  }

  @Test
  void valueDepthLimit_unchangedPassThrough() {
    // limit well above the nesting depth: nothing should change.
    Value<?> inner = Value.of(Value.of("x"));
    Value<?> outer = Value.of(inner);
    Attributes attrs =
        Attributes.builder(AttributeLimits.builder().setValueDepthLimit(100).build())
            .put(valueKey("k"), outer)
            .build();
    assertThat(attrs.get(valueKey("k"))).isSameAs(outer);
  }

  // ---- combined length + depth ----

  @Test
  void combined_lengthAndDepth() {
    Value<?> nested = Value.of(Value.of("very-long-string"));
    Value<?> outer = Value.of(Value.of("also-long"), nested);
    Attributes attrs =
        Attributes.builder(
                AttributeLimits.builder().setValueLengthLimit(4).setValueDepthLimit(1).build())
            .put(valueKey("k"), outer)
            .build();
    Value<?> result = attrs.get(valueKey("k"));
    assertThat(result).isNotNull();
    @SuppressWarnings("unchecked")
    List<Value<?>> elements = (List<Value<?>>) result.getValue();
    // First element: a string at depth 2, truncated to 4 chars.
    assertThat(elements.get(0).getValue()).isEqualTo("also");
    // Second element: an array at depth 2, replaced with empty array.
    assertThat(elements.get(1).getValue()).isEqualTo(Collections.emptyList());
  }

  // ---- build caching ----

  @Test
  void buildIsCached_returnsSameInstanceBetweenMutations() {
    AttributesBuilder builder =
        Attributes.builder(AttributeLimits.builder().setCountLimit(10).build())
            .put(stringKey("a"), "v");
    Attributes first = builder.build();
    Attributes second = builder.build();
    assertThat(second).isSameAs(first);
    builder.put(stringKey("b"), "v2");
    Attributes third = builder.build();
    assertThat(third).isNotSameAs(first);
    assertThat(third.size()).isEqualTo(2);
  }

  @Test
  void remove_invalidatesCache() {
    AttributesBuilder builder =
        Attributes.builder(AttributeLimits.builder().setCountLimit(10).build())
            .put(stringKey("a"), "v1")
            .put(stringKey("b"), "v2");
    Attributes first = builder.build();
    builder.remove(stringKey("a"));
    Attributes second = builder.build();
    assertThat(second).isNotSameAs(first);
    assertThat(second.size()).isEqualTo(1);
  }
}
