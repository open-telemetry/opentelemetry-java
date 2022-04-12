/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** Builder class for {@link BatchCallback}. */
public interface BatchCallbackBuilder {

  /**
   * Register instruments for which the callback may observe values.
   *
   * <p>Values observed for instruments that are not registered will be ignored.
   */
  BatchCallbackBuilder add(ObservableDoubleMeasurement... observables);

  /**
   * Register instruments for which the callback may observe values.
   *
   * <p>Values observed for instruments that are not registered will be ignored.
   */
  BatchCallbackBuilder add(ObservableLongMeasurement... observables);

  /**
   * Builds a batch callback with the given callback.
   *
   * <p>The callback will only be called when the {@link Meter} is being observed.
   *
   * <p>Callbacks are expected to abide by the following restrictions:
   *
   * <ul>
   *   <li>Run in a finite amount of time.
   *   <li>Safe to call repeatedly, across multiple threads.
   *   <li>Only observe values to instruments registered via {@link
   *       #add(ObservableLongMeasurement...)} or {@link #add(ObservableDoubleMeasurement...)}.
   * </ul>
   *
   * @param callback a callback used to observe values on-demand.
   */
  BatchCallback build(Runnable callback);
}
