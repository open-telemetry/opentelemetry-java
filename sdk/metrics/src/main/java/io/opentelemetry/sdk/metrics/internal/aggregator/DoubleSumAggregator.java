/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.concurrent.AdderUtil;
import io.opentelemetry.sdk.metrics.internal.concurrent.DoubleAdder;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Sum aggregator that keeps values as {@code double}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DoubleSumAggregator
    extends AbstractSumAggregator<DoubleAccumulation, DoubleExemplarData> {
  private final Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier;

  /**
   * Constructs a sum aggregator.
   *
   * @param instrumentDescriptor The instrument being recorded, used to compute monotonicity.
   * @param reservoirSupplier Supplier of exemplar reservoirs per-stream.
   */
  public DoubleSumAggregator(
      InstrumentDescriptor instrumentDescriptor,
      Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier) {
    super(instrumentDescriptor);

    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<DoubleAccumulation, DoubleExemplarData> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public DoubleAccumulation accumulateDoubleMeasurement(
      double value, Attributes attributes, Context context) {
    return DoubleAccumulation.create(value);
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
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor descriptor,
      Map<Attributes, DoubleAccumulation> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return ImmutableMetricData.createDoubleSum(
        resource,
        instrumentationScopeInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getSourceInstrument().getUnit(),
        ImmutableSumData.create(
            isMonotonic(),
            temporality,
            MetricDataUtils.toDoublePointList(
                accumulationByLabels,
                temporality == AggregationTemporality.CUMULATIVE
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos)));
  }

  static final class Handle extends AggregatorHandle<DoubleAccumulation, DoubleExemplarData> {
    private final DoubleAdder current = AdderUtil.createDoubleAdder();

    Handle(ExemplarReservoir<DoubleExemplarData> exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected DoubleAccumulation doAccumulateThenMaybeReset(
        List<DoubleExemplarData> exemplars, boolean reset) {
      if (reset) {
        return DoubleAccumulation.create(this.current.sumThenReset(), exemplars);
      }
      return DoubleAccumulation.create(this.current.sum(), exemplars);
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(value);
    }
  }
}
