package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.resources.Resource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static io.opentelemetry.sdk.common.export.MemoryMode.REUSABLE_DATA;
import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.CUMULATIVE;

class CumulativeSynchronousMetricStorage<T extends PointData>
    extends DefaultSynchronousMetricStorage<T> {
  private final MemoryMode memoryMode;
  private final ConcurrentHashMap<Attributes, AggregatorHandle<T>> aggregatorHandles =
      new ConcurrentHashMap<>();
  // Only populated if memoryMode == REUSABLE_DATA
  private final ArrayList<T> reusableResultList = new ArrayList<>();

  CumulativeSynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      AttributesProcessor attributesProcessor,
      Clock clock,
      int maxCardinality,
      boolean enabled,
      MemoryMode memoryMode) {
    super(metricDescriptor, aggregator, attributesProcessor, clock, maxCardinality, enabled);
    this.memoryMode = memoryMode;
  }

  @Override
  void doRecordLong(long value, Attributes attributes, Context context) {
    getAggregatorHandle(aggregatorHandles, attributes, context)
        .recordLong(value, attributes, context);
  }

  @Override
  void doRecordDouble(double value, Attributes attributes, Context context) {
    getAggregatorHandle(aggregatorHandles, attributes, context)
        .recordDouble(value, attributes, context);
  }

  @Nullable
  @Override
  AggregatorHandle<T> maybeGetPooledAggregatorHandle() {
    // No aggregator handle pooling for cumulative temporality
    return null;
  }

  @Override
  public MetricData collect(
      Resource resource, InstrumentationScopeInfo instrumentationScopeInfo, long epochNanos) {
    List<T> points;
    if (memoryMode == REUSABLE_DATA) {
      reusableResultList.clear();
      points = reusableResultList;
    } else {
      points = new ArrayList<>(aggregatorHandles.size());
    }

    // Grab aggregated points.
    aggregatorHandles.forEach(
        (attributes, handle) -> {
          if (!handle.hasRecordedValues()) {
            return;
          }
          // Start time for cumulative synchronous instruments is the time the first series
          // measurement was recorded. I.e. the time the AggregatorHandle was created.
          T point =
              handle.aggregateThenMaybeReset(
                  handle.getCreationEpochNanos(), epochNanos, attributes, /* reset= */ false);

          if (point != null) {
            points.add(point);
          }
        });

    if (points.isEmpty() || !enabled) {
      return EmptyMetricData.getInstance();
    }

    return aggregator.toMetricData(
        resource, instrumentationScopeInfo, metricDescriptor, points, CUMULATIVE);
  }
}
