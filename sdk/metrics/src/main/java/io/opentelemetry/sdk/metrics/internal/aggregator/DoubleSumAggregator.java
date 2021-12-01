/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Sum aggregator that keeps values as {@code double}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DoubleSumAggregator extends AbstractSumAggregator<DoubleAccumulation> {
  private final Supplier<ExemplarReservoir> reservoirSupplier;

  /**
   * Constructs a sum aggregator.
   *
   * @param instrumentDescriptor The instrument being recorded, used to compute monotonicity.
   * @param reservoirSupplier Supplier of exemplar reservoirs per-stream.
   */
  public DoubleSumAggregator(
      InstrumentDescriptor instrumentDescriptor, Supplier<ExemplarReservoir> reservoirSupplier) {
    super(instrumentDescriptor);

    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<DoubleAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public DoubleAccumulation accumulateDoubleMeasurement(
      double value, Attributes attributes, Context context) {
    return DoubleAccumulation.create(value);
  }

  @Override
  public DoubleAccumulation merge(
      DoubleAccumulation previousAccumulation, DoubleAccumulation accumulation) {
    return DoubleAccumulation.create(
        previousAccumulation.getValue() + accumulation.getValue(), accumulation.getExemplars());
  }

  @Override
  public DoubleAccumulation diff(
      DoubleAccumulation previousAccumulation, DoubleAccumulation accumulation) {
    return DoubleAccumulation.create(
        accumulation.getValue() - previousAccumulation.getValue(), accumulation.getExemplars());
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor descriptor,
      Map<Attributes, DoubleAccumulation> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createDoubleSum(
        resource,
        instrumentationLibraryInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getUnit(),
        DoubleSumData.create(
            isMonotonic(),
            temporality,
            MetricDataUtils.toDoublePointList(
                accumulationByLabels,
                temporality == AggregationTemporality.CUMULATIVE
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos)));
  }

  static final class Handle extends AggregatorHandle<DoubleAccumulation> {
    private final AtomicLong current = new AtomicLong();

    Handle(ExemplarReservoir exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected DoubleAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
      return DoubleAccumulation.create(getAndSet(current, 0), exemplars);
    }

    @Override
    protected void doRecordDouble(double value) {
      addAndGet(current, value);
    }

    /**
     * Helper to replicate {@code double getAndSet(double)} functionality on an {@link AtomicLong}
     * being used to store a double.
     *
     * <p>Heavily influenced by {@code com.google.common.util.concurrent.AtomicDouble}.
     *
     * @param atomicLong an atomic long being used to store a double via {@link
     *     Double#doubleToLongBits(double)}
     * @param newValue the new value
     * @return the previous value
     */
    private static double getAndSet(AtomicLong atomicLong, double newValue) {
      long nextLongBits = Double.doubleToLongBits(newValue);
      long prev;
      do {
        prev = atomicLong.get();
      } while (!atomicLong.compareAndSet(prev, nextLongBits));
      return Double.longBitsToDouble(prev);
    }

    /**
     * Helper to replicate {@code double addAndGet(double)} functionality on an {@link AtomicLong}
     * being used to store a double.
     *
     * <p>Heavily influenced by {@code com.google.common.util.concurrent.AtomicDouble}.
     *
     * @param atomicLong an atomic long being used to store a double via {@link
     *     Double#doubleToLongBits(double)}
     * @param delta the value to add
     * @return the updated value
     */
    private static double addAndGet(AtomicLong atomicLong, double delta) {
      while (true) {
        long currentLongBits = atomicLong.get();
        double currentDouble = Double.longBitsToDouble(currentLongBits);
        double nextDouble = currentDouble + delta;
        long nextLongBits = Double.doubleToLongBits(nextDouble);
        if (atomicLong.compareAndSet(currentLongBits, nextLongBits)) {
          return nextDouble;
        }
      }
    }
  }
}
