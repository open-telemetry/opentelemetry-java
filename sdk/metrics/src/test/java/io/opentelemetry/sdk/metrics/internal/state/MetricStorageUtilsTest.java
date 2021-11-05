/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
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
    Aggregator<String> agg = new TestAggregator();
    MetricStorageUtils.mergeInPlace(result, toMerge, agg);

    assertThat(result.keySet()).containsExactlyInAnyOrder(a, b, c, d);
    assertThat(result.get(a)).isEqualTo("A");
    assertThat(result.get(b)).isEqualTo("BB'");
    assertThat(result.get(c)).isEqualTo("C");
    assertThat(result.get(d)).isNull();
  }

  @Test
  void diffInPlace() {
    Aggregator<String> agg = new TestAggregator();
    MetricStorageUtils.diffInPlace(result, toMerge, agg);

    assertThat(result.keySet()).containsExactlyInAnyOrder(a, c, d);
    assertThat(result.get(a)).isEqualTo("A");
    assertThat(result.get(c)).isEqualTo("C");
    assertThat(result.get(d)).isNull();
  }

  private static class TestAggregator implements Aggregator<String> {
    @Override
    public String merge(String previousCumulative, String delta) {
      return previousCumulative + delta;
    }

    @Override
    public String diff(String previousCumulative, String currentCumulative) {
      return null;
    }

    @Override
    public AggregatorHandle<String> createHandle() {
      return null;
    }

    @Nullable
    @Override
    public MetricData toMetricData(
        Resource resource,
        InstrumentationLibraryInfo instrumentationLibrary,
        MetricDescriptor metricDescriptor,
        Map<Attributes, String> accumulationByLabels,
        AggregationTemporality temporality,
        long startEpochNanos,
        long lastCollectionEpoch,
        long epochNanos) {
      return null;
    }
  }
}
