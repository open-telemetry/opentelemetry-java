/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.List;

/**
 * Builder class for {@link DoubleHistogram}.
 *
 * @since 1.10.0
 */
public interface DoubleHistogramBuilder {

  /**
   * Sets the description for this instrument.
   *
   * @param description The description.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description">Instrument
   *     Description</a>
   */
  DoubleHistogramBuilder setDescription(String description);

  /**
   * Sets the unit of measure for this instrument.
   *
   * @param unit The unit. Instrument units must be 63 or fewer ASCII characters.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit">Instrument
   *     Unit</a>
   */
  DoubleHistogramBuilder setUnit(String unit);

  /**
   * Set the explicit bucket buckets boundaries advice, which suggests the recommended set of
   * explicit bucket boundaries for this histogram.
   *
   * @param bucketBoundaries The explicit bucket boundaries advice.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-advisory-parameter-explicitbucketboundaries">Explicit
   *     bucket boundaries advisory parameter</a>
   * @since 1.32.0
   */
  default DoubleHistogramBuilder setExplicitBucketBoundariesAdvice(List<Double> bucketBoundaries) {
    return this;
  }

  /** Sets the Counter for recording {@code long} values. */
  LongHistogramBuilder ofLongs();

  /**
   * Builds and returns a Histogram instrument with the configuration.
   *
   * @return The Histogram instrument.
   */
  DoubleHistogram build();
}
