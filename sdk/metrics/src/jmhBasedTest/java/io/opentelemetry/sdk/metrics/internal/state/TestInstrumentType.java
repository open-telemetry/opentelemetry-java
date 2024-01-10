/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.state.tester.AsyncCounterTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.ExponentialHistogramTester;
import java.util.List;
import java.util.Random;

public enum TestInstrumentType {
  ASYNC_COUNTER(AsyncCounterTester.class),
  EXPONENTIAL_HISTOGRAM(ExponentialHistogramTester.class);

  final Class<? extends InstrumentTester> instrumentTesterClass;

  TestInstrumentType(Class<? extends InstrumentTester> instrumentTesterClass) {
    this.instrumentTesterClass = instrumentTesterClass;
  }

  public interface InstrumentTester {
    Aggregation testedAggregation();

    TestInstrumentsState buildInstruments(
        double instrumentCount,
        SdkMeterProvider sdkMeterProvider,
        List<Attributes> attributesList,
        Random random);

    void recordValuesInInstruments(
        TestInstrumentsState testInstrumentsState, List<Attributes> attributesList, Random random);
  }

  public interface TestInstrumentsState {}

  public static class EmptyInstrumentsState implements TestInstrumentsState {}
}
