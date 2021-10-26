/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
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
    assertThat(Aggregation.sum(AggregationTemporality.DELTA)).asString().contains("Sum");
    assertThat(Aggregation.explicitBucketHistogram())
        .asString()
        .contains("ExplicitBucketHistogram");
    assertThat(Aggregation.explicitBucketHistogram(AggregationTemporality.DELTA))
        .asString()
        .contains("ExplicitBucketHistogram");
    assertThat(
            Aggregation.explicitBucketHistogram(
                AggregationTemporality.CUMULATIVE, Arrays.asList(1.0d)))
        .asString()
        .contains("ExplicitBucketHistogram");
    assertThat(Aggregation.explicitBucketHistogram(Arrays.asList(1.0d)))
        .asString()
        .contains("ExplicitBucketHistogram");
  }

  @Test
  void noTemporalityIsNullTemporality() {
    assertThat(Aggregation.none()).extracting(Aggregation::getConfiguredTemporality).isNull();
    assertThat(Aggregation.defaultAggregation())
        .extracting(Aggregation::getConfiguredTemporality)
        .isNull();
    assertThat(Aggregation.lastValue()).extracting(Aggregation::getConfiguredTemporality).isNull();
    assertThat(Aggregation.sum()).extracting(Aggregation::getConfiguredTemporality).isNull();
    assertThat(Aggregation.histogram()).extracting(Aggregation::getConfiguredTemporality).isNull();
    assertThat(Aggregation.explicitBucketHistogram())
        .extracting(Aggregation::getConfiguredTemporality)
        .isNull();
    assertThat(Aggregation.explicitBucketHistogram(Arrays.asList(1d)))
        .extracting(Aggregation::getConfiguredTemporality)
        .isNull();
  }

  @Test
  void returnsConfiguredTemporality() {
    assertThat(Aggregation.sum(AggregationTemporality.CUMULATIVE))
        .extracting(Aggregation::getConfiguredTemporality)
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(Aggregation.explicitBucketHistogram(AggregationTemporality.DELTA))
        .extracting(Aggregation::getConfiguredTemporality)
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void histogramUsesExplicitBucket() {
    // Note: This will change when exponential histograms are launched.
    assertThat(Aggregation.histogram()).asString().contains("ExplicitBucketHistogram");
  }
}
