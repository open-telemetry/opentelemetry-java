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
   * @param description The description.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description">Instrument
   *     Description</a>
   */
  DoubleGaugeBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * @param unit The unit. Instrument units must be 63 or fewer ASCII characters.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit">Instrument
   *     Unit</a>
   */
  DoubleGaugeBuilder setUnit(String unit);

  /** Sets the Gauge for recording {@code long} values. */
  LongGaugeBuilder ofLongs();

  /**
   * Builds an Asynchronous Gauge instrument with the given callback.
   *
   * <p>The callback will only be called when the instrument is being observed.
   *
   * <p>Callbacks are expected to abide by the following restrictions:
   *
   * <ul>
   *   <li>Run in a finite amount of time.
   *   <li>Safe to call repeatedly, across multiple threads.
   * </ul>
   *
   * @param callback A callback which observes measurements when invoked.
   */
  ObservableDoubleGauge buildWithCallback(Consumer<ObservableDoubleMeasurement> callback);
}
