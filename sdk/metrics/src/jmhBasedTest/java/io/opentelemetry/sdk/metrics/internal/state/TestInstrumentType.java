/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.state.tester.AsyncCounterTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.DoubleLastValueTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.DoubleSumTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.ExplicitBucketHistogramTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.ExponentialHistogramTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.LongLastValueTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.LongSumTester;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@SuppressWarnings("ImmutableEnumChecker")
public enum TestInstrumentType {
  ASYNC_COUNTER(AsyncCounterTester::new),
  EXPONENTIAL_HISTOGRAM(ExponentialHistogramTester::new),
  EXPLICIT_BUCKET(ExplicitBucketHistogramTester::new),
  LONG_SUM(
      LongSumTester::new,
      /* dataAllocRateReductionPercentage= */ 97.3f,
      /* allowedPercentOffset= */ 4.0f),
  DOUBLE_SUM(
      DoubleSumTester::new,
      /* dataAllocRateReductionPercentage= */ 97.3f,
      /* allowedPercentOffset= */ 2.0f),
  LONG_LAST_VALUE(
      LongLastValueTester::new,
      /* dataAllocRateReductionPercentage= */ 97.3f,
      /* allowedPercentOffset= */ 4.0f),
  DOUBLE_LAST_VALUE(
      DoubleLastValueTester::new,
      /* dataAllocRateReductionPercentage= */ 97.3f,
      /* allowedPercentOffset= */ 4.0f);

  private final Supplier<? extends InstrumentTester> instrumentTesterInitializer;
  private final float dataAllocRateReductionPercentage;
  private final double allowedPercentOffset;

  @SuppressWarnings("unused")
  TestInstrumentType(Supplier<? extends InstrumentTester> instrumentTesterInitializer) {
    this.dataAllocRateReductionPercentage = 99.8f; // default
    this.instrumentTesterInitializer = instrumentTesterInitializer;
    this.allowedPercentOffset = 2.0f;
  }

  // Some instruments have different reduction percentage.
  TestInstrumentType(
      Supplier<? extends InstrumentTester> instrumentTesterInitializer,
      float dataAllocRateReductionPercentage,
      float allowedPercentOffset) {
    this.instrumentTesterInitializer = instrumentTesterInitializer;
    this.dataAllocRateReductionPercentage = dataAllocRateReductionPercentage;
    this.allowedPercentOffset = allowedPercentOffset;
  }

  float getDataAllocRateReductionPercentage() {
    return dataAllocRateReductionPercentage;
  }

  public double getAllowedPercentOffset() {
    return allowedPercentOffset;
  }

  InstrumentTester createInstrumentTester() {
    return instrumentTesterInitializer.get();
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
