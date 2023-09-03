/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;

/**
 * A mutable {@link Measurement} implementation
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>This class is not thread-safe.
 */
public class MutableMeasurement implements Measurement {

  static void setDoubleMeasurement(
      MutableMeasurement mutableMeasurement,
      long startEpochNanos,
      long epochNanos,
      double value,
      Attributes attributes) {
    mutableMeasurement.set(
        startEpochNanos,
        epochNanos,
        /* hasLongValue= */ false,
        0L,
        /* hasDoubleValue= */ true,
        value,
        attributes);
  }

  static void setLongMeasurement(
      MutableMeasurement mutableMeasurement,
      long startEpochNanos,
      long epochNanos,
      long value,
      Attributes attributes) {
    mutableMeasurement.set(
        startEpochNanos,
        epochNanos,
        /* hasLongValue= */ true,
        value,
        /* hasDoubleValue= */ false,
        0.0,
        attributes);
  }

  private long startEpochNanos;
  private long epochNanos;
  private boolean hasLongValue;
  private long longValue;
  private boolean hasDoubleValue;
  private double doubleValue;

  @Nullable private Attributes attributes;

  /** Sets the values. */
  private void set(
      long startEpochNanos,
      long epochNanos,
      boolean hasLongValue,
      long longValue,
      boolean hasDoubleValue,
      double doubleValue,
      Attributes attributes) {
    this.startEpochNanos = startEpochNanos;
    this.epochNanos = epochNanos;
    this.hasLongValue = hasLongValue;
    this.longValue = longValue;
    this.hasDoubleValue = hasDoubleValue;
    this.doubleValue = doubleValue;
    this.attributes = attributes;
  }

  @Override
  public Measurement withStartEpochNanos(long startEpochNanos) {
    this.startEpochNanos = startEpochNanos;
    return this;
  }

  @Override
  public Measurement withAttributes(Attributes attributes) {
    this.attributes = attributes;
    return this;
  }

  @Override
  public long startEpochNanos() {
    return startEpochNanos;
  }

  @Override
  public long epochNanos() {
    return epochNanos;
  }

  @Override
  public boolean hasLongValue() {
    return hasLongValue;
  }

  @Override
  public long longValue() {
    return longValue;
  }

  @Override
  public boolean hasDoubleValue() {
    return hasDoubleValue;
  }

  @Override
  public double doubleValue() {
    return doubleValue;
  }

  @SuppressWarnings("NullAway")
  @Override
  public Attributes attributes() {
    return attributes;
  }
}
