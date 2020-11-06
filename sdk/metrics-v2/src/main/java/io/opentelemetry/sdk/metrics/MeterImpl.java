/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleSumObserver;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownSumObserver;
import io.opentelemetry.api.metrics.DoubleValueObserver;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongSumObserver;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownSumObserver;
import io.opentelemetry.api.metrics.LongValueObserver;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

public class MeterImpl implements Meter {

  private final Accumulator accumulator;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  public MeterImpl(Accumulator accumulator, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.accumulator = accumulator;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  @Override
  public DoubleCounter.Builder doubleCounterBuilder(String name) {
    return null;
  }

  @Override
  public LongCounter.Builder longCounterBuilder(String name) {
    return new LongCounterBuilderImpl(accumulator, instrumentationLibraryInfo, name);
  }

  @Override
  public DoubleUpDownCounter.Builder doubleUpDownCounterBuilder(String name) {
    return null;
  }

  @Override
  public LongUpDownCounter.Builder longUpDownCounterBuilder(String name) {
    return null;
  }

  @Override
  public DoubleValueRecorder.Builder doubleValueRecorderBuilder(String name) {
    return null;
  }

  @Override
  public LongValueRecorder.Builder longValueRecorderBuilder(String name) {
    return null;
  }

  @Override
  public DoubleSumObserver.Builder doubleSumObserverBuilder(String name) {
    return null;
  }

  @Override
  public LongSumObserver.Builder longSumObserverBuilder(String name) {
    return null;
  }

  @Override
  public DoubleUpDownSumObserver.Builder doubleUpDownSumObserverBuilder(String name) {
    return null;
  }

  @Override
  public LongUpDownSumObserver.Builder longUpDownSumObserverBuilder(String name) {
    return null;
  }

  @Override
  public DoubleValueObserver.Builder doubleValueObserverBuilder(String name) {
    return null;
  }

  @Override
  public LongValueObserver.Builder longValueObserverBuilder(String name) {
    return null;
  }

  @Override
  public BatchRecorder newBatchRecorder(String... keyValueLabelPairs) {
    return null;
  }
}
