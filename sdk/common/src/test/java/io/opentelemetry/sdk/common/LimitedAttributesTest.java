/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.api.common.AttributeKey.valueKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class LimitedAttributesTest {

  // ---- count limit ----

  @Test
  void unbounded_acceptsAllPuts() {
    AttributeLimits noLimits =
        AttributeLimits.builder()
            .setCountLimit(Integer.MAX_VALUE)
            .setValueLengthLimit(Integer.MAX_VALUE)
            .setValueDepthLimit(Integer.MAX_VALUE)
            .build();
    Attributes attrs =
        LimitedAttributes.builder(noLimits).put(stringKey("k"), "v").put(longKey("n"), 1L).build();
    assertThat(attrs.size()).isEqualTo(2);
  }

  @Test
  void sameNameDifferentType_lastValueWins() {
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setCountLimit(10).build())
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
        LimitedAttributes.builder(AttributeLimits.builder().setCountLimit(2).build())
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
        LimitedAttributes.builder(AttributeLimits.builder().setCountLimit(2).build())
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
        LimitedAttributes.builder(AttributeLimits.builder().setValueLengthLimit(3).build())
            .put(stringKey("k"), "hello")
            .build();
    assertThat(attrs.get(stringKey("k"))).isEqualTo("hel");
  }

  @Test
  void valueLengthLimit_atExactBoundary_notTruncated() {
    String input = "hel";
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueLengthLimit(3).build())
            .put(stringKey("k"), input)
            .build();
    assertThat(attrs.get(stringKey("k"))).isSameAs(input);
  }

  @Test
  void valueLengthLimit_stringArrayTruncatesEntries() {
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueLengthLimit(2).build())
            .put(stringArrayKey("k"), Arrays.asList("aaa", "bb", "cccc"))
            .build();
    assertThat(attrs.get(stringArrayKey("k"))).containsExactly("aa", "bb", "cc");
  }

  @Test
  void valueLengthLimit_zeroTruncatesStringsToEmpty() {
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueLengthLimit(0).build())
            .put(stringKey("k"), "hello")
            .build();
    assertThat(attrs.get(stringKey("k"))).isEqualTo("");
  }

  @Test
  void valueLengthLimit_truncatesBytes() {
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueLengthLimit(3).build())
            .put(valueKey("k"), Value.of(new byte[] {1, 2, 3, 4, 5}))
            .build();
    assertThat(attrs.get(valueKey("k"))).isEqualTo(Value.of(new byte[] {1, 2, 3}));
  }

  @Test
  void valueLengthLimit_truncatesStringsInsideValueArray() {
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueLengthLimit(5).build())
            .put(valueKey("k"), Value.of(Value.of("short"), Value.of("way-too-long")))
            .build();
    assertThat(attrs.get(valueKey("k"))).isEqualTo(Value.of(Value.of("short"), Value.of("way-t")));
  }

  @Test
  void valueLengthLimit_truncatesStringsInsideValueMap() {
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueLengthLimit(3).build())
            .put(
                valueKey("k"),
                Value.of(
                    KeyValue.of("a", Value.of("ok")), KeyValue.of("b", Value.of("way-too-long"))))
            .build();
    assertThat(attrs.get(valueKey("k")))
        .isEqualTo(Value.of(KeyValue.of("a", Value.of("ok")), KeyValue.of("b", Value.of("way"))));
  }

  @Test
  void valueLengthLimit_numericArrayUntouched() {
    List<Long> input = Arrays.asList(1L, 2L, 3L);
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueLengthLimit(1).build())
            .put(longArrayKey("k"), input)
            .build();
    assertThat(attrs.get(longArrayKey("k"))).isSameAs(input);
  }

  // ---- value depth limit ----

  @Test
  void valueDepthLimit_topLevelListAtDepthOne_kept() {
    List<String> input = Arrays.asList("a", "b");
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueDepthLimit(1).build())
            .put(stringArrayKey("k"), input)
            .build();
    assertThat(attrs.get(stringArrayKey("k"))).isSameAs(input);
  }

  @Test
  void valueDepthLimit_nestedArrayReplacedWithEmpty() {
    Value<?> nested = Value.of(Value.of("a"), Value.of("b"));
    Value<?> outer = Value.of(Value.of("x"), nested);
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueDepthLimit(1).build())
            .put(valueKey("k"), outer)
            .build();
    assertThat(attrs.get(valueKey("k")))
        .isEqualTo(Value.of(Value.of("x"), Value.of(Collections.<Value<?>>emptyList())));
  }

  @Test
  void valueDepthLimit_nestedMapReplacedWithEmpty() {
    Value<?> innerMap = Value.of(KeyValue.of("inner", Value.of("v")));
    Value<?> outerMap = Value.of(KeyValue.of("nested", innerMap));
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueDepthLimit(1).build())
            .put(valueKey("k"), outerMap)
            .build();
    assertThat(attrs.get(valueKey("k")))
        .isEqualTo(Value.of(KeyValue.of("nested", Value.of(new KeyValue[0]))));
  }

  @Test
  void valueDepthLimit_atExactBoundary_kept() {
    Value<?> inner = Value.of(Value.of("x"));
    Value<?> outer = Value.of(inner);
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueDepthLimit(2).build())
            .put(valueKey("k"), outer)
            .build();
    assertThat(attrs.get(valueKey("k"))).isSameAs(outer);
  }

  @Test
  void valueDepthLimit_threeLevelNesting_middleKeptInnermostReplaced() {
    Value<?> level3 = Value.of(Value.of("deep"));
    Value<?> level2 = Value.of(level3);
    Value<?> level1 = Value.of(level2);
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueDepthLimit(2).build())
            .put(valueKey("k"), level1)
            .build();
    assertThat(attrs.get(valueKey("k")))
        .isEqualTo(Value.of(Value.of(Value.of(Collections.<Value<?>>emptyList()))));
  }

  @Test
  void valueDepthLimit_emptyArrayAtExcessDepthStillReplaced() {
    Value<?> emptyInner = Value.of(Collections.<Value<?>>emptyList());
    Value<?> outer = Value.of(emptyInner);
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueDepthLimit(1).build())
            .put(valueKey("k"), outer)
            .build();
    assertThat(attrs.get(valueKey("k")))
        .isEqualTo(Value.of(Value.of(Collections.<Value<?>>emptyList())));
  }

  @Test
  void valueDepthLimit_unchangedPassThrough() {
    Value<?> inner = Value.of(Value.of("x"));
    Value<?> outer = Value.of(inner);
    Attributes attrs =
        LimitedAttributes.builder(AttributeLimits.builder().setValueDepthLimit(100).build())
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
        LimitedAttributes.builder(
                AttributeLimits.builder().setValueLengthLimit(4).setValueDepthLimit(1).build())
            .put(valueKey("k"), outer)
            .build();
    assertThat(attrs.get(valueKey("k")))
        .isEqualTo(Value.of(Value.of("also"), Value.of(Collections.<Value<?>>emptyList())));
  }

  // ---- build caching ----

  @Test
  void buildIsCached_returnsSameInstanceBetweenMutations() {
    AttributesBuilder builder =
        LimitedAttributes.builder(AttributeLimits.builder().setCountLimit(10).build())
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
        LimitedAttributes.builder(AttributeLimits.builder().setCountLimit(10).build())
            .put(stringKey("a"), "v1")
            .put(stringKey("b"), "v2");
    Attributes first = builder.build();
    builder.remove(stringKey("a"));
    Attributes second = builder.build();
    assertThat(second).isNotSameAs(first);
    assertThat(second.size()).isEqualTo(1);
  }

  // ---- applyLimits fast path ----

  @Test
  void applyLimits_nullInput_returnsEmptySingleton() {
    Attributes result = LimitedAttributes.applyLimits(AttributeLimits.builder().build(), null);
    assertThat(result).isSameAs(Attributes.empty());
  }

  @Test
  void applyLimits_emptyInput_returnsEmptySingleton() {
    Attributes result =
        LimitedAttributes.applyLimits(AttributeLimits.builder().build(), Attributes.empty());
    assertThat(result).isSameAs(Attributes.empty());
  }

  @Test
  void applyLimits_unlimited_returnsInputIdentity() {
    Attributes input = Attributes.builder().put("k", "any-value").put("n", 42L).build();
    Attributes result = LimitedAttributes.applyLimits(AttributeLimits.builder().build(), input);
    assertThat(result).isSameAs(input);
  }

  @Test
  void applyLimits_withinLimits_returnsInputIdentity() {
    Attributes input = Attributes.builder().put("k", "short").put("n", 42L).build();
    AttributeLimits limits =
        AttributeLimits.builder()
            .setCountLimit(10)
            .setValueLengthLimit(100)
            .setValueDepthLimit(4)
            .build();
    assertThat(LimitedAttributes.applyLimits(limits, input)).isSameAs(input);
  }

  @Test
  void applyLimits_countOverflow_returnsTruncatedInstance() {
    Attributes input = Attributes.builder().put("a", "1").put("b", "2").put("c", "3").build();
    AttributeLimits limits = AttributeLimits.builder().setCountLimit(2).build();
    Attributes result = LimitedAttributes.applyLimits(limits, input);
    assertThat(result).isNotSameAs(input);
    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  void applyLimits_overLongString_returnsTruncatedInstance() {
    Attributes input = Attributes.builder().put("k", "way-too-long").build();
    AttributeLimits limits = AttributeLimits.builder().setValueLengthLimit(3).build();
    Attributes result = LimitedAttributes.applyLimits(limits, input);
    assertThat(result).isNotSameAs(input);
    assertThat(result.get(stringKey("k"))).isEqualTo("way");
  }

  @Test
  void applyLimits_overDeepValue_returnsTruncatedInstance() {
    Attributes input =
        Attributes.builder().put(valueKey("k"), Value.of(Value.of(Value.of("deep")))).build();
    AttributeLimits limits = AttributeLimits.builder().setValueDepthLimit(1).build();
    Attributes result = LimitedAttributes.applyLimits(limits, input);
    assertThat(result).isNotSameAs(input);
  }

  // ---- applyValueLimits internal helper ----

  @Test
  void applyValueLimits_returnsInputWhenUnchanged() {
    String input = "abc";
    assertThat(LimitedAttributes.applyValueLimits(input, 5, Integer.MAX_VALUE)).isSameAs(input);
  }

  @Test
  void applyValueLimits_unlimited_returnsInput() {
    Value<?> value = Value.of(Value.of("anything"));
    assertThat(LimitedAttributes.applyValueLimits(value, Integer.MAX_VALUE, Integer.MAX_VALUE))
        .isSameAs(value);
  }
}
