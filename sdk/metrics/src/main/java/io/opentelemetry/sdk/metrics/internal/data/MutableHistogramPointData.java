/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import static io.opentelemetry.sdk.metrics.internal.data.HistogramPointDataValidations.validateFiniteBoundaries;
import static io.opentelemetry.sdk.metrics.internal.data.HistogramPointDataValidations.validateIsStrictlyIncreasing;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.internal.DynamicPrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import java.util.Collections;
import java.util.List;

/**
 * A mutable {@link HistogramPointData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>This class is not thread-safe.
 */
public final class MutableHistogramPointData implements HistogramPointData {
  private long startEpochNanos;
  private long epochNanos;
  private Attributes attributes = Attributes.empty();
  private double sum;
  private long count;
  private boolean hasMin;
  private double min;
  private boolean hasMax;
  private double max;
  private List<Double> boundaries = Collections.emptyList();
  private final DynamicPrimitiveLongList counts;
  private List<DoubleExemplarData> exemplars = Collections.emptyList();

  public MutableHistogramPointData(int buckets) {
    this.counts = DynamicPrimitiveLongList.ofSubArrayCapacity(buckets);
    this.counts.resizeAndClear(buckets);
  }

  @SuppressWarnings({"TooManyParameters", "ForLoopReplaceableByForEach"})
  public MutableHistogramPointData set(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      double sum,
      boolean hasMin,
      double min,
      boolean hasMax,
      double max,
      List<Double> boundaries,
      long[] counts,
      List<DoubleExemplarData> exemplars) {

    if (this.counts.size() != boundaries.size() + 1) {
      throw new IllegalArgumentException(
          "invalid boundaries: size should be "
              + (this.counts.size() - 1)
              + " but was "
              + boundaries.size());
    }
    if (this.counts.size() != counts.length) {
      throw new IllegalArgumentException(
          "invalid counts: size should be " + this.counts.size() + " but was " + counts.length);
    }
    validateIsStrictlyIncreasing(boundaries);
    validateFiniteBoundaries(boundaries);

    long totalCount = 0;
    for (int i = 0; i < counts.length; i++) {
      totalCount += counts[i];
    }

    this.startEpochNanos = startEpochNanos;
    this.epochNanos = epochNanos;
    this.attributes = attributes;
    this.sum = sum;
    this.count = totalCount;
    this.hasMin = hasMin;
    this.min = min;
    this.hasMax = hasMax;
    this.max = max;
    this.boundaries = boundaries;
    for (int i = 0; i < counts.length; i++) {
      this.counts.setLong(i, counts[i]);
    }
    this.exemplars = exemplars;

    return this;
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
  public double getSum() {
    return sum;
  }

  @Override
  public long getCount() {
    return count;
  }

  @Override
  public boolean hasMin() {
    return hasMin;
  }

  @Override
  public double getMin() {
    return min;
  }

  @Override
  public boolean hasMax() {
    return hasMax;
  }

  @Override
  public double getMax() {
    return max;
  }

  @Override
  public List<Double> getBoundaries() {
    return boundaries;
  }

  @Override
  public List<Long> getCounts() {
    return counts;
  }

  @Override
  public List<DoubleExemplarData> getExemplars() {
    return exemplars;
  }

  @Override
  public String toString() {
    return "MutableHistogramPointData{"
        + "startEpochNanos="
        + startEpochNanos
        + ", "
        + "epochNanos="
        + epochNanos
        + ", "
        + "attributes="
        + attributes
        + ", "
        + "sum="
        + sum
        + ", "
        + "count="
        + count
        + ", "
        + "hasMin="
        + hasMin
        + ", "
        + "min="
        + min
        + ", "
        + "hasMax="
        + hasMax
        + ", "
        + "max="
        + max
        + ", "
        + "boundaries="
        + boundaries
        + ", "
        + "counts="
        + counts
        + ", "
        + "exemplars="
        + exemplars
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof HistogramPointData) {
      HistogramPointData that = (HistogramPointData) o;
      return this.startEpochNanos == that.getStartEpochNanos()
          && this.epochNanos == that.getEpochNanos()
          && this.attributes.equals(that.getAttributes())
          && Double.doubleToLongBits(this.sum) == Double.doubleToLongBits(that.getSum())
          && this.count == that.getCount()
          && this.hasMin == that.hasMin()
          && Double.doubleToLongBits(this.min) == Double.doubleToLongBits(that.getMin())
          && this.hasMax == that.hasMax()
          && Double.doubleToLongBits(this.max) == Double.doubleToLongBits(that.getMax())
          && this.boundaries.equals(that.getBoundaries())
          && this.counts.equals(that.getCounts())
          && this.exemplars.equals(that.getExemplars());
    }
    return false;
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
    hashcode ^= (int) ((Double.doubleToLongBits(sum) >>> 32) ^ Double.doubleToLongBits(sum));
    hashcode *= 1000003;
    hashcode ^= (int) ((count >>> 32) ^ count);
    hashcode *= 1000003;
    hashcode ^= hasMin ? 1231 : 1237;
    hashcode *= 1000003;
    hashcode ^= (int) ((Double.doubleToLongBits(min) >>> 32) ^ Double.doubleToLongBits(min));
    hashcode *= 1000003;
    hashcode ^= hasMax ? 1231 : 1237;
    hashcode *= 1000003;
    hashcode ^= (int) ((Double.doubleToLongBits(max) >>> 32) ^ Double.doubleToLongBits(max));
    hashcode *= 1000003;
    hashcode ^= boundaries.hashCode();
    hashcode *= 1000003;
    hashcode ^= counts.hashCode();
    hashcode *= 1000003;
    hashcode ^= exemplars.hashCode();
    return hashcode;
  }
}
