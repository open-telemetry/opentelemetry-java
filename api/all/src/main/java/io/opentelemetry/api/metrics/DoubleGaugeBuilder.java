/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** A builder for Gauge metric types. These can only be asynchronously collected. */
public interface DoubleGaugeBuilder {
  /**
   * Sets the description for this instrument.
   *
   * <p>Description strings should follow the instrument description rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description
   */
  DoubleGaugeBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * <p>Unit strings should follow the instrument unit rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit
   */
  DoubleGaugeBuilder setUnit(String unit);

  /** Sets the gauge for recording {@code long} values. */
  LongGaugeBuilder ofLongs();

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
   * </ul>
   *
   * @param callback A state-capturing callback used to observe values on-demand.
   */
  ObservableDoubleGauge buildWithCallback(Consumer<ObservableDoubleMeasurement> callback);

  /**
   * Build an observer for this instrument to observe values from a {@link BatchCallback}.
   *
   * <p>This observer MUST be registered when creating a {@link Meter#batchCallback(Runnable,
   * ObservableMeasurement, ObservableMeasurement...) batchCallback}, which records to it. Values
   * observed outside registered callbacks are ignored.
   *
   * @return an observable measurement that batch callbacks use to observe values.
   */
  default ObservableDoubleMeasurement buildObserver() {
    return DefaultMeter.getInstance().gaugeBuilder("noop").buildObserver();
  }
}
