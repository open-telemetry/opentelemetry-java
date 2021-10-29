/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Simple tests of Aggregation classes API. */
class AggregationTest {

  @Test
  void haveToString() {
    assertThat(Aggregation.none()).asString().contains("NoAggregation");
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
    assertThat(Aggregation.histogram()).asString().contains("ExplicitBucketHistogram");
  }
}
