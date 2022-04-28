/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link LongCounter}. */
public interface LongCounterBuilder {

  /**
   * Sets the description for this instrument.
   *
   * <p>Description strings should follow the instrument description rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description
   */
  LongCounterBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * <p>Unit strings should follow the instrument unit rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit
   */
  LongCounterBuilder setUnit(String unit);

  /** Sets the counter for recording {@code double} values. */
  DoubleCounterBuilder ofDoubles();

  /**
   * Builds and returns a {@code LongCounter} with the desired options.
   *
   * @return a {@code LongCounter} with the desired options.
   */
  LongCounter build();

  /**
   * Builds this asynchronous instrument with the given callback.
   *
   * <p>The callback will be called when the {@link Meter} is being observed.
   *
   * <p>Callbacks are expected to abide by the following restrictions:
   *
   * <ul>
   *   <li>Run in a finite amount of time.
   *   <li>Safe to call repeatedly, across multiple threads.
   *   <li>Return positive, monotonically increasing values.
   * </ul>
   *
   * @param callback A state-capturing callback used to observe values on-demand.
   */
  ObservableLongCounter buildWithCallback(Consumer<ObservableLongMeasurement> callback);

  /**
   * Build an observer for this instrument to observe values from a {@link BatchCallback}.
   *
   * <p>This observer MUST be registered when creating a {@link Meter#batchCallback(Runnable,
   * ObservableMeasurement, ObservableMeasurement...) batchCallback}, which records to it. Values
   * observed outside registered callbacks are ignored.
   *
   * @return an observable measurement that batch callbacks use to observe values.
   */
  default ObservableLongMeasurement buildObserver() {
    return DefaultMeter.getInstance().counterBuilder("noop").buildObserver();
  }
}
