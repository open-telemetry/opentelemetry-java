/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.util.Collections;
import java.util.List;

/**
 * A mutable {@link ExponentialHistogramPointData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>This class is not thread-safe.
 */
public final class MutableExponentialHistogramPointData implements ExponentialHistogramPointData {

  private long startEpochNanos;
  private long epochNanos;
  private Attributes attributes = Attributes.empty();
  private int scale;
  private double sum;
  private long count;
  private long zeroCount;
  private boolean hasMin;
  private double min;
  private boolean hasMax;
  private double max;
  private ExponentialHistogramBuckets positiveBuckets = EmptyExponentialHistogramBuckets.get(0);
  private ExponentialHistogramBuckets negativeBuckets = EmptyExponentialHistogramBuckets.get(0);
  private List<DoubleExemplarData> exemplars = Collections.emptyList();

  @Override
  public int getScale() {
    return scale;
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
  public long getZeroCount() {
    return zeroCount;
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
  public ExponentialHistogramBuckets getPositiveBuckets() {
    return positiveBuckets;
  }

  @Override
  public ExponentialHistogramBuckets getNegativeBuckets() {
    return negativeBuckets;
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

  @SuppressWarnings("TooManyParameters")
  public ExponentialHistogramPointData set(
      int scale,
      double sum,
      long zeroCount,
      boolean hasMin,
      double min,
      boolean hasMax,
      double max,
      ExponentialHistogramBuckets positiveBuckets,
      ExponentialHistogramBuckets negativeBuckets,
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<DoubleExemplarData> exemplars) {
    this.count = zeroCount + positiveBuckets.getTotalCount() + negativeBuckets.getTotalCount();
    this.scale = scale;
    this.sum = sum;
    this.zeroCount = zeroCount;
    this.hasMin = hasMin;
    this.min = min;
    this.hasMax = hasMax;
    this.max = max;
    this.positiveBuckets = positiveBuckets;
    this.negativeBuckets = negativeBuckets;
    this.startEpochNanos = startEpochNanos;
    this.epochNanos = epochNanos;
    this.attributes = attributes;
    this.exemplars = exemplars;

    return this;
  }

  @Override
  public String toString() {
    return "MutableExponentialHistogramPointData{"
        + "startEpochNanos="
        + startEpochNanos
        + ", "
        + "epochNanos="
        + epochNanos
        + ", "
        + "attributes="
        + attributes
        + ", "
        + "scale="
        + scale
        + ", "
        + "sum="
        + sum
        + ", "
        + "count="
        + count
        + ", "
        + "zeroCount="
        + zeroCount
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
        + "positiveBuckets="
        + positiveBuckets
        + ", "
        + "negativeBuckets="
        + negativeBuckets
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
    if (o instanceof ExponentialHistogramPointData) {
      ExponentialHistogramPointData that = (ExponentialHistogramPointData) o;
      return this.startEpochNanos == that.getStartEpochNanos()
          && this.epochNanos == that.getEpochNanos()
          && this.attributes.equals(that.getAttributes())
          && this.scale == that.getScale()
          && Double.doubleToLongBits(this.sum) == Double.doubleToLongBits(that.getSum())
          && this.count == that.getCount()
          && this.zeroCount == that.getZeroCount()
          && this.hasMin == that.hasMin()
          && Double.doubleToLongBits(this.min) == Double.doubleToLongBits(that.getMin())
          && this.hasMax == that.hasMax()
          && Double.doubleToLongBits(this.max) == Double.doubleToLongBits(that.getMax())
          && this.positiveBuckets.equals(that.getPositiveBuckets())
          && this.negativeBuckets.equals(that.getNegativeBuckets())
          && this.exemplars.equals(that.getExemplars());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash *= 1000003;
    hash ^= (int) ((startEpochNanos >>> 32) ^ startEpochNanos);
    hash *= 1000003;
    hash ^= (int) ((epochNanos >>> 32) ^ epochNanos);
    hash *= 1000003;
    hash ^= attributes.hashCode();
    hash *= 1000003;
    hash ^= scale;
    hash *= 1000003;
    hash ^= (int) ((Double.doubleToLongBits(sum) >>> 32) ^ Double.doubleToLongBits(sum));
    hash *= 1000003;
    hash ^= (int) ((count >>> 32) ^ count);
    hash *= 1000003;
    hash ^= (int) ((zeroCount >>> 32) ^ zeroCount);
    hash *= 1000003;
    hash ^= hasMin ? 1231 : 1237;
    hash *= 1000003;
    hash ^= (int) ((Double.doubleToLongBits(min) >>> 32) ^ Double.doubleToLongBits(min));
    hash *= 1000003;
    hash ^= hasMax ? 1231 : 1237;
    hash *= 1000003;
    hash ^= (int) ((Double.doubleToLongBits(max) >>> 32) ^ Double.doubleToLongBits(max));
    hash *= 1000003;
    hash ^= positiveBuckets.hashCode();
    hash *= 1000003;
    hash ^= negativeBuckets.hashCode();
    hash *= 1000003;
    hash ^= exemplars.hashCode();
    return hash;
  }
}
