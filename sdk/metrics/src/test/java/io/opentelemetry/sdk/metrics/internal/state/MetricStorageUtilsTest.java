/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricStorageUtilsTest {

  static final Attributes a = Attributes.of(AttributeKey.stringKey("a"), "a");
  static final Attributes b = Attributes.of(AttributeKey.stringKey("b"), "b");
  static final Attributes c = Attributes.of(AttributeKey.stringKey("c"), "c");
  static final Attributes d = Attributes.of(AttributeKey.stringKey("d"), "d");
  static final Attributes e = Attributes.of(AttributeKey.stringKey("e"), "e");
  Map<Attributes, String> result;
  Map<Attributes, String> toMerge;

  @BeforeEach
  void setup() {
    result = new HashMap<>();
    result.put(a, "A");
    result.put(b, "B");
    result.put(d, null);
    toMerge = new HashMap<>();
    toMerge.put(b, "B'");
    toMerge.put(c, "C");
    toMerge.put(e, null);
  }

  @Test
  void mergeInPlace() {
    Aggregator<String> agg = buildConcatAggregator();
    MetricStorageUtils.mergeInPlace(result, toMerge, agg);

    assertThat(result).containsOnly(entry(a, "A"), entry(b, "BB'"), entry(c, "C"), entry(d, null));
    assertThat(result.get(d)).isNull();
  }

  @Test
  void diffInPlace() {
    Aggregator<String> agg = buildConcatAggregator();
    MetricStorageUtils.diffInPlace(result, toMerge, agg);

    assertThat(result).containsOnly(entry(a, "A"), entry(c, "C"), entry(d, null));
  }

  @SuppressWarnings("unchecked")
  private Aggregator<String> buildConcatAggregator() {
    Aggregator<String> agg = mock(Aggregator.class);
    doAnswer(
            invocation -> {
              String previousCumulative = invocation.getArgument(0);
              String delta = invocation.getArgument(1);
              return previousCumulative + delta;
            })
        .when(agg)
        .merge(anyString(), anyString());
    return agg;
  }
}
