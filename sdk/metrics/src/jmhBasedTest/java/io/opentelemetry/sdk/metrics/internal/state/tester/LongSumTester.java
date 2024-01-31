/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state.tester;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.state.TestInstrumentType;
import java.util.List;
import java.util.Random;

public class LongSumTester implements TestInstrumentType.InstrumentTester {
  private static final int measurementsPerAttributeSet = 1_000;

  static class LongSumState implements TestInstrumentType.TestInstrumentsState {
    LongCounter longCounter;
  }

  @Override
  public Aggregation testedAggregation() {
    return Aggregation.sum();
  }

  @Override
  public TestInstrumentType.TestInstrumentsState buildInstruments(
      double instrumentCount,
      SdkMeterProvider sdkMeterProvider,
      List<Attributes> attributesList,
      Random random) {
    LongSumState longSumState = new LongSumState();

    Meter meter = sdkMeterProvider.meterBuilder("meter").build();
    longSumState.longCounter = meter.counterBuilder("test.long.sum").build();

    return longSumState;
  }

  @SuppressWarnings("ForLoopReplaceableByForEach") // This is for GC sensitivity testing: no streams
  @Override
  public void recordValuesInInstruments(
      TestInstrumentType.TestInstrumentsState testInstrumentsState,
      List<Attributes> attributesList,
      Random random) {
    LongSumState state = (LongSumState) testInstrumentsState;

    for (int j = 0; j < attributesList.size(); j++) {
      Attributes attributes = attributesList.get(j);
      for (int i = 0; i < measurementsPerAttributeSet; i++) {
        state.longCounter.add(1, attributes);
      }
    }
  }
}
