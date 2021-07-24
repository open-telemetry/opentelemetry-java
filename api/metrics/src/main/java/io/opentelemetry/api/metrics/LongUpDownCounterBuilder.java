/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link LongUpDownCounter}. */
public interface LongUpDownCounterBuilder {
  /**
   * Sets the description for this instrument.
   *
   * <p>Description stirngs should follw the instrument description rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description
   */
  public LongUpDownCounterBuilder setDescription(String description);
  /**
   * Set the unit of measure for this instrument.
   *
   * <p>Unit strings should follow the instrument unit rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit
   */
  public LongUpDownCounterBuilder setUnit(String unit);

  /** Sets the counter for recording {@code double} values. */
  public DoubleUpDownCounterBuilder ofDoubles();

  /**
   * Builds and returns a {@code LongCounter} with the desired options.
   *
   * @return a {@code LongCounter} with the desired options.
   */
  public LongUpDownCounter build();

  /**
   * Builds this asynchronous insturment with the given callback.
   *
   * <p>The callback will only be called when the {@link Meter} is being observed.
   *
   * @param callback A state-capturing callback used to observe values on-demand.
   */
  public void buildWithCallback(Consumer<ObservableLongMeasurement> callback);
}
