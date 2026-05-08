/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.common.export.MemoryMode.IMMUTABLE_DATA;
import static io.opentelemetry.sdk.common.export.MemoryMode.REUSABLE_DATA;
import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.DELTA;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue; // pool only
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.annotation.Nullable;

class DeltaSynchronousMetricStorage<T extends PointData>
    extends DefaultSynchronousMetricStorage<T> {
  private final long instrumentCreationEpochNanos;
  private final RegisteredReader registeredReader;
  private final MemoryMode memoryMode;

  private volatile AggregatorHolder<T> aggregatorHolder = new AggregatorHolder<>();
  // Only used when memoryMode == REUSABLE_DATA. Alternates with the current holder's map so
  // that new recordings never race with an in-progress collection on the same accumulator.
  private volatile ConcurrentHashMap<Attributes, AggregatorHandle<T>>
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
    AggregatorHandle<T> handle = acquireHandleForRecord(attributes, context);
    try {
      handle.recordLong(value, attributes, context);
    } finally {
      handle.releaseRecord();
    }
  }

  @Override
  void doRecordDouble(double value, Attributes attributes, Context context) {
    AggregatorHandle<T> handle = acquireHandleForRecord(attributes, context);
    try {
      handle.recordDouble(value, attributes, context);
    } finally {
      handle.releaseRecord();
    }
  }

  private AggregatorHandle<T> acquireHandleForRecord(Attributes attributes, Context context) {
    while (true) {
      AggregatorHandle<T> handle = getOrCreateHandle(this.aggregatorHolder, attributes, context);
      if (handle != null) {
        return handle;
      }
    }
  }

  @Nullable
  protected AggregatorHandle<T> getOrCreateHandle(
      AggregatorHolder<T> holder, Attributes attributes, Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    ConcurrentHashMap<Attributes, AggregatorHandle<T>> aggregatorHandles = holder.aggregatorHandles;
    AggregatorHandle<T> handle = aggregatorHandles.get(attributes);
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
      // Existing series: try to acquire a recording slot. Returns false if the collector has
      // locked this handle (odd state), meaning we should retry with the new holder.
      if (!handle.tryAcquireForRecord()) {
        return null;
      }
      // Also check the holder-level gate. The collect thread sets it to locked (odd) and never
      // resets it. This catches the window after the collect thread's awaitRecordersAndUnlock()
      // decrements the per-handle state back to even but before collection finishes: a stale
      // thread that read the old holder can still reach here with an even per-handle state. The
      // hb chain (CT's holder lock → CT's awaitRecordersAndUnlock() decrement → this
      // tryAcquireForRecord) guarantees we see the holder gate as locked at that point.
      if (holder.isLockedForCollect()) {
        handle.releaseRecord();
        return null;
      }
      return handle;
    }
    // New series: acquire the holder gate to coordinate with the collect thread.
    // The gate ensures (a) we don't insert into a holder whose lock pass has already run,
    // and (b) the per-handle pre-increment below is visible to the collect thread's lock pass.
    if (!holder.tryAcquireForNewSeries()) {
      return null;
    }
    try {
      // Get handle from pool if available, else create a new one.
      // Note: pooled handles retain their original creationEpochNanos, but delta storage does not
      // use the handle's creation time for the start epoch — it uses the reader's last collect time
      // directly in collect(). So the stale creation time on a recycled handle does not affect
      // correctness.
      AggregatorHandle<T> newHandle = aggregatorHandlePool.poll();
      if (newHandle == null) {
        newHandle = aggregator.createHandle(clock.now());
        newHandle.initDelta();
      }
      // Always update attributes: fresh handles need them set for the first time; pooled handles
      // retain old attributes from their previous series and must be updated for the new one.
      newHandle.setAttributes(attributes);
      handle = aggregatorHandles.putIfAbsent(attributes, newHandle);
      if (handle == null) {
        handle = newHandle;
      }
      // Pre-increment per-handle state while the holder gate is still held. The collect
      // thread's lock pass cannot start until all threads release the holder gate, so this
      // increment is guaranteed to be observed by the lock pass before it runs.
      handle.acquireForRecord();
      return handle;
    } finally {
      holder.releaseNewSeries();
    }
  }

  @Override
  public AggregatorHandle<T> bind(Attributes attributes) {
    // Get or create the handle in the current holder's map (same coordination as a normal
    // recording), then mark it as bound and release the transient recording slot. The bound flag
    // tells collect() to use the awaitRecorders/reset/unlock path and to carry the handle over
    // to the new holder on each IMMUTABLE_DATA collection.
    while (true) {
      AggregatorHandle<T> handle =
          getOrCreateHandle(this.aggregatorHolder, attributes, Context.current());
      if (handle != null) {
        handle.bound = true;
        handle.releaseRecord();
        return handle;
      }
    }
  }

  @Override
  public MetricData collect(
      Resource resource, InstrumentationScopeInfo instrumentationScopeInfo, long epochNanos) {
    AggregatorHolder<T> holder = this.aggregatorHolder;

    // Lock out new series creation in the old holder and wait for any in-flight new-series
    // operations to complete BEFORE installing the new holder. This makes the subsequent scan
    // for bound handles (IMMUTABLE_DATA) race-free: no new bind() can sneak in after the scan.
    holder.lockForCollectAndAwait();

    if (memoryMode == REUSABLE_DATA) {
      // REUSABLE_DATA: ping-pong between two maps. New recordings must NOT write to the same
      // accumulator that is mid-collection, because some aggregators (e.g. LastValue) hold a
      // single shared mutable field — a concurrent write would corrupt the collected value.
      // Bound handles are an exception: their RecordOp spins on the per-handle lock and cannot
      // write during the collection window, so they are safe to appear in both maps. Copy them
      // into previousCollectionAggregatorHandles now (before the swap) so they are visible in
      // the new holder and are collected every interval rather than every other interval.
      holder.aggregatorHandles.forEach(
          (attrs, h) -> {
            if (h.bound) {
              previousCollectionAggregatorHandles.put(attrs, h);
            }
          });
      this.aggregatorHolder = new AggregatorHolder<>(previousCollectionAggregatorHandles);
    } else {
      // IMMUTABLE_DATA: seed the new holder with only the bound handles from the old holder.
      // Non-bound series start fresh each interval (delta semantics via holder abandonment).
      // Bound series must survive so their RecordOp references remain valid across intervals.
      ConcurrentHashMap<Attributes, AggregatorHandle<T>> boundHandles = new ConcurrentHashMap<>();
      holder.aggregatorHandles.forEach(
          (attrs, h) -> {
            if (h.bound) {
              boundHandles.put(attrs, h);
            }
          });
      this.aggregatorHolder = new AggregatorHolder<>(boundHandles);
    }

    // Pass 1: signal all handles in the old holder that collection is starting.
    holder.aggregatorHandles.values().forEach(AggregatorHandle::lockForCollect);

    List<T> points;
    if (memoryMode == REUSABLE_DATA) {
      reusableResultList.clear();
      points = reusableResultList;
    } else {
      points = new ArrayList<>(holder.aggregatorHandles.size());
    }

    // REUSABLE_DATA: when at capacity, remove unbound handles that had no recordings so that
    // the map has room for newly seen attribute sets next interval. Bound handles are kept
    // unconditionally since their RecordOp references must remain valid.
    if (memoryMode == REUSABLE_DATA && holder.aggregatorHandles.size() >= maxCardinality) {
      holder.aggregatorHandles.forEach(
          (attribute, handle) -> {
            if (!handle.bound && !handle.hasRecordedValues()) {
              holder.aggregatorHandles.remove(attribute);
            }
          });
    }

    // Start time for synchronous delta instruments is the time of the last collection, or if no
    // collection has yet taken place, the time the instrument was created.
    long startEpochNanos =
        registeredReader.getLastCollectEpochNanosOrDefault(instrumentCreationEpochNanos);

    // Pass 2: drain, aggregate, and (where needed) reset or pool each handle.
    // Unbound handles use awaitRecordersAndUnlock (state → 0 immediately after drain).
    // Bound handles stay locked through aggregation so the accumulator is reset atomically
    // before recordings resume — no inner-handle rotation needed since
    // aggregateThenMaybeReset(reset=true) already resets in-place while locked.
    holder.aggregatorHandles.forEach(
        (attributes, handle) -> {
          if (handle.bound) {
            handle.awaitRecorders();
            T point = null;
            if (handle.hasRecordedValues()) {
              point =
                  handle.aggregateThenMaybeReset(
                      startEpochNanos, epochNanos, attributes, /* reset= */ true);
            }
            handle.unlockAfterCollect();
            if (point != null) {
              points.add(point);
            }
          } else {
            handle.awaitRecordersAndUnlock();
            if (!handle.hasRecordedValues()) {
              return;
            }
            T point =
                handle.aggregateThenMaybeReset(
                    startEpochNanos, epochNanos, attributes, /* reset= */ true);
            if (memoryMode == IMMUTABLE_DATA) {
              aggregatorHandlePool.offer(handle);
            }
            if (point != null) {
              points.add(point);
            }
          }
        });

    if (memoryMode == REUSABLE_DATA) {
      previousCollectionAggregatorHandles = holder.aggregatorHandles;
    }

    if (points.isEmpty() || !enabled) {
      return EmptyMetricData.getInstance();
    }

    return aggregator.toMetricData(
        resource, instrumentationScopeInfo, metricDescriptor, points, DELTA);
  }

  private static class AggregatorHolder<T extends PointData> {
    private final ConcurrentHashMap<Attributes, AggregatorHandle<T>> aggregatorHandles;
    // Guards new-series creation using an even/odd protocol:
    //   - Threads creating a new series increment by 2 (keeping the value even while unlocked)
    //     and decrement by 2 on release.
    //   - The collect thread increments by 1 (making the value odd) to lock out new-series
    //     creation, then waits for the value to return to 1 (no threads in-flight).
    private final AtomicInteger newSeriesGate = new AtomicInteger(0);

    private AggregatorHolder() {
      aggregatorHandles = new ConcurrentHashMap<>();
    }

    private AggregatorHolder(ConcurrentHashMap<Attributes, AggregatorHandle<T>> aggregatorHandles) {
      this.aggregatorHandles = aggregatorHandles;
    }

    /** Returns true and acquires the gate if not locked for collection. */
    boolean tryAcquireForNewSeries() {
      int s = newSeriesGate.addAndGet(2);
      if ((s & 1) != 0) {
        newSeriesGate.addAndGet(-2);
        return false;
      }
      return true;
    }

    /** Releases the gate acquired via {@link #tryAcquireForNewSeries()}. */
    void releaseNewSeries() {
      newSeriesGate.addAndGet(-2);
    }

    /** Returns true if the collector has locked this holder against new-series creation. */
    boolean isLockedForCollect() {
      return (newSeriesGate.get() & 1) != 0;
    }

    /** Locks new-series creation and waits for any in-flight new-series operations to complete. */
    void lockForCollectAndAwait() {
      int s = newSeriesGate.addAndGet(1);
      while (s != 1) {
        s = newSeriesGate.get();
      }
    }
  }
}
