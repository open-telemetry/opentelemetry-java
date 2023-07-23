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
public abstract class ImmutableMeasurement implements Measurement {

  static ImmutableMeasurement doubleMeasurement(
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

  static ImmutableMeasurement longMeasurement(
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
}
