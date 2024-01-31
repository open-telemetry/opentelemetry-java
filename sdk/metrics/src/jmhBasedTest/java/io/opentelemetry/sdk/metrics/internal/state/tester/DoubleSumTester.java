/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state.tester;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.state.TestInstrumentType;
import java.util.List;
import java.util.Random;

public class DoubleSumTester implements TestInstrumentType.InstrumentTester {
  private static final int measurementsPerAttributeSet = 1_000;

  static class DoubleSumState implements TestInstrumentType.TestInstrumentsState {
    DoubleCounter doubleCounter;
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
    DoubleSumState doubleSumState = new DoubleSumState();

    Meter meter = sdkMeterProvider.meterBuilder("meter").build();
    doubleSumState.doubleCounter = meter.counterBuilder("test.double.sum").ofDoubles().build();

    return doubleSumState;
  }

  @SuppressWarnings("ForLoopReplaceableByForEach") // This is for GC sensitivity testing: no streams
  @Override
  public void recordValuesInInstruments(
      TestInstrumentType.TestInstrumentsState testInstrumentsState,
      List<Attributes> attributesList,
      Random random) {
    DoubleSumState state = (DoubleSumState) testInstrumentsState;

    for (int j = 0; j < attributesList.size(); j++) {
      Attributes attributes = attributesList.get(j);
      for (int i = 0; i < measurementsPerAttributeSet; i++) {
        state.doubleCounter.add(1.2f, attributes);
      }
    }
  }
}
