/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.concurrent.AdderUtil;
import io.opentelemetry.sdk.metrics.internal.concurrent.DoubleAdder;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.MutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.state.Measurement;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Sum aggregator that keeps values as {@code double}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DoubleSumAggregator
    extends AbstractSumAggregator<DoublePointData, DoubleExemplarData> {
  private final Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier;
  private final MemoryMode memoryMode;

  /**
   * Constructs a sum aggregator.
   *
   * @param instrumentDescriptor The instrument being recorded, used to compute monotonicity.
   * @param reservoirSupplier Supplier of exemplar reservoirs per-stream.
   * @param memoryMode The memory mode to use.
   */
  public DoubleSumAggregator(
      InstrumentDescriptor instrumentDescriptor,
      Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier,
      MemoryMode memoryMode) {
    super(instrumentDescriptor);

    this.reservoirSupplier = reservoirSupplier;
    this.memoryMode = memoryMode;
  }

  @Override
  public AggregatorHandle<DoublePointData, DoubleExemplarData> createHandle() {
    return new Handle(reservoirSupplier.get(), memoryMode);
  }

  @Override
  public DoublePointData diff(DoublePointData previousPoint, DoublePointData currentPoint) {
    return ImmutableDoublePointData.create(
        currentPoint.getStartEpochNanos(),
        currentPoint.getEpochNanos(),
        currentPoint.getAttributes(),
        currentPoint.getValue() - previousPoint.getValue(),
        currentPoint.getExemplars());
  }

  @Override
  public void diffInPlace(DoublePointData previousReusablePoint, DoublePointData currentPoint) {
    ((MutableDoublePointData) previousReusablePoint)
        .set(
            currentPoint.getStartEpochNanos(),
            currentPoint.getEpochNanos(),
            currentPoint.getAttributes(),
            currentPoint.getValue() - previousReusablePoint.getValue(),
            currentPoint.getExemplars());
  }

  @Override
  public DoublePointData toPoint(Measurement measurement) {
    return ImmutableDoublePointData.create(
        measurement.startEpochNanos(),
        measurement.epochNanos(),
        measurement.attributes(),
        measurement.doubleValue());
  }

  @Override
  public void toPoint(Measurement measurement, DoublePointData reusablePoint) {
    ((MutableDoublePointData) reusablePoint)
        .set(
            measurement.startEpochNanos(),
            measurement.epochNanos(),
            measurement.attributes(),
            measurement.doubleValue());
  }

  @Override
  public DoublePointData createReusablePoint() {
    return new MutableDoublePointData();
  }

  @Override
  public void copyPoint(DoublePointData point, DoublePointData toReusablePoint) {
    ((MutableDoublePointData) toReusablePoint).set(point);
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor descriptor,
      Collection<DoublePointData> points,
      AggregationTemporality temporality) {
    return ImmutableMetricData.createDoubleSum(
        resource,
        instrumentationScopeInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getSourceInstrument().getUnit(),
        ImmutableSumData.create(isMonotonic(), temporality, points));
  }

  static final class Handle extends AggregatorHandle<DoublePointData, DoubleExemplarData> {
    private final DoubleAdder current = AdderUtil.createDoubleAdder();

    // Only used if memoryMode == MemoryMode.REUSABLE_DATA
    @Nullable private final MutableDoublePointData reusablePoint;

    Handle(ExemplarReservoir<DoubleExemplarData> exemplarReservoir, MemoryMode memoryMode) {
      super(exemplarReservoir);
      reusablePoint = memoryMode == MemoryMode.REUSABLE_DATA ? new MutableDoublePointData() : null;
    }

    @Override
    protected DoublePointData doAggregateThenMaybeReset(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<DoubleExemplarData> exemplars,
        boolean reset) {
      double value = reset ? this.current.sumThenReset() : this.current.sum();
      if (reusablePoint != null) {
        reusablePoint.set(startEpochNanos, epochNanos, attributes, value, exemplars);
        return reusablePoint;
      } else {
        return ImmutableDoublePointData.create(
            startEpochNanos, epochNanos, attributes, value, exemplars);
      }
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(value);
    }
  }
}
