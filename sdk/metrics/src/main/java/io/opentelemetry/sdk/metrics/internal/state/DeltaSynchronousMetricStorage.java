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
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.resources.Resource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static io.opentelemetry.sdk.common.export.MemoryMode.IMMUTABLE_DATA;
import static io.opentelemetry.sdk.common.export.MemoryMode.REUSABLE_DATA;
import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.DELTA;

class DeltaSynchronousMetricStorage<T extends PointData>
    extends DefaultSynchronousMetricStorage<T> {
  private final long instrumentCreationEpochNanos;
  private final RegisteredReader registeredReader;
  private final MemoryMode memoryMode;

  private volatile AggregatorHolder<T> aggregatorHolder = new AggregatorHolder<>();
  // Only populated if memoryMode == REUSABLE_DATA
  private volatile ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>>
      previousCollectionAggregatorHandles = new ConcurrentHashMap<>();
  // Only populated if memoryMode == REUSABLE_DATA
  private final ArrayList<T> reusableResultList = new ArrayList<>();
  private final ConcurrentLinkedQueue<AggregatorHandle<T>> aggregatorHandlePool =
      new ConcurrentLinkedQueue<>();

  DeltaSynchronousMetricStorage(
      RegisteredReader registeredReader,
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      AttributesProcessor attributesProcessor,
      Clock clock,
      int maxCardinality,
      boolean enabled) {
    super(metricDescriptor, aggregator, attributesProcessor, clock, maxCardinality, enabled);
    this.instrumentCreationEpochNanos = clock.now();
    this.registeredReader = registeredReader;
    this.memoryMode = registeredReader.getReader().getMemoryMode();
  }

  @Override
  void doRecordLong(long value, Attributes attributes, Context context) {
    do {
      AggregatorHolder<T> aggregatorHolder = this.aggregatorHolder;
      DeltaAggregatorHandle<T> deltaAggregatorHandle = getDeltaAggregatorHandle(aggregatorHolder.aggregatorHandles, attributes, context);
      int recordsInProgress = deltaAggregatorHandle.activeRecordingThreads.addAndGet(2);
      if (recordsInProgress % 2 != 0) {
        deltaAggregatorHandle.activeRecordingThreads.addAndGet(-2);
      } else {
        try {
          deltaAggregatorHandle.handle.recordLong(value, attributes, context);
          break;
        } finally {
          deltaAggregatorHandle.activeRecordingThreads.addAndGet(-2);
        }
      }
    } while (true);
  }

  @Override
  void doRecordDouble(double value, Attributes attributes, Context context) {
    do {
      AggregatorHolder<T> aggregatorHolder = this.aggregatorHolder;
      DeltaAggregatorHandle<T> deltaAggregatorHandle = getDeltaAggregatorHandle(aggregatorHolder.aggregatorHandles, attributes, context);
      int recordsInProgress = deltaAggregatorHandle.activeRecordingThreads.addAndGet(2);
      if (recordsInProgress % 2 != 0) {
        deltaAggregatorHandle.activeRecordingThreads.addAndGet(-2);
      } else {
        try {
          deltaAggregatorHandle.handle.recordDouble(value, attributes, context);
          break;
        } finally {
          deltaAggregatorHandle.activeRecordingThreads.addAndGet(-2);
        }
      }
    } while (true);
  }

  private DeltaAggregatorHandle<T> getDeltaAggregatorHandle(
      ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles,
      Attributes attributes,
      Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    DeltaAggregatorHandle<T> handle = aggregatorHandles.get(attributes);
    if (handle != null) {
      return handle;
    }
    if (aggregatorHandles.size() >= maxCardinality) {
      logger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has exceeded the maximum allowed cardinality ("
              + maxCardinality
              + ").");
      // Return handle for overflow series, first checking if a handle already exists for it
      attributes = MetricStorage.CARDINALITY_OVERFLOW;
      handle = aggregatorHandles.get(attributes);
      if (handle != null) {
        return handle;
      }
    }
    // Get handle from pool if available, else create a new one.
    // Note: pooled handles (used only for delta temporality) retain their original
    // creationEpochNanos, but delta storage does not use the handle's creation time for the
    // start epoch — it uses the reader's last collect time directly in collect(). So the stale
    // creation time on a recycled handle does not affect correctness.
    AggregatorHandle<T> newHandle = maybeGetPooledAggregatorHandle();
    if (newHandle == null) {
      newHandle = aggregator.createHandle(clock.now());
    }
    DeltaAggregatorHandle<T> newDeltaHandle = new DeltaAggregatorHandle<>(newHandle);
    handle = aggregatorHandles.putIfAbsent(attributes, newDeltaHandle);
    return handle != null ? handle : newDeltaHandle;
  }

  @Nullable
  @Override
  AggregatorHandle<T> maybeGetPooledAggregatorHandle() {
    return aggregatorHandlePool.poll();
  }

  @Override
  public MetricData collect(
      Resource resource, InstrumentationScopeInfo instrumentationScopeInfo, long epochNanos) {
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles;
    AggregatorHolder<T> holder = this.aggregatorHolder;
    this.aggregatorHolder =
        (memoryMode == REUSABLE_DATA)
            ? new AggregatorHolder<>(previousCollectionAggregatorHandles)
            : new AggregatorHolder<>();

    // Increment recordsInProgress by 1, which produces an odd number acting as a signal that
    // record operations should re-read the volatile this.aggregatorHolder.
    // Repeatedly grab recordsInProgress until it is <= 1, which signals all active record
    // operations are complete.
    holder.aggregatorHandles.values().forEach(handle -> handle.activeRecordingThreads.addAndGet(1));
    holder.aggregatorHandles.values().forEach(handle -> {
      int recordsInProgress = handle.activeRecordingThreads.get();
      while (recordsInProgress > 1) {
        recordsInProgress = handle.activeRecordingThreads.get();
      }
      handle.activeRecordingThreads.addAndGet(-1);
    });
    aggregatorHandles = holder.aggregatorHandles;

    List<T> points;
    if (memoryMode == REUSABLE_DATA) {
      reusableResultList.clear();
      points = reusableResultList;
    } else {
      points = new ArrayList<>(aggregatorHandles.size());
    }

    // In DELTA aggregation temporality each Attributes is reset to 0
    // every time we perform a collection (by definition of DELTA).
    // In IMMUTABLE_DATA MemoryMode, this is accomplished by removing all aggregator handles
    // (into which the values are recorded) effectively starting from 0
    // for each recorded Attributes.
    // In REUSABLE_DATA MemoryMode, we strive for zero allocations. Since even removing
    // a key-value from a map and putting it again on next recording will cost an allocation,
    // we are keeping the aggregator handles in their map, and only reset their value once
    // we finish collecting the aggregated value from each one.
    // The SDK must adhere to keeping no more than maxCardinality unique Attributes in memory,
    // hence during collect(), when the map is at full capacity, we try to clear away unused
    // aggregator handles, so on next recording cycle using this map, there will be room for newly
    // recorded Attributes. This comes at the expanse of memory allocations. This can be avoided
    // if the user chooses to increase the maxCardinality.
    if (memoryMode == REUSABLE_DATA) {
      if (aggregatorHandles.size() >= maxCardinality) {
        aggregatorHandles.forEach(
            (attribute, handle) -> {
              if (!handle.handle.hasRecordedValues()) {
                aggregatorHandles.remove(attribute);
              }
            });
      }
    }

    // Start time for synchronous delta instruments is the time of the last collection, or if no
    // collection has yet taken place, the time the instrument was created.
    long startEpochNanos =
        registeredReader.getLastCollectEpochNanosOrDefault(instrumentCreationEpochNanos);

    // Grab aggregated points.
    aggregatorHandles.forEach(
        (attributes, handle) -> {
          if (!handle.handle.hasRecordedValues()) {
            return;
          }
          T point =
              handle.handle.aggregateThenMaybeReset(
                  startEpochNanos, epochNanos, attributes, /* reset= */ true);

          if (memoryMode == IMMUTABLE_DATA) {
            // Return the aggregator to the pool.
            // The pool is only used in DELTA temporality (since in CUMULATIVE the handler is
            // always used as it is the place accumulating the values and never resets)
            // AND only in IMMUTABLE_DATA memory mode since in REUSABLE_DATA we avoid
            // using the pool since it allocates memory internally on each put() or remove()
            aggregatorHandlePool.offer(handle.handle);
          }

          if (point != null) {
            points.add(point);
          }
        });

    // Trim pool down if needed. pool.size() will only exceed maxCardinality if new handles are
    // created during collection.
    int toDelete = aggregatorHandlePool.size() - (maxCardinality + 1);
    for (int i = 0; i < toDelete; i++) {
      aggregatorHandlePool.poll();
    }

    if (memoryMode == REUSABLE_DATA) {
      previousCollectionAggregatorHandles = aggregatorHandles;
    }

    if (points.isEmpty() || !enabled) {
      return EmptyMetricData.getInstance();
    }

    return aggregator.toMetricData(
        resource, instrumentationScopeInfo, metricDescriptor, points, DELTA);
  }

  private static class AggregatorHolder<T extends PointData> {
    private final ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles;

    private AggregatorHolder() {
      aggregatorHandles = new ConcurrentHashMap<>();
    }

    private AggregatorHolder(ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles) {
      this.aggregatorHandles = aggregatorHandles;
    }
  }

  private static final class DeltaAggregatorHandle<T extends PointData> {
    final AggregatorHandle<T> handle;
    // Uses the same even/odd protocol as the former AggregatorHolder.activeRecordingThreads,
    // but scoped to a single series instead of the entire map:
    //   - Recording threads increment by 2 before recording, decrement by 2 when done.
    //   - The collect thread increments by 1 (making the count odd) as a signal that this
    //     handle is being collected; recorders that observe an odd count release and retry.
    //   - Once all in-flight recordings finish the count returns to 1, and the collect
    //     thread decrements by 1 to restore it to even for the next cycle.
    final AtomicInteger activeRecordingThreads = new AtomicInteger(0);

    DeltaAggregatorHandle(AggregatorHandle<T> handle) {
      this.handle = handle;
    }
  }
}
