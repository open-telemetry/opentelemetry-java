/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.datadoghq.sketch.ddsketch.DDSketch;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.data.MetricData.SketchPoint;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.function.Supplier;

@ThreadSafe
public final class DDSketchAggregator extends AbstractAggregator {

  private static final AggregatorFactory AGGREGATOR_FACTORY =
      () -> new DDSketchAggregator(() -> DDSketch.balanced(0.01));

  public static AggregatorFactory getBalancedFactory() {
    return AGGREGATOR_FACTORY;
  }

  private final Supplier<DDSketch> sketchSupplier;

  @GuardedBy("lock")
  private volatile DDSketch current;

  private final Object lock = new Object();

  private DDSketchAggregator(@Nonnull Supplier<DDSketch> sketchSupplier) {
    this.sketchSupplier = sketchSupplier;
    current = sketchSupplier.get();
    assert current != null;
  }

  @Override
  void doMergeAndReset(Aggregator target) {
    DDSketch old;
    synchronized (lock) {
      old = current;
      current = sketchSupplier.get();
    }

    ((DDSketchAggregator) target).mergeFrom(old);
  }

  private void mergeFrom(DDSketch other) {
    synchronized (lock) {
      current.mergeWith(other);
    }
  }

  @Nullable
  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
    synchronized (lock) {
      return current.getCount() > 0
          ? SketchPoint.create(startEpochNanos, epochNanos, labels, current)
          : null;
    }
  }

  @Override
  public void doRecordLong(long value) {
    synchronized (lock) {
      current.accept(value);
    }
  }
}
