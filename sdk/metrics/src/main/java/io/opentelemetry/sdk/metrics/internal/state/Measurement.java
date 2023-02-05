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
 */
@AutoValue
public abstract class Measurement {

  static Measurement doubleMeasurement(
      long startEpochNanos, long epochNanos, double value, Attributes attributes) {
    return new AutoValue_Measurement(
        startEpochNanos,
        epochNanos,
        /* hasLongValue= */ false,
        0L,
        /* hasDoubleValue= */ true,
        value,
        attributes);
  }

  static Measurement longMeasurement(
      long startEpochNanos, long epochNanos, long value, Attributes attributes) {
    return new AutoValue_Measurement(
        startEpochNanos,
        epochNanos,
        /* hasLongValue= */ true,
        value,
        /* hasDoubleValue= */ false,
        0.0,
        attributes);
  }

  public abstract long startEpochNanos();

  public abstract long epochNanos();

  public abstract boolean hasLongValue();

  public abstract long longValue();

  public abstract boolean hasDoubleValue();

  public abstract double doubleValue();

  public abstract Attributes attributes();
}
