/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link DoubleCounter}. */
public interface DoubleCounterBuilder {
  /**
   * Sets the description for this instrument.
   *
   * <p>Description strings should follow the instrument description rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description
   */
  DoubleCounterBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * <p>Unit strings should follow the instrument unit rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit
   */
  DoubleCounterBuilder setUnit(String unit);

  /**
   * Builds and returns a {@code DoubleCounter} with the desired options.
   *
   * @return a {@code DoubleCounter} with the desired options.
   */
  DoubleCounter build();

  /**
   * Builds this asynchronous instrument with the given callback.
   *
   * <p>The callback will only be called when the {@link Meter} is being observed.
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
  ObservableDoubleCounter buildWithCallback(Consumer<ObservableDoubleMeasurement> callback);

  /**
   * Build an observer for this instrument to observe values from a {@link BatchCallback}.
   *
   * <p>This observer MUST be registered when creating a {@link Meter#batchCallback(Runnable,
   * ObservableMeasurement, ObservableMeasurement...) batchCallback}, which records to it. Values
   * observed outside registered callbacks are ignored.
   *
   * @return an observable measurement that batch callbacks use to observe values.
   */
  ObservableDoubleMeasurement buildObserver();
}
