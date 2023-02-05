/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.metrics.Aggregation;
import org.junit.jupiter.api.Test;

class AggregationUtilTest {

  @Test
  void forName() {
    assertThat(AggregationUtil.forName("sum")).isEqualTo(Aggregation.sum());
    assertThat(AggregationUtil.forName("last_value")).isEqualTo(Aggregation.lastValue());
    assertThat(AggregationUtil.forName("explicit_bucket_histogram"))
        .isEqualTo(Aggregation.explicitBucketHistogram());
    assertThat(AggregationUtil.forName("EXplicit_BUCKet_HISTOGRAM"))
        .isEqualTo(Aggregation.explicitBucketHistogram());
    assertThat(AggregationUtil.forName("drop")).isEqualTo(Aggregation.drop());
    assertThat(AggregationUtil.forName("base2_exponential_bucket_histogram"))
        .isEqualTo(Aggregation.base2ExponentialBucketHistogram());
    assertThatThrownBy(() -> AggregationUtil.forName("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unrecognized aggregation name foo");
  }

  @Test
  void aggregationName() {
    assertThat(AggregationUtil.aggregationName(Aggregation.defaultAggregation()))
        .isEqualTo("default");
    assertThat(AggregationUtil.aggregationName(Aggregation.sum())).isEqualTo("sum");
    assertThat(AggregationUtil.aggregationName(Aggregation.lastValue())).isEqualTo("last_value");
    assertThat(AggregationUtil.aggregationName(Aggregation.drop())).isEqualTo("drop");
    assertThat(AggregationUtil.aggregationName(Aggregation.explicitBucketHistogram()))
        .isEqualTo("explicit_bucket_histogram");
    assertThat(AggregationUtil.aggregationName(Aggregation.base2ExponentialBucketHistogram()))
        .isEqualTo("base2_exponential_bucket_histogram");
    assertThatThrownBy(() -> AggregationUtil.aggregationName(new Aggregation() {}))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Unrecognized aggregation");
  }
}
