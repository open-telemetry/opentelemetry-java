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
import io.opentelemetry.sdk.metrics.internal.aggregator.RecordOp;
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
  private volatile ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>>
      previousCollectionAggregatorHandles = new ConcurrentHashMap<>();
  // Only populated if memoryMode == REUSABLE_DATA
  private final ArrayList<T> reusableResultList = new ArrayList<>();
  private final ConcurrentLinkedQueue<DeltaAggregatorHandle<T>> aggregatorHandlePool =
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
    DeltaAggregatorHandle<T> handle = acquireHandleForRecord(attributes, context);
    try {
      handle.handle.recordLong(value, attributes, context);
    } finally {
      handle.releaseRecord();
    }
  }

  @Override
  void doRecordDouble(double value, Attributes attributes, Context context) {
    DeltaAggregatorHandle<T> handle = acquireHandleForRecord(attributes, context);
    try {
      handle.handle.recordDouble(value, attributes, context);
    } finally {
      handle.releaseRecord();
    }
  }

  private DeltaAggregatorHandle<T> acquireHandleForRecord(Attributes attributes, Context context) {
    while (true) {
      DeltaAggregatorHandle<T> handle =
          getDeltaAggregatorHandle(this.aggregatorHolder, attributes, context);
      if (handle != null) {
        return handle;
      }
    }
  }

  @Nullable
  protected DeltaAggregatorHandle<T> getDeltaAggregatorHandle(
      AggregatorHolder<T> holder, Attributes attributes, Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles =
        holder.aggregatorHandles;
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
      DeltaAggregatorHandle<T> newDeltaHandle = aggregatorHandlePool.poll();
      if (newDeltaHandle == null) {
        newDeltaHandle =
            new DeltaAggregatorHandle<>(attributes, aggregator.createHandle(clock.now()));
      } else {
        // Pooled handles retain their old inner handle and creationEpochNanos; update attributes
        // for the new series so that RecordOp recordings use the correct attribute set.
        newDeltaHandle.handle.setAttributes(attributes);
      }
      handle = aggregatorHandles.putIfAbsent(attributes, newDeltaHandle);
      if (handle == null) {
        handle = newDeltaHandle;
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
  public RecordOp bind(Attributes attributes) {
    // Get or create the handle in the current holder's map (same coordination as a normal
    // recording), then mark it as bound and release the transient recording slot. The bound flag
    // tells collect() to use the awaitRecorders/rotate/unlock path and to carry the handle over
    // to the new holder on each IMMUTABLE_DATA collection.
    while (true) {
      DeltaAggregatorHandle<T> handle =
          getDeltaAggregatorHandle(this.aggregatorHolder, attributes, Context.current());
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
            if (h.bound) previousCollectionAggregatorHandles.put(attrs, h);
          });
      this.aggregatorHolder = new AggregatorHolder<>(previousCollectionAggregatorHandles);
    } else {
      // IMMUTABLE_DATA: seed the new holder with only the bound handles from the old holder.
      // Non-bound series start fresh each interval (delta semantics via holder abandonment).
      // Bound series must survive so their RecordOp references remain valid across intervals.
      ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> boundHandles =
          new ConcurrentHashMap<>();
      holder.aggregatorHandles.forEach(
          (attrs, h) -> {
            if (h.bound) boundHandles.put(attrs, h);
          });
      this.aggregatorHolder = new AggregatorHolder<>(boundHandles);
    }

    // Pass 1: signal all handles in the old holder that collection is starting.
    holder.aggregatorHandles.values().forEach(DeltaAggregatorHandle::lockForCollect);

    List<T> points;
    if (memoryMode == REUSABLE_DATA) {
      reusableResultList.clear();
      points = reusableResultList;
    } else {
      points = new ArrayList<>(holder.aggregatorHandles.size());
    }

    // REUSABLE_DATA: when at capacity, remove handles that had no recordings so that the map
    // has room for newly seen attribute sets next interval. Bound handles are kept
    // unconditionally since their RecordOp references must remain valid.
    if (memoryMode == REUSABLE_DATA && holder.aggregatorHandles.size() >= maxCardinality) {
      holder.aggregatorHandles.forEach(
          (attribute, handle) -> {
            if (!handle.bound && !handle.handle.hasRecordedValues()) {
              holder.aggregatorHandles.remove(attribute);
            }
          });
    }

    // Start time for synchronous delta instruments is the time of the last collection, or if no
    // collection has yet taken place, the time the instrument was created.
    long startEpochNanos =
        registeredReader.getLastCollectEpochNanosOrDefault(instrumentCreationEpochNanos);

    // Pass 2: drain, aggregate, and (where needed) reset or rotate each handle.
    // Unbound handles use awaitRecordersAndUnlock (state → 0 immediately after drain).
    // Bound handles stay locked through aggregation so the inner accumulator can be rotated
    // (IMMUTABLE_DATA) before recordings resume; this guarantees new recordings write to the
    // fresh accumulator, not the one being aggregated.
    holder.aggregatorHandles.forEach(
        (attributes, handle) -> {
          if (handle.bound) {
            handle.awaitRecorders();
            T point = null;
            if (handle.handle.hasRecordedValues()) {
              point =
                  handle.handle.aggregateThenMaybeReset(
                      startEpochNanos, epochNanos, attributes, /* reset= */ true);
            }
            if (memoryMode == IMMUTABLE_DATA) {
              DeltaAggregatorHandle<T> fresh = aggregatorHandlePool.poll();
              AggregatorHandle<T> freshInner =
                  (fresh != null) ? fresh.handle : aggregator.createHandle(clock.now());
              freshInner.setAttributes(attributes);
              handle.handle = freshInner;
            }
            handle.unlockAfterCollect();
            if (point != null) {
              points.add(point);
            }
          } else {
            handle.awaitRecordersAndUnlock();
            if (!handle.handle.hasRecordedValues()) {
              return;
            }
            T point =
                handle.handle.aggregateThenMaybeReset(
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
    private final ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles;
    // Guards new-series creation using an even/odd protocol:
    //   - Threads creating a new series increment by 2 (keeping the value even while unlocked)
    //     and decrement by 2 on release.
    //   - The collect thread increments by 1 (making the value odd) to lock out new-series
    //     creation, then waits for the value to return to 1 (no threads in-flight).
    private final AtomicInteger newSeriesGate = new AtomicInteger(0);

    private AggregatorHolder() {
      aggregatorHandles = new ConcurrentHashMap<>();
    }

    private AggregatorHolder(
        ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles) {
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

  private static final class DeltaAggregatorHandle<T extends PointData> implements RecordOp {
    private volatile AggregatorHandle<T> handle;
    // Written by bind(), read by collect(). Volatile so the collect thread sees the write
    // even though it is not protected by the per-handle state machine.
    volatile boolean bound = false;
    // Guards per-handle recording using the same even/odd protocol as
    // AggregatorHolder.newSeriesGate,
    // but scoped to a single series:
    //   - Recording threads increment by 2 before recording, decrement by 2 when done.
    //   - The collect thread increments by 1 (making the count odd) as a signal that this
    //     handle is being collected; recorders that observe an odd count release and retry.
    //   - Once all in-flight recordings finish the count returns to 1, and the collect
    //     thread decrements by 1 to restore it to even for the next cycle.
    private final AtomicInteger state = new AtomicInteger(0);

    DeltaAggregatorHandle(Attributes attributes, AggregatorHandle<T> handle) {
      this.handle = handle;
      this.handle.setAttributes(attributes);
    }

    /**
     * Tries to acquire a recording slot. Returns false if the collector has locked this handle (odd
     * state); the caller should retry.
     */
    boolean tryAcquireForRecord() {
      int s = state.addAndGet(2);
      if ((s & 1) != 0) {
        state.addAndGet(-2);
        return false;
      }
      return true;
    }

    /**
     * Acquires a recording slot unconditionally. Only safe to call while the holder gate is held,
     * which prevents the collector from starting its lock pass.
     */
    void acquireForRecord() {
      state.addAndGet(2);
    }

    /**
     * Releases a recording slot acquired via {@link #tryAcquireForRecord()} or {@link
     * #acquireForRecord()}.
     */
    void releaseRecord() {
      state.addAndGet(-2);
    }

    /** Signals that collection is starting. Recorders that observe this will abort and retry. */
    void lockForCollect() {
      state.addAndGet(1);
    }

    /** Waits for all in-flight recorders to finish, then clears the collection lock. */
    void awaitRecordersAndUnlock() {
      while (state.get() > 1) {}
      state.addAndGet(-1);
    }

    /**
     * Waits for all in-flight recorders to finish WITHOUT clearing the collection lock. Used by the
     * collect thread for bound handles so that the inner handle can be aggregated and (in
     * IMMUTABLE_DATA mode) rotated while the lock is still held, preventing any new recording from
     * reaching the old accumulator before it is pooled.
     */
    void awaitRecorders() {
      while (state.get() > 1) {}
    }

    /**
     * Clears the collection lock after aggregation is complete. Must be called after {@link
     * #awaitRecorders()} and any inner handle rotation. The happens-before edge from this write to
     * the next {@link #tryAcquireForRecord()} ensures recording threads see the updated {@link
     * #handle} value.
     */
    void unlockAfterCollect() {
      state.addAndGet(-1);
    }

    @Override
    public void recordLong(long value) {
      while (true) {
        if (tryAcquireForRecord()) {
          try {
            handle.recordLong(value);
          } finally {
            releaseRecord();
          }
          return;
        }
      }
    }

    @Override
    public void recordDouble(double value) {
      while (true) {
        if (tryAcquireForRecord()) {
          try {
            handle.recordDouble(value);
          } finally {
            releaseRecord();
          }
          return;
        }
      }
    }
  }
}
