/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** Builder class for {@link DoubleHistogram}. */
public interface DoubleHistogramBuilder {

  /**
   * Sets the description for this instrument.
   *
   * <p>Description strings should follow the instrument description rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description
   */
  DoubleHistogramBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * <p>Unit strings should follow the instrument unit rules:
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit
   */
  DoubleHistogramBuilder setUnit(String unit);

  /** Sets the counter for recording {@code long} values. */
  LongHistogramBuilder ofLongs();

  /**
   * Builds and returns a {@code DoubleHistogram} with the desired options.
   *
   * @return a {@code DoubleHistogram} with the desired options.
   */
  DoubleHistogram build();
}
