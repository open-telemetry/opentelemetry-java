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
      DeltaAggregatorHandle<T> deltaAggregatorHandle = getDeltaAggregatorHandle(aggregatorHolder, attributes, context);
      if (deltaAggregatorHandle == null) {
        continue;
      }
      try {
        deltaAggregatorHandle.handle.recordLong(value, attributes, context);
        break;
      } finally {
        deltaAggregatorHandle.activeRecordingThreads.addAndGet(-2);
      }
    } while (true);
  }

  @Override
  void doRecordDouble(double value, Attributes attributes, Context context) {
    do {
      AggregatorHolder<T> aggregatorHolder = this.aggregatorHolder;
      DeltaAggregatorHandle<T> deltaAggregatorHandle = getDeltaAggregatorHandle(aggregatorHolder, attributes, context);
      if (deltaAggregatorHandle == null) {
        continue;
      }
      try {
        deltaAggregatorHandle.handle.recordDouble(value, attributes, context);
        break;
      } finally {
        deltaAggregatorHandle.activeRecordingThreads.addAndGet(-2);
      }
    } while (true);
  }

  @Nullable
  protected DeltaAggregatorHandle<T> getDeltaAggregatorHandle(
      AggregatorHolder<T> holder,
      Attributes attributes,
      Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles = holder.aggregatorHandles;
    DeltaAggregatorHandle<T> handle = aggregatorHandles.get(attributes);
    if (handle == null && aggregatorHandles.size() >= maxCardinality) {
      logger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has exceeded the maximum allowed cardinality ("
              + maxCardinality
              + ").");
      attributes = MetricStorage.CARDINALITY_OVERFLOW;
      handle = aggregatorHandles.get(attributes);
    }
    if (handle != null) {
      // Existing series: pre-increment the per-handle counter and check if odd (locked by the
      // collect thread's lock pass).
      int count = handle.activeRecordingThreads.addAndGet(2);
      if (count % 2 != 0) {
        handle.activeRecordingThreads.addAndGet(-2);
        return null; // handle is being collected; caller should retry with new holder
      }
      // Also check the holder-level counter. The collect thread sets it to 1 (odd) and never
      // resets it. This catches the window after the collect thread's wait-pass decrements the
      // per-handle counter back to 0 (even) but before collection finishes: a stale thread that
      // read the old holder can still reach here with an even per-handle count. The hb chain
      // (CT's holder lock → CT's wait-pass decrement → this addAndGet(2)) guarantees we see
      // the holder counter as odd at that point.
      if (holder.activeRecordingThreads.get() % 2 != 0) {
        handle.activeRecordingThreads.addAndGet(-2);
        return null; // holder is being collected; caller should retry with new holder
      }
      return handle;
    }
    // New series: use the holder-level gate to coordinate with the collect thread.
    // The gate ensures (a) we don't insert into a holder whose lock pass has already run,
    // and (b) the per-handle pre-increment below is visible to the collect thread's lock pass.
    int holderCount = holder.activeRecordingThreads.addAndGet(2);
    if (holderCount % 2 != 0) {
      holder.activeRecordingThreads.addAndGet(-2);
      return null; // holder is being collected; caller should retry with new holder
    }
    try {
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
      if (handle == null) {
        handle = newDeltaHandle;
      }
      // Pre-increment per-handle counter while the holder gate is still held. The collect
      // thread's lock pass cannot start until all threads release the holder gate, so this
      // increment is guaranteed to be observed by the lock pass before it runs.
      handle.activeRecordingThreads.addAndGet(2);
      return handle;
    } finally {
      holder.activeRecordingThreads.addAndGet(-2);
    }
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

    // Lock out new series creation in the old holder by making its activeRecordingThreads odd,
    // then wait until it equals 1, meaning no new-series creation is in flight.
    // This guarantees the per-handle lock pass below sees every handle that will ever be
    // inserted into holder.aggregatorHandles.
    int holderRecordingThreads = holder.activeRecordingThreads.addAndGet(1);
    while (holderRecordingThreads != 1) {
      holderRecordingThreads = holder.activeRecordingThreads.get();
    }

    // Increment per-handle recordsInProgress by 1, which produces an odd number acting as a
    // signal that record operations should re-read the volatile this.aggregatorHolder.
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
    // Used as a gate for new-series creation (not for per-handle recording contention).
    // Recording threads creating a new series increment by 2; the collect thread increments
    // by 1 to lock out new-series creation and waits for the value to return to 1.
    private final AtomicInteger activeRecordingThreads = new AtomicInteger(0);

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
