/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyExponentialHistogramBuckets;
import java.util.Collections;
import java.util.List;

public class MutableExponentialHistogramPointData implements ExponentialHistogramPointData {

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
        + "startEpochNanos=" + startEpochNanos + ", "
        + "epochNanos=" + epochNanos + ", "
        + "attributes=" + attributes + ", "
        + "scale=" + scale + ", "
        + "sum=" + sum + ", "
        + "count=" + count + ", "
        + "zeroCount=" + zeroCount + ", "
        + "hasMin=" + hasMin + ", "
        + "min=" + min + ", "
        + "hasMax=" + hasMax + ", "
        + "max=" + max + ", "
        + "positiveBuckets=" + positiveBuckets + ", "
        + "negativeBuckets=" + negativeBuckets + ", "
        + "exemplars=" + exemplars
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MutableExponentialHistogramPointData) {
      MutableExponentialHistogramPointData that = (MutableExponentialHistogramPointData) o;
      return this.startEpochNanos == that.startEpochNanos
          && this.epochNanos == that.epochNanos
          && this.attributes.equals(that.attributes)
          && this.scale == that.scale
          && Double.doubleToLongBits(this.sum) == Double.doubleToLongBits(that.sum)
          && this.count == that.count
          && this.zeroCount == that.zeroCount
          && this.hasMin == that.hasMin
          && Double.doubleToLongBits(this.min) == Double.doubleToLongBits(that.min)
          && this.hasMax == that.hasMax
          && Double.doubleToLongBits(this.max) == Double.doubleToLongBits(that.max)
          && this.positiveBuckets.equals(that.positiveBuckets)
          && this.negativeBuckets.equals(that.negativeBuckets)
          && this.exemplars.equals(that.exemplars);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= (int) ((startEpochNanos >>> 32) ^ startEpochNanos);
    h$ *= 1000003;
    h$ ^= (int) ((epochNanos >>> 32) ^ epochNanos);
    h$ *= 1000003;
    h$ ^= attributes.hashCode();
    h$ *= 1000003;
    h$ ^= scale;
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(sum) >>> 32) ^ Double.doubleToLongBits(sum));
    h$ *= 1000003;
    h$ ^= (int) ((count >>> 32) ^ count);
    h$ *= 1000003;
    h$ ^= (int) ((zeroCount >>> 32) ^ zeroCount);
    h$ *= 1000003;
    h$ ^= hasMin ? 1231 : 1237;
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(min) >>> 32) ^ Double.doubleToLongBits(min));
    h$ *= 1000003;
    h$ ^= hasMax ? 1231 : 1237;
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(max) >>> 32) ^ Double.doubleToLongBits(max));
    h$ *= 1000003;
    h$ ^= positiveBuckets.hashCode();
    h$ *= 1000003;
    h$ ^= negativeBuckets.hashCode();
    h$ *= 1000003;
    h$ ^= exemplars.hashCode();
    return h$;
  }
}
