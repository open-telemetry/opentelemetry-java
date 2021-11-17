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
   * <p>Description strings should follow the instrument description rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description
   */
  DoubleUpDownCounterBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * <p>Unit strings should follow the instrument unit rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit
   */
  DoubleUpDownCounterBuilder setUnit(String unit);

  /**
   * Builds and returns a {@code DoubleUpDownCounter} with the desired options.
   *
   * @return a {@code DoubleUpDownCounter} with the desired options.
   */
  DoubleUpDownCounter build();

  /**
   * Builds this asynchronous instrument with the given callback.
   *
   * <p>The callback will only be called when the {@link Meter} is being observed.
   *
   * @param callback A state-capturing callback used to observe values on-demand.
   */
  void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback);
}
