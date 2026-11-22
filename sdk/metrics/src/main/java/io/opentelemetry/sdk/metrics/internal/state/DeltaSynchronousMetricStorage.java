/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.common.export.MemoryMode.IMMUTABLE_DATA;
import static io.opentelemetry.sdk.common.export.MemoryMode.REUSABLE_DATA;
import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.DELTA;

import io.opentelemetry.api.common.AttributeKey;
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
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import javax.annotation.Nullable;

class DeltaSynchronousMetricStorage<T extends PointData>
    extends DefaultSynchronousMetricStorage<T> {
  private final long instrumentCreationEpochNanos;
  private final RegisteredReader registeredReader;
  private final MemoryMode memoryMode;

  private volatile AggregatorHolder<T> aggregatorHolder = new AggregatorHolder<>();

  // Reset unbound handles from the preceding collection, published after it drains.
  // Only populated in REUSABLE_DATA mode.
  @Nullable
  private volatile ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>>
      previousCollectionAggregatorHandles;

  // Only populated if memoryMode == REUSABLE_DATA
  private final ArrayList<T> reusableResultList = new ArrayList<>();
  private final ConcurrentLinkedQueue<DeltaAggregatorHandle<T>> aggregatorHandlePool =
      new ConcurrentLinkedQueue<>();

  DeltaSynchronousMetricStorage(
      RegisteredReader registeredReader,
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      UnaryOperator<Attributes> attributesFilter,
      Clock clock,
      int maxCardinality,
      boolean enabled,
      @Nullable Predicate<AttributeKey<?>> contextScopedAttributesFilter) {
    super(
        metricDescriptor,
        aggregator,
        attributesFilter,
        clock,
        maxCardinality,
        enabled,
        contextScopedAttributesFilter);
    this.instrumentCreationEpochNanos = clock.now();
    this.registeredReader = registeredReader;
    this.memoryMode = registeredReader.getReader().getMemoryMode();
  }

  @Override
  void doRecordLong(long value, Attributes attributes, Context context) {
    DeltaAggregatorHandle<T> handle = acquireHandleForRecord(attributes);
    try {
      handle.handle.recordLong(value, attributes, context);
    } finally {
      handle.releaseRecord();
    }
  }

  @Override
  void doRecordDouble(double value, Attributes attributes, Context context) {
    DeltaAggregatorHandle<T> handle = acquireHandleForRecord(attributes);
    try {
      handle.handle.recordDouble(value, attributes, context);
    } finally {
      handle.releaseRecord();
    }
  }

  @SuppressWarnings("ThreadPriorityCheck")
  private DeltaAggregatorHandle<T> acquireHandleForRecord(Attributes attributes) {
    while (true) {
      DeltaAggregatorHandle<T> handle =
          tryAcquireHandleForRecord(this.aggregatorHolder, attributes);
      if (handle != null) {
        return handle;
      }
      // Holder or handle is locked for collection; yield to let the collector advance
      Thread.yield();
    }
  }

  @Nullable
  private DeltaAggregatorHandle<T> tryAcquireHandleForRecord(
      AggregatorHolder<T> holder, Attributes attributes) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesFilter.apply(attributes);
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
      handle = insertHandleForNewSeries(aggregatorHandles, attributes);
      // Pre-increment per-handle state while the holder gate is still held. The collect
      // thread's lock pass cannot start until all threads release the holder gate, so this
      // increment is guaranteed to be observed by the lock pass before it runs.
      handle.acquireForRecord();
      return handle;
    } finally {
      holder.releaseNewSeries();
    }
  }

  // Called with the holder gate acquired, so the collector cannot recycle the map while in use.
  private DeltaAggregatorHandle<T> insertHandleForNewSeries(
      ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles,
      Attributes attributes) {
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> previous =
        previousCollectionAggregatorHandles;
    // Delta timestamps do not depend on the handle's original creation time.
    DeltaAggregatorHandle<T> candidate =
        previous == null ? aggregatorHandlePool.poll() : previous.remove(attributes);
    boolean reused = candidate != null;
    if (candidate == null) {
      candidate = new DeltaAggregatorHandle<>(this, aggregator.createHandle(clock.now()));
    }

    DeltaAggregatorHandle<T> existing = aggregatorHandles.putIfAbsent(attributes, candidate);
    // A collection may have published its reset handles since the lookup above. Once a series
    // has an active handle, its reset handle is redundant. The collector also checks for series
    // inserted before publication, so the two maps cannot retain separate idle copies.
    if (!reused && memoryMode == REUSABLE_DATA) {
      previous = previousCollectionAggregatorHandles;
      if (previous != null) {
        previous.remove(attributes);
      }
    }
    return existing == null ? candidate : existing;
  }

  @Override
  public BoundStorageHandle bind(Attributes attributes) {
    Attributes processed = attributesFilter.apply(attributes);
    return new DeltaBoundHandle<>(bindHandle(processed), attributes);
  }

  @SuppressWarnings("ThreadPriorityCheck")
  private DeltaAggregatorHandle<T> bindHandle(Attributes attributes) {
    while (true) {
      AggregatorHolder<T> holder = this.aggregatorHolder;
      // Always coordinate through the holder gate, even for an existing series: this serializes the
      // bound flag write with collect()'s bound-handle scan (which runs while the gate is locked),
      // guaranteeing the handle is carried into the new holder rather than abandoned/pooled.
      if (!holder.tryAcquireForNewSeries()) {
        // Holder is locked for collection. Retry; once the collector installs the new holder this
        // loop re-reads it and succeeds.
        Thread.yield();
        continue;
      }
      try {
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
        if (handle == null) {
          handle = insertHandleForNewSeries(aggregatorHandles, attributes);
        }
        handle.bound = true;
        return handle;
      } finally {
        holder.releaseNewSeries();
      }
    }
  }

  @Override
  public MetricData collect(
      Resource resource, InstrumentationScopeInfo instrumentationScopeInfo, long epochNanos) {
    AggregatorHolder<T> holder = this.aggregatorHolder;

    // Lock out new series creation (and bind()) in the old holder and wait for any in-flight
    // operations to complete. Done before scanning for bound handles and before installing the new
    // holder, so the bound-handle scan below sees a stable set and no bind() can land in the holder
    // we are about to stop recording into.
    holder.lockForCollectAndAwait();

    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles =
        holder.aggregatorHandles;
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> previous =
        previousCollectionAggregatorHandles;
    if (memoryMode == REUSABLE_DATA && aggregatorHandles.isEmpty()) {
      // No series were admitted. Keep the cache below the limit without moving its entries.
      if (previous != null && previous.size() >= maxCardinality) {
        previous.clear();
      }
      reusableResultList.clear();
      this.aggregatorHolder = new AggregatorHolder<>(aggregatorHandles);
      return EmptyMetricData.getInstance();
    }
    boolean keepUnused =
        previous != null && aggregatorHandles.size() + previous.size() < maxCardinality;
    // For a sparse interval, leave the larger map of idle handles in place and add only the
    // drained active handles to it. For a busy interval, recycle that map for new recordings.
    boolean keepCacheMap =
        previous != null && keepUnused && previous.size() >= aggregatorHandles.size();
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> nextHandles;
    if (previous == null || keepCacheMap) {
      nextHandles = new ConcurrentHashMap<>();
    } else {
      // The holder gate excludes cache lookups until the next holder is published.
      previousCollectionAggregatorHandles = null;
      if (keepUnused) {
        aggregatorHandles.putAll(previous);
      }
      previous.clear();
      nextHandles = previous;
    }

    // Bound instruments hold direct references. Carry their handles into the new holder so bound
    // and unbound recordings for those series continue to share the same wrapper.
    holder.aggregatorHandles.forEach((attributes, handle) -> {
      if (handle.bound) {
        nextHandles.put(attributes, handle);
      }
    });
    this.aggregatorHolder = new AggregatorHolder<>(nextHandles);

    // Lock and drain unbound handles (unchanged from the unbound-only path). Bound handles are
    // handled separately below with a tight per-handle lock window so that recording to a bound
    // series never blocks for the duration of the whole collection.
    aggregatorHandles.forEach((attributes, handle) -> {
      if (!handle.bound) {
        handle.lockForCollect();
      }
    });
    aggregatorHandles.forEach((attributes, handle) -> {
      if (!handle.bound) {
        handle.awaitRecordersAndUnlock();
      }
    });

    List<T> points;
    if (memoryMode == REUSABLE_DATA) {
      reusableResultList.clear();
      points = reusableResultList;
    } else {
      points = new ArrayList<>(aggregatorHandles.size());
    }

    // Start time for synchronous delta instruments is the later of the previous collection
    // and instrument creation, so newly created instruments never predate their creation.
    long startEpochNanos =
        Math.max(
            registeredReader.getLastCollectEpochNanosOrDefault(instrumentCreationEpochNanos),
            instrumentCreationEpochNanos);

    // Grab aggregated points.
    aggregatorHandles.forEach((attributes, handle) -> {
      if (handle.bound) {
        collectBound(handle, attributes, startEpochNanos, epochNanos, points);
        return;
      }
      if (!handle.handle.hasRecordedValues()) {
        return;
      }
      T point =
          handle.handle.aggregateThenMaybeReset(
              startEpochNanos, epochNanos, attributes, /* reset= */ true);

      if (memoryMode == IMMUTABLE_DATA) {
        // Return the handle to the pool.
        // Only in IMMUTABLE_DATA memory mode: in REUSABLE_DATA we avoid using the pool
        // since ConcurrentLinkedQueue.offer() allocates memory internally.
        aggregatorHandlePool.offer(handle);
      }

      if (point != null) {
        points.add(point);
      }
    });

    if (memoryMode == REUSABLE_DATA) {
      if (keepCacheMap && previous != null) {
        // Recorders can be using the cache while we add the drained handles. An active
        // insertion removes a cached duplicate; check again in case it won the race.
        aggregatorHandles.forEach((attributes, handle) -> {
          if (!nextHandles.containsKey(attributes)) {
            previous.put(attributes, handle);
            if (nextHandles.containsKey(attributes)) {
              previous.remove(attributes, handle);
            }
          }
        });
      } else {
        previousCollectionAggregatorHandles = aggregatorHandles;
        aggregatorHandles.forEach((attributes, handle) -> {
          if (nextHandles.containsKey(attributes)) {
            aggregatorHandles.remove(attributes, handle);
          }
        });
      }
    }

    if (points.isEmpty() || !enabled) {
      return EmptyMetricData.getInstance();
    }

    return aggregator.toMetricData(
        resource, instrumentationScopeInfo, metricDescriptor, points, DELTA);
  }

  /**
   * Collects a bound handle by rotation: drain in-flight recorders, swap in a fresh (already-zero)
   * accumulator while locked, unlock so recorders resume on the fresh accumulator, then aggregate
   * the drained accumulator off the lock (no recorders reference it anymore). The two accumulators
   * are ping-ponged via {@link DeltaAggregatorHandle#spare} so steady-state collection allocates
   * nothing.
   */
  private void collectBound(
      DeltaAggregatorHandle<T> handle,
      Attributes attributes,
      long startEpochNanos,
      long epochNanos,
      List<T> points) {
    handle.lockForCollect();
    handle.awaitRecorders();
    if (!handle.handle.hasRecordedValues()) {
      // Nothing recorded this interval; no rotation needed.
      handle.unlockAfterCollect();
      return;
    }
    AggregatorHandle<T> collected = handle.handle;
    AggregatorHandle<T> next = handle.spare;
    if (next == null) {
      next = aggregator.createHandle(clock.now());
    }
    handle.handle = next; // recorders resuming after unlock see the zero accumulator
    handle.spare = collected;
    handle.unlockAfterCollect();
    // Aggregate (and reset) the drained accumulator off the lock; it becomes the spare for the next
    // rotation.
    T point =
        collected.aggregateThenMaybeReset(
            startEpochNanos, epochNanos, attributes, /* reset= */ true);
    if (point != null) {
      points.add(point);
    }
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
    @SuppressWarnings("ThreadPriorityCheck")
    void lockForCollectAndAwait() {
      int s = newSeriesGate.addAndGet(1);
      while (s != 1) {
        Thread.yield();
        s = newSeriesGate.get();
      }
    }
  }

  /**
   * Holds each binding's own attributes and delegates to the shared {@link DeltaAggregatorHandle}.
   * Distinct bindings can collapse to the same handle (e.g. a view drops an attribute, or
   * cardinality overflow), so attributes are kept per-bind here rather than on the shared handle,
   * where a later binding would overwrite them.
   */
  private static final class DeltaBoundHandle<T extends PointData> implements BoundStorageHandle {
    private final DeltaAggregatorHandle<T> handle;
    private final Attributes attributes;

    DeltaBoundHandle(DeltaAggregatorHandle<T> handle, Attributes attributes) {
      this.handle = handle;
      this.attributes = attributes;
    }

    @Override
    public void recordLong(long value, Context context) {
      handle.recordLong(value, attributes, context);
    }

    @Override
    public void recordDouble(double value, Context context) {
      handle.recordDouble(value, attributes, context);
    }
  }

  private static final class DeltaAggregatorHandle<T extends PointData> {
    // The storage this handle belongs to. Used by the bound record path (via DeltaBoundHandle) to
    // reach the storage-level enabled / NaN checks. Kept as an explicit back-reference rather than
    // making this a non-static inner class, which would force AggregatorHolder to be non-static
    // too.
    private final DeltaSynchronousMetricStorage<T> storage;
    // Rotated by the collector for bound series (a fresh, already-zero handle swapped in while
    // recorders are drained); write-once for unbound series. Not volatile: the per-handle `state`
    // gate carries visibility — every read is preceded by a `state` acquire and every rotation
    // write
    // followed by a `state` release. INVARIANT: never read `handle` outside the gate.
    AggregatorHandle<T> handle;
    // The off-duty accumulator used for bound rotation, ping-ponged with {@link #handle} each
    // collect. Touched only by the (serialized) collector; volatile to publish across collect
    // cycles. Always null for unbound handles.
    @Nullable private volatile AggregatorHandle<T> spare;
    // True once this series has been bound. Bound wrappers are carried across holder swaps (never
    // pooled) and rotated rather than reset in place. Written under the holder gate by bind(), read
    // by the collector.
    private volatile boolean bound;
    // Guards per-handle recording using the same even/odd protocol as
    // AggregatorHolder.newSeriesGate,
    // but scoped to a single series:
    //   - Recording threads increment by 2 before recording, decrement by 2 when done.
    //   - The collect thread increments by 1 (making the count odd) as a signal that this
    //     handle is being collected; recorders that observe an odd count release and retry.
    //   - Once all in-flight recordings finish the count returns to 1, and the collect
    //     thread decrements by 1 to restore it to even for the next cycle.
    private final AtomicInteger state = new AtomicInteger(0);

    DeltaAggregatorHandle(DeltaSynchronousMetricStorage<T> storage, AggregatorHandle<T> handle) {
      this.storage = storage;
      this.handle = handle;
    }

    /**
     * The bound record path, called by {@link DeltaBoundHandle} with its per-bind attributes.
     * Records onto the current accumulator via the per-handle gate — no map lookup and no holder
     * coordination, since the handle already exists and is carried across holders. Spins if the
     * collector currently holds the lock.
     */
    @SuppressWarnings("ThreadPriorityCheck")
    void recordLong(long value, Attributes attributes, Context context) {
      if (!storage.isEnabled()) {
        return;
      }
      while (!tryAcquireForRecord()) {
        Thread.yield();
      }
      try {
        handle.recordLong(value, attributes, context);
      } finally {
        releaseRecord();
      }
    }

    @SuppressWarnings("ThreadPriorityCheck")
    void recordDouble(double value, Attributes attributes, Context context) {
      if (!storage.shouldRecordDouble(value, attributes)) {
        return;
      }
      while (!tryAcquireForRecord()) {
        Thread.yield();
      }
      try {
        handle.recordDouble(value, attributes, context);
      } finally {
        releaseRecord();
      }
    }

    /**
     * Tries to acquire a recording slot. Returns false if the collector has locked this handle (odd
     * state); the caller should retry with a fresh holder.
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
    @SuppressWarnings("ThreadPriorityCheck")
    void awaitRecordersAndUnlock() {
      while (state.get() > 1) {
        Thread.yield();
      }
      state.addAndGet(-1);
    }

    /**
     * Waits for all in-flight recorders to finish but leaves the handle locked. Used by the bound
     * rotation path, which swaps the accumulator before unlocking via {@link
     * #unlockAfterCollect()}.
     */
    @SuppressWarnings("ThreadPriorityCheck")
    void awaitRecorders() {
      while (state.get() > 1) {
        Thread.yield();
      }
    }

    /** Clears the collection lock set by {@link #lockForCollect()}. */
    void unlockAfterCollect() {
      state.addAndGet(-1);
    }
  }
}
