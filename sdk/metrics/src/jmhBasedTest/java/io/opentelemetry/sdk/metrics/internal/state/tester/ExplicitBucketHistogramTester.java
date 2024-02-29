/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state.tester;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.metrics.internal.state.TestInstrumentType.InstrumentTester;
import io.opentelemetry.sdk.metrics.internal.state.TestInstrumentType.TestInstrumentsState;
import java.util.List;
import java.util.Random;

public class ExplicitBucketHistogramTester implements InstrumentTester {

  static class ExplicitHistogramState implements TestInstrumentsState {
    public double maxBucketValue;
    DoubleHistogram doubleHistogram;
  }

  private static final int measurementsPerAttributeSet = 1_000;

  @Override
  public Aggregation testedAggregation() {
    return Aggregation.explicitBucketHistogram();
  }

  @Override
  public TestInstrumentsState buildInstruments(
      double instrumentCount,
      SdkMeterProvider sdkMeterProvider,
      List<Attributes> attributesList,
      Random random) {
    ExplicitHistogramState state = new ExplicitHistogramState();
    state.doubleHistogram =
        sdkMeterProvider.get("meter").histogramBuilder("test.explicit.histogram").build();
    state.maxBucketValue =
        ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES.get(
            ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES.size() - 1);
    return state;
  }

  @SuppressWarnings("ForLoopReplaceableByForEach") // This is for GC sensitivity testing: no streams
  @Override
  public void recordValuesInInstruments(
      TestInstrumentsState testInstrumentsState, List<Attributes> attributesList, Random random) {

    ExplicitHistogramState state = (ExplicitHistogramState) testInstrumentsState;

    for (int j = 0; j < attributesList.size(); j++) {
      Attributes attributes = attributesList.get(j);
      for (int i = 0; i < measurementsPerAttributeSet; i++) {
        state.doubleHistogram.record(
            random.nextInt(Double.valueOf(state.maxBucketValue * 1.1).intValue()), attributes);
      }
    }
  }
}
