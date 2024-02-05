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

public class DoubleLastValueTester implements TestInstrumentType.InstrumentTester {
  private static final int measurementsPerAttributeSet = 1_000;

  static class DoubleLastValueState implements TestInstrumentType.TestInstrumentsState {
    DoubleCounter doubleCounter;
  }

  @Override
  public Aggregation testedAggregation() {
    return Aggregation.lastValue();
  }

  @Override
  public TestInstrumentType.TestInstrumentsState buildInstruments(
      double instrumentCount,
      SdkMeterProvider sdkMeterProvider,
      List<Attributes> attributesList,
      Random random) {
    DoubleLastValueState doubleLastValueState = new DoubleLastValueState();

    Meter meter = sdkMeterProvider.meterBuilder("meter").build();
    doubleLastValueState.doubleCounter =
        meter.counterBuilder("test.double.last.value").ofDoubles().build();

    return doubleLastValueState;
  }

  @SuppressWarnings("ForLoopReplaceableByForEach") // This is for GC sensitivity testing: no streams
  @Override
  public void recordValuesInInstruments(
      TestInstrumentType.TestInstrumentsState testInstrumentsState,
      List<Attributes> attributesList,
      Random random) {
    DoubleLastValueState state = (DoubleLastValueState) testInstrumentsState;

    for (int j = 0; j < attributesList.size(); j++) {
      Attributes attributes = attributesList.get(j);
      for (int i = 0; i < measurementsPerAttributeSet; i++) {
        state.doubleCounter.add(1.2f, attributes);
      }
    }
  }
}
