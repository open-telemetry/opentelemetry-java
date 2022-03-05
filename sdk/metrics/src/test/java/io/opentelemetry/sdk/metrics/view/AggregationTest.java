/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Simple tests of Aggregation classes API. */
class AggregationTest {

  @Test
  void haveToString() {
    assertThat(Aggregation.drop()).asString().contains("DropAggregation");
    assertThat(Aggregation.defaultAggregation()).asString().contains("Default");
    assertThat(Aggregation.lastValue()).asString().contains("LastValue");
    assertThat(Aggregation.sum()).asString().contains("Sum");
    assertThat(Aggregation.explicitBucketHistogram())
        .asString()
        .contains("ExplicitBucketHistogram");
    assertThat(Aggregation.explicitBucketHistogram(Arrays.asList(1.0d)))
        .asString()
        .contains("ExplicitBucketHistogram");
  }

  @Test
  void histogramUsesExplicitBucket() {
    // Note: This will change when exponential histograms are launched.
    assertThat(Aggregation.explicitBucketHistogram())
        .asString()
        .contains("ExplicitBucketHistogram");
  }

  @Test
  void forName() {
    assertThat(Aggregation.forName("sum")).isEqualTo(Aggregation.sum());
    assertThat(Aggregation.forName("last_value")).isEqualTo(Aggregation.lastValue());
    assertThat(Aggregation.forName("histogram")).isEqualTo(Aggregation.explicitBucketHistogram());
    assertThat(Aggregation.forName("drop")).isEqualTo(Aggregation.drop());
    assertThatThrownBy(() -> Aggregation.forName("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unrecognized aggregation name foo");
  }

  @Test
  void aggregationName() {
    assertThat(Aggregation.aggregationName(Aggregation.defaultAggregation())).isEqualTo("default");
    assertThat(Aggregation.aggregationName(Aggregation.sum())).isEqualTo("sum");
    assertThat(Aggregation.aggregationName(Aggregation.lastValue())).isEqualTo("last_value");
    assertThat(Aggregation.aggregationName(Aggregation.drop())).isEqualTo("drop");
    assertThat(Aggregation.aggregationName(Aggregation.explicitBucketHistogram()))
        .isEqualTo("explicit_bucket_histogram");
    assertThatThrownBy(() -> Aggregation.aggregationName(new Aggregation() {}))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Unrecognized aggregation");
  }
}
