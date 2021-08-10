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
   * <p>Description stirngs should follw the instrument description rules:
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

  /** Sets the counter for recording {@code long} values. */
  LongCounterBuilder ofLongs();

  /**
   * Builds and returns a {@code DoubleCounter} with the desired options.
   *
   * @return a {@code DoubleCounter} with the desired options.
   */
  DoubleCounter build();

  /**
   * Builds this asynchronous insturment with the given callback.
   *
   * <p>The callback will only be called when the {@link Meter} is being observed.
   *
   * @param callback A state-capturing callback used to observe values on-demand.
   */
  void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback);
}
