/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.concurrent.AdderUtil;
import io.opentelemetry.sdk.metrics.internal.concurrent.LongAdder;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.state.Measurement;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sum aggregator that keeps values as {@code long}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LongSumAggregator
    extends AbstractSumAggregator<LongPointData, LongExemplarData> {

  private final Supplier<ExemplarReservoir<LongExemplarData>> reservoirSupplier;

  public LongSumAggregator(
      InstrumentDescriptor instrumentDescriptor,
      Supplier<ExemplarReservoir<LongExemplarData>> reservoirSupplier) {
    super(instrumentDescriptor);
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<LongPointData, LongExemplarData> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public LongPointData diff(LongPointData previousPoint, LongPointData currentPoint) {
    return ImmutableLongPointData.create(
        currentPoint.getStartEpochNanos(),
        currentPoint.getEpochNanos(),
        currentPoint.getAttributes(),
        currentPoint.getValue() - previousPoint.getValue(),
        currentPoint.getExemplars());
  }

  @Override
  public LongPointData toPoint(Measurement measurement) {
    return ImmutableLongPointData.create(
        measurement.startEpochNanos(),
        measurement.epochNanos(),
        measurement.attributes(),
        measurement.longValue());
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor descriptor,
      Collection<LongPointData> points,
      AggregationTemporality temporality) {
    return ImmutableMetricData.createLongSum(
        resource,
        instrumentationScopeInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getSourceInstrument().getUnit(),
        ImmutableSumData.create(isMonotonic(), temporality, points));
  }

  static final class Handle extends AggregatorHandle<LongPointData, LongExemplarData> {
    private final LongAdder current = AdderUtil.createLongAdder();

    Handle(ExemplarReservoir<LongExemplarData> exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected LongPointData doAggregateThenMaybeReset(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<LongExemplarData> exemplars,
        boolean reset) {
      long value = reset ? this.current.sumThenReset() : this.current.sum();
      return ImmutableLongPointData.create(
          startEpochNanos, epochNanos, attributes, value, exemplars);
    }

    @Override
    public void doRecordLong(long value) {
      current.add(value);
    }
  }
}
