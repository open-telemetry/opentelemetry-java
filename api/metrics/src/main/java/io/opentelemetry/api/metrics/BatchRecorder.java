/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Util class that can be use to atomically record measurements associated with a set of Metrics.
 *
 * <p>This class is equivalent with individually calling record on every Measure, but has the
 * advantage that all these operations are recorded atomically and it is more efficient.
 */
@ThreadSafe
public interface BatchRecorder {
  /**
   * Associates the {@link LongValueRecorder} with the given value. Subsequent updates to the same
   * {@link LongValueRecorder} will overwrite the previous value.
   *
   * @param valueRecorder the {@link LongValueRecorder}.
   * @param value the value to be associated with {@code valueRecorder}.
   * @return this.
   */
  BatchRecorder put(LongValueRecorder valueRecorder, long value);

  /**
   * Associates the {@link DoubleValueRecorder} with the given value. Subsequent updates to the same
   * {@link DoubleValueRecorder} will overwrite the previous value.
   *
   * @param valueRecorder the {@link DoubleValueRecorder}.
   * @param value the value to be associated with {@code valueRecorder}.
   * @return this.
   */
  BatchRecorder put(DoubleValueRecorder valueRecorder, double value);

  /**
   * Associates the {@link LongCounter} with the given value. Subsequent updates to the same {@link
   * LongCounter} will overwrite the previous value.
   *
   * @param counter the {@link LongCounter}.
   * @param value the value to be associated with {@code counter}.
   * @return this.
   */
  BatchRecorder put(LongCounter counter, long value);

  /**
   * Associates the {@link DoubleCounter} with the given value. Subsequent updates to the same
   * {@link DoubleCounter} will overwrite the previous value.
   *
   * @param counter the {@link DoubleCounter}.
   * @param value the value to be associated with {@code counter}.
   * @return this.
   */
  BatchRecorder put(DoubleCounter counter, double value);

  /**
   * Associates the {@link LongUpDownCounter} with the given value. Subsequent updates to the same
   * {@link LongCounter} will overwrite the previous value.
   *
   * @param upDownCounter the {@link LongCounter}.
   * @param value the value to be associated with {@code counter}.
   * @return this.
   */
  BatchRecorder put(LongUpDownCounter upDownCounter, long value);

  /**
   * Associates the {@link DoubleUpDownCounter} with the given value. Subsequent updates to the same
   * {@link DoubleCounter} will overwrite the previous value.
   *
   * @param upDownCounter the {@link DoubleCounter}.
   * @param value the value to be associated with {@code counter}.
   * @return this.
   */
  BatchRecorder put(DoubleUpDownCounter upDownCounter, double value);

  /**
   * Records all of measurements at the same time.
   *
   * <p>This method records all measurements every time it is called, so make sure it is not called
   * twice if not needed.
   */
  void record();
}
