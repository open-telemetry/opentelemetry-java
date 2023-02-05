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
import javax.annotation.Nullable;

/**
 * Auto value implementation of {@link ExponentialHistogramPointData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class MutableExponentialHistogramPointData implements ExponentialHistogramPointData {

  private int scale;
  private double sum;
  private long zeroCount;
  private boolean hasMin;
  private double min;
  private boolean hasMax;
  private double max;
  @Nullable private ExponentialHistogramBuckets positiveBuckets;
  @Nullable private ExponentialHistogramBuckets negativeBuckets;
  private long startEpochNanos;
  private long epochNanos;
  private Attributes attributes = Attributes.empty();
  private List<DoubleExemplarData> exemplars = Collections.emptyList();

  public MutableExponentialHistogramPointData() {}

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
    if (positiveBuckets == null || negativeBuckets == null) {
      throw new IllegalStateException("Set not called");
    }
    return zeroCount + positiveBuckets.getTotalCount() + negativeBuckets.getTotalCount();
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
    if (positiveBuckets == null) {
      throw new IllegalStateException("Set not called");
    }
    return positiveBuckets;
  }

  @Override
  public ExponentialHistogramBuckets getNegativeBuckets() {
    if (negativeBuckets == null) {
      throw new IllegalStateException("Set not called");
    }
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

  /** Set the values. */
  @SuppressWarnings("TooManyParameters")
  public void set(
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
  }
}
