/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/** {@link SdkMeter} is SDK implementation of {@link Meter}. */
final class SdkMeter implements Meter {
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;
  private final BatchLatch batchLatch = new BatchLatch();

  SdkMeter(
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = MeterSharedState.create(instrumentationLibraryInfo);
  }

  // Only used in testing....
  InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return meterSharedState.getInstrumentationLibraryInfo();
  }

  /** Collects all the metric recordings that changed since the previous call. */
  Collection<MetricData> collectAll(
      CollectionInfo collectionInfo, long epochNanos, boolean suppressSynchronousCollection) {
    batchLatch.startCollect();
    try {
      return meterSharedState.collectAll(
          collectionInfo, meterProviderSharedState, epochNanos, suppressSynchronousCollection);
    } finally {
      batchLatch.finishCollect();
    }
  }

  @Override
  public LongCounterBuilder counterBuilder(String name) {
    return new SdkLongCounter.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public LongUpDownCounterBuilder upDownCounterBuilder(String name) {
    return new SdkLongUpDownCounter.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleHistogramBuilder histogramBuilder(String name) {
    return new SdkDoubleHistogram.Builder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public DoubleGaugeBuilder gaugeBuilder(String name) {
    return new SdkDoubleGaugeBuilder(meterProviderSharedState, meterSharedState, name);
  }

  @Override
  public BatchRecorder batch() {
    return new SdkBatchRecorder(batchLatch);
  }

  static class BatchLatch {

    private static final int batchSlots = 64;

    private volatile CountDownLatch collectLatch = new CountDownLatch(0);
    private final Semaphore batchSemaphore = new Semaphore(batchSlots);

    void startBatchRecord() {
      try {
        collectLatch.await();
        batchSemaphore.acquire();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    void finishBatchRecord() {
      batchSemaphore.release();
    }

    private void startCollect() {
      collectLatch = new CountDownLatch(1);
      try {
        batchSemaphore.acquire(batchSlots);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    private void finishCollect() {
      collectLatch.countDown();
      batchSemaphore.release(batchSlots);
    }
  }
}
