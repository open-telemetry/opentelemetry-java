/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A mutable {@link DoublePointData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>This class is not thread-safe.
 */
public class MutableDoublePointData implements DoublePointData {

  private long startEpochNanos;
  private long epochNanos;

  private Attributes attributes = Attributes.empty();

  private double value;
  private List<DoubleExemplarData> exemplars = Collections.emptyList();

  @Override
  public double getValue() {
    return value;
  }

  @Override
  public long getStartEpochNanos() {
    return startEpochNanos;
  }

  @Override
  public long getEpochNanos() {
    return epochNanos;
  }

  @Override
  public Attributes getAttributes() {
    return attributes;
  }

  @Override
  public List<DoubleExemplarData> getExemplars() {
    return exemplars;
  }

  /**
   * Sets all {@link MutableDoublePointData} values based on {@code point}.
   *
   * @param point The point to take the values from
   */
  public void set(DoublePointData point) {
    set(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point.getAttributes(),
        point.getValue(),
        point.getExemplars());
  }

  /** Sets all {@link MutableDoublePointData} values , besides exemplars which are set to empty. */
  public void set(long startEpochNanos, long epochNanos, Attributes attributes, double value) {
    set(startEpochNanos, epochNanos, attributes, value, Collections.emptyList());
  }

  /** Sets all {@link MutableDoublePointData} values. */
  public void set(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      double value,
      List<DoubleExemplarData> exemplars) {
    this.startEpochNanos = startEpochNanos;
    this.epochNanos = epochNanos;
    this.attributes = attributes;
    this.value = value;
    this.exemplars = exemplars;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DoublePointData)) {
      return false;
    }
    DoublePointData pointData = (DoublePointData) o;
    return startEpochNanos == pointData.getStartEpochNanos()
        && epochNanos == pointData.getEpochNanos()
        && Double.doubleToLongBits(value) == Double.doubleToLongBits(pointData.getValue())
        && Objects.equals(attributes, pointData.getAttributes())
        && Objects.equals(exemplars, pointData.getExemplars());
  }

  @Override
  public int hashCode() {
    int hashcode = 1;
    hashcode *= 1000003;
    hashcode ^= (int) ((startEpochNanos >>> 32) ^ startEpochNanos);
    hashcode *= 1000003;
    hashcode ^= (int) ((epochNanos >>> 32) ^ epochNanos);
    hashcode *= 1000003;
    hashcode ^= attributes.hashCode();
    hashcode *= 1000003;
    hashcode ^= (int) ((Double.doubleToLongBits(value) >>> 32) ^ Double.doubleToLongBits(value));
    hashcode *= 1000003;
    hashcode ^= exemplars.hashCode();
    return hashcode;
  }

  @Override
  public String toString() {
    return "MutableDoublePointData{"
        + "startEpochNanos="
        + startEpochNanos
        + ", epochNanos="
        + epochNanos
        + ", attributes="
        + attributes
        + ", value="
        + value
        + ", exemplars="
        + exemplars
        + '}';
  }
}
