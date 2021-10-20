/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
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
   * Constructs a histogram aggregator.
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
    private final DoubleAdder current = new DoubleAdder();

    Handle(ExemplarReservoir exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected DoubleAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
      return DoubleAccumulation.create(this.current.sumThenReset(), exemplars);
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(value);
    }
  }
}
