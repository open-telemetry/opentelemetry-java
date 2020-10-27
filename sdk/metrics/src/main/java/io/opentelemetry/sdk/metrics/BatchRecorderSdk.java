/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongValueRecorder;

/**
 * Minimal implementation of the {@link BatchRecorder} that simply redirects the calls to the
 * instruments.
 *
 * <p>TODO: Add an async queue processing to process batch records.
 */
final class BatchRecorderSdk implements BatchRecorder {
  private final Labels labelSet;

  BatchRecorderSdk(String... keyValuePairs) {
    this.labelSet = Labels.of(keyValuePairs);
  }

  @Override
  public BatchRecorder put(LongValueRecorder valueRecorder, long value) {
    valueRecorder.record(value, labelSet);
    return this;
  }

  @Override
  public BatchRecorder put(DoubleValueRecorder valueRecorder, double value) {
    valueRecorder.record(value, labelSet);
    return this;
  }

  @Override
  public BatchRecorder put(LongCounter counter, long value) {
    counter.add(value, labelSet);
    return this;
  }

  @Override
  public BatchRecorder put(DoubleCounter counter, double value) {
    counter.add(value, labelSet);
    return this;
  }

  @Override
  public BatchRecorder put(LongUpDownCounter upDownCounter, long value) {
    upDownCounter.add(value, labelSet);
    return this;
  }

  @Override
  public BatchRecorder put(DoubleUpDownCounter upDownCounter, double value) {
    upDownCounter.add(value, labelSet);
    return this;
  }

  @Override
  public void record() {
    // No-op in this minimal implementation.
  }
}
