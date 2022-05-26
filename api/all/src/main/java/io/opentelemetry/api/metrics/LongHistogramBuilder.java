/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/**
 * Builder class for {@link LongHistogram}.
 *
 * @since 1.10.0
 */
public interface LongHistogramBuilder {
  /**
   * Sets the description for this instrument.
   *
   * @param description The description.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description">Instrument
   *     Description</a>
   */
  LongHistogramBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * @param unit The unit. Instrument units must be 63 or fewer ASCII characters.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit">Instrument
   *     Unit</a>
   */
  LongHistogramBuilder setUnit(String unit);

  /**
   * Builds and returns a Histogram instrument with the configuration.
   *
   * @return The Histogram instrument.
   */
  LongHistogram build();
}
