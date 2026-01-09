/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A mutable {@link LongPointData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>This class is not thread-safe.
 */
public class MutableLongPointData implements LongPointData {

  private long value;
  private long startEpochNanos;
  private long epochNanos;
  private Attributes attributes = Attributes.empty();
  private List<LongExemplarData> exemplars = Collections.emptyList();

  @Override
  public long getValue() {
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
  public List<LongExemplarData> getExemplars() {
    return exemplars;
  }

  /**
   * Sets all {@link MutableDoublePointData} based on {@code point}.
   *
   * @param point The point to set values upon
   */
  public void set(LongPointData point) {
    set(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point.getAttributes(),
        point.getValue(),
        point.getExemplars());
  }

  /** Sets all {@link MutableDoublePointData} values besides exemplars which are set to be empty. */
  public void set(long startEpochNanos, long epochNanos, Attributes attributes, long value) {
    set(startEpochNanos, epochNanos, attributes, value, Collections.emptyList());
  }

  /** Sets all {@link MutableDoublePointData} values. */
  public void set(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      long value,
      List<LongExemplarData> exemplars) {
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
    if (!(o instanceof LongPointData)) {
      return false;
    }
    LongPointData that = (LongPointData) o;
    return value == that.getValue()
        && startEpochNanos == that.getStartEpochNanos()
        && epochNanos == that.getEpochNanos()
        && Objects.equals(attributes, that.getAttributes())
        && Objects.equals(exemplars, that.getExemplars());
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
    hashcode ^= (int) ((value >>> 32) ^ value);
    hashcode *= 1000003;
    hashcode ^= exemplars.hashCode();
    return hashcode;
  }

  @Override
  public String toString() {
    return "MutableLongPointData{"
        + "value="
        + value
        + ", startEpochNanos="
        + startEpochNanos
        + ", epochNanos="
        + epochNanos
        + ", attributes="
        + attributes
        + ", exemplars="
        + exemplars
        + '}';
  }
}
