/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;

/**
 * A mutable {@link ImmutableMeasurement} implementation, that is leased to other objects
 * temporarily.
 */
@SuppressWarnings("ExtendsAutoValue")
public class LeasedMeasurement implements Measurement {

  static void setDoubleMeasurement(
      LeasedMeasurement leasedMeasurement,
      long startEpochNanos,
      long epochNanos,
      double value,
      Attributes attributes) {
    leasedMeasurement.set(
        startEpochNanos,
        epochNanos,
        /* hasLongValue= */ false,
        0L,
        /* hasDoubleValue= */ true,
        value,
        attributes);
  }

  static void setLongMeasurement(
      LeasedMeasurement leasedMeasurement,
      long startEpochNanos,
      long epochNanos,
      long value,
      Attributes attributes) {
    leasedMeasurement.set(
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
  @Nullable
  private Attributes attributes;

  /**
   * Sets the values
   */
  public void set(
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
  @Nullable
  @Override
  public Attributes attributes() {
    return attributes;
  }
}
