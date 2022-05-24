/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link DoubleUpDownCounter}. */
public interface DoubleUpDownCounterBuilder {
  /**
   * Sets the description for this instrument.
   *
   * @param description The description.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description">Instrument
   *     Description</a>
   */
  DoubleUpDownCounterBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * @param unit The unit. Instrument units must be 63 or fewer ASCII characters.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit">Instrument
   *     Unit</a>
   */
  DoubleUpDownCounterBuilder setUnit(String unit);

  /**
   * Builds and returns an up down counter instrument with the configuration.
   *
   * @return The up down counter instrument.
   */
  DoubleUpDownCounter build();

  /**
   * Builds an asynchronous up down counter instrument with the given callback.
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
  ObservableDoubleUpDownCounter buildWithCallback(Consumer<ObservableDoubleMeasurement> callback);
}
