/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state.tester;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.state.TestInstrumentType;
import io.opentelemetry.sdk.metrics.internal.state.TestInstrumentType.InstrumentTester;
import java.util.List;
import java.util.Random;

public class ExponentialHistogramTester implements InstrumentTester {

  static class ExponentialHistogramState implements TestInstrumentType.TestInstrumentsState {
    DoubleHistogram doubleHistogram;
  }

  private static final int measurementsPerAttributeSet = 1_000;

  @Override
  public Aggregation testedAggregation() {
    return Aggregation.base2ExponentialBucketHistogram();
  }

  @Override
  public TestInstrumentType.TestInstrumentsState buildInstruments(
      double instrumentCount,
      SdkMeterProvider sdkMeterProvider,
      List<Attributes> attributesList,
      Random random) {
    ExponentialHistogramState state = new ExponentialHistogramState();
    state.doubleHistogram = sdkMeterProvider.get("meter").histogramBuilder("testhistogram").build();
    return state;
  }

  @SuppressWarnings("ForLoopReplaceableByForEach") // This is for GC sensitivity testing: no streams
  @Override
  public void recordValuesInInstruments(
      TestInstrumentType.TestInstrumentsState testInstrumentsState,
      List<Attributes> attributesList,
      Random random) {

    ExponentialHistogramState state = (ExponentialHistogramState) testInstrumentsState;

    for (int j = 0; j < attributesList.size(); j++) {
      Attributes attributes = attributesList.get(j);
      for (int i = 0; i < measurementsPerAttributeSet; i++) {
        state.doubleHistogram.record(random.nextInt(10_000), attributes);
      }
    }
  }
}
