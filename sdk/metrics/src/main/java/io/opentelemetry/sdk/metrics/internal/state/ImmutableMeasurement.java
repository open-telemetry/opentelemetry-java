/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import com.google.auto.value.AutoValue;
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
@AutoValue
public abstract class ImmutableMeasurement implements Measurement {

  static ImmutableMeasurement createDouble(
      long startEpochNanos, long epochNanos, double value, Attributes attributes) {
    return new AutoValue_ImmutableMeasurement(
        startEpochNanos,
        epochNanos,
        /* hasLongValue= */ false,
        0L,
        /* hasDoubleValue= */ true,
        value,
        attributes);
  }

  static ImmutableMeasurement createLong(
      long startEpochNanos, long epochNanos, long value, Attributes attributes) {
    return new AutoValue_ImmutableMeasurement(
        startEpochNanos,
        epochNanos,
        /* hasLongValue= */ true,
        value,
        /* hasDoubleValue= */ false,
        0.0,
        attributes);
  }

  @Override
  public Measurement withAttributes(Attributes attributes) {
    if (hasDoubleValue()) {
      return createDouble(startEpochNanos(), epochNanos(), doubleValue(), attributes);
    } else {
      return createLong(startEpochNanos(), epochNanos(), longValue(), attributes);
    }
  }

  @Override
  public Measurement withStartEpochNanos(long startEpochNanos) {
    if (hasDoubleValue()) {
      return createDouble(startEpochNanos, epochNanos(), doubleValue(), attributes());
    } else {
      return createLong(startEpochNanos, epochNanos(), longValue(), attributes());
    }
  }
}
