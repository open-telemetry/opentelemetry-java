/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.state.tester.AsyncCounterTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.DoubleSumTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.ExplicitBucketHistogramTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.ExponentialHistogramTester;
import io.opentelemetry.sdk.metrics.internal.state.tester.LongSumTester;
import java.util.List;
import java.util.Random;

public enum TestInstrumentType {
  ASYNC_COUNTER() {
    @Override
    InstrumentTester createInstrumentTester() {
      return new AsyncCounterTester();
    }
  },
  EXPONENTIAL_HISTOGRAM() {
    @Override
    InstrumentTester createInstrumentTester() {
      return new ExponentialHistogramTester();
    }
  },
  EXPLICIT_BUCKET() {
    @Override
    InstrumentTester createInstrumentTester() {
      return new ExplicitBucketHistogramTester();
    }
  },
  LONG_SUM(/* dataAllocRateReductionPercentage= */ 97.3f) {
    @Override
    InstrumentTester createInstrumentTester() {
      return new LongSumTester();
    }
  },
  DOUBLE_SUM(/* dataAllocRateReductionPercentage= */ 97.3f) {
    @Override
    InstrumentTester createInstrumentTester() {
      return new DoubleSumTester();
    }
  };

  private final float dataAllocRateReductionPercentage;

  TestInstrumentType() {
    this.dataAllocRateReductionPercentage = 99.8f; // default
  }

  // Some instruments have different reduction percentage.
  TestInstrumentType(float dataAllocRateReductionPercentage) {
    this.dataAllocRateReductionPercentage = dataAllocRateReductionPercentage;
  }

  float getDataAllocRateReductionPercentage() {
    return dataAllocRateReductionPercentage;
  }

  abstract InstrumentTester createInstrumentTester();

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
