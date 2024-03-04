/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state.tester;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.state.TestInstrumentType;
import java.util.List;
import java.util.Random;

public class DoubleLastValueTester implements TestInstrumentType.InstrumentTester {

  @Override
  public Aggregation testedAggregation() {
    return Aggregation.lastValue();
  }

  @SuppressWarnings("ForLoopReplaceableByForEach") // This is for GC sensitivity testing: no streams
  @Override
  public TestInstrumentType.TestInstrumentsState buildInstruments(
      double instrumentCount,
      SdkMeterProvider sdkMeterProvider,
      List<Attributes> attributesList,
      Random random) {
    Meter meter = sdkMeterProvider.meterBuilder("meter").build();
    meter
        .gaugeBuilder("test.double.last.value")
        .buildWithCallback(
            observableDoubleMeasurement -> {
              for (int j = 0; j < attributesList.size(); j++) {
                observableDoubleMeasurement.record(1.2f, attributesList.get(j));
              }
            });

    return new TestInstrumentType.EmptyInstrumentsState();
  }

  @Override
  public void recordValuesInInstruments(
      TestInstrumentType.TestInstrumentsState testInstrumentsState,
      List<Attributes> attributesList,
      Random random) {
    // Recording is done by the callback define above
  }
}
