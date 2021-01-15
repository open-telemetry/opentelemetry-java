/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** The {@code Builder} class for the {@code Instrument}. */
public interface InstrumentBuilder {
  /**
   * Sets the description of the {@code Instrument}.
   *
   * <p>Default value is {@code ""}.
   *
   * @param description the description of the Instrument.
   * @return this.
   */
  InstrumentBuilder setDescription(String description);

  /**
   * Sets the unit of the {@code Instrument}.
   *
   * <p>Default value is {@code "1"}.
   *
   * @param unit the unit of the Instrument.
   * @return this.
   */
  InstrumentBuilder setUnit(String unit);

  /**
   * Builds and returns a {@code Instrument} with the desired options.
   *
   * @return a {@code Instrument} with the desired options.
   */
  Instrument build();
}
