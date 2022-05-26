/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/**
 * Builder class for {@link DoubleUpDownCounter}.
 *
 * @since 1.10.0
 */
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
   * Builds and returns an UpDownCounter instrument with the configuration.
   *
   * @return The UpDownCounter instrument.
   */
  DoubleUpDownCounter build();

  /**
   * Builds an Asynchronous UpDownCounter instrument with the given callback.
   *
   * <p>The callback will be called when the instrument is being observed.
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

  /**
   * Build an observer for this instrument to observe values from a {@link BatchCallback}.
   *
   * <p>This observer MUST be registered when creating a {@link Meter#batchCallback(Runnable,
   * ObservableMeasurement, ObservableMeasurement...) batchCallback}, which records to it. Values
   * observed outside registered callbacks are ignored.
   *
   * @return an observable measurement that batch callbacks use to observe values.
   * @since 1.15.0
   */
  default ObservableDoubleMeasurement buildObserver() {
    return DefaultMeter.getInstance().upDownCounterBuilder("noop").ofDoubles().buildObserver();
  }
}
