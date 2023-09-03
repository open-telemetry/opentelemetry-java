/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;

/**
 * A long or double measurement recorded from {@link ObservableLongMeasurement} or {@link
 * ObservableDoubleMeasurement}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
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
   *     updated attributes and for {@link MutableMeasurement} it will return itself with the
   *     attributes updated
   */
  Measurement withAttributes(Attributes attributes);

  /**
   * Updates the startEpochNanos.
   *
   * @param startEpochNanos start epoch nanosecond
   * @return The updated object. For {@link ImmutableMeasurement} it will be a new object with the
   *     updated startEpochNanos and for {@link MutableMeasurement} it will return itself with the
   *     startEpochNanos updated
   */
  Measurement withStartEpochNanos(long startEpochNanos);
}
