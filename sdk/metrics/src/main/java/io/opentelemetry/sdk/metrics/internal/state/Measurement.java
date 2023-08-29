/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;

public interface Measurement {
  long startEpochNanos();

  long epochNanos();

  boolean hasLongValue();

  long longValue();

  boolean hasDoubleValue();

  double doubleValue();

  Attributes attributes();

  /**
   * Updates the attributes.
   *
   * @param attributes The attributes to update
   * @return The updated object. For {@link ImmutableMeasurement} it will be a new object with the
   *     updated attributes and for {@link LeasedMeasurement} it will return itself with the
   *     attributes updated
   */
  Measurement withAttributes(Attributes attributes);

  /**
   * Updates the startEpochNanos.
   *
   * @param startEpochNanos start epoch nanosecond
   * @return The updated object. For {@link ImmutableMeasurement} it will be a new object with the
   *     updated startEpochNanos and for {@link LeasedMeasurement} it will return itself with the
   *     startEpochNanos updated
   */
  Measurement withStartEpochNanos(long startEpochNanos);
}
