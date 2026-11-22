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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
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

  // Only populated if memoryMode == REUSABLE_DATA
  @Nullable
  private volatile ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>>
      previousCollectionAggregatorHandles;

  // Only accessed by the serialized collector.
  private boolean reusableMapsMayDiffer;
  // Admission marks this under the current holder's gate. The collector consumes and resets
  // it after closing that gate. During the handoff, writers and the collector also hold the
  // new holder's monitor.
  private volatile boolean currentMapChanged;
  // Set under the holder gate. Bound series survive every subsequent collection.
  private volatile boolean hasBoundHandles;

  // Only populated if memoryMode == REUSABLE_DATA
  private final ArrayList<T> reusableResultList = new ArrayList<>();
  private final PointCollector pointCollector = new PointCollector();
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
    // Read this before looking up the handle. Once it is null, no replacement in this
    // holder can still be in progress.
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> draining = holder.draining;
    DeltaAggregatorHandle<T> handle = holder.aggregatorHandles.get(attributes);
    if (handle == null || draining != null) {
      return acquireOnMissOrDuringHandoff(holder, attributes, handle, draining);
    }
    if (!handle.tryAcquireForRecord()) {
      return null;
    }
    if (holder.isLockedForCollect()) {
      handle.releaseRecord();
      return null;
    }
    return handle;
  }

  @Nullable
  private DeltaAggregatorHandle<T> acquireOnMissOrDuringHandoff(
      AggregatorHolder<T> holder,
      Attributes attributes,
      @Nullable DeltaAggregatorHandle<T> handle,
      @Nullable ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> draining) {
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles =
        holder.aggregatorHandles;
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
    if (draining != null
        && (handle == null || (!handle.bound && draining.get(attributes) == handle))) {
      return acquireWhileCollecting(holder, attributes, /* record= */ true);
    }
    if (handle != null) {
      // Existing series: try to acquire a recording slot. Returns false if the collector has
      // locked this handle (odd state), meaning we should retry with the new holder.
      if (!handle.tryAcquireForRecord()) {
        return null;
      }
      // A stale recorder must check the holder after acquiring its recording slot. Once the
      // collector closes the holder, only slots that were already admitted can record.
      if (holder.isLockedForCollect()
          || (draining != null && aggregatorHandles.get(attributes) != handle)) {
        handle.releaseRecord();
        return null;
      }
      return handle;
    }
    // Reserve the new series before the collector closes this holder.
    if (!holder.tryAcquireForNewSeries()) {
      return null;
    }
    try {
      handle = insertHandle(holder, attributes);
      // Reserve a recording slot before releasing the holder gate, so the collector waits
      // for this measurement.
      handle.acquireForRecord();
      return handle;
    } finally {
      holder.releaseNewSeries();
    }
  }

  // Called while the holder gate is held. In reusable mode copy only the reference to the
  // reset handle, leaving the map entry in place for the next exchange.
  private DeltaAggregatorHandle<T> insertHandle(AggregatorHolder<T> holder, Attributes attributes) {
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> previous =
        previousCollectionAggregatorHandles;
    DeltaAggregatorHandle<T> candidate =
        previous == null ? aggregatorHandlePool.poll() : previous.get(attributes);
    if (candidate == null) {
      candidate = new DeltaAggregatorHandle<>(this, aggregator.createHandle(clock.now()));
    }
    DeltaAggregatorHandle<T> existing = holder.aggregatorHandles.putIfAbsent(attributes, candidate);
    if (existing == null && memoryMode == REUSABLE_DATA) {
      currentMapChanged = true;
    }
    return existing == null ? candidate : existing;
  }

  @Nullable
  private DeltaAggregatorHandle<T> acquireWhileCollecting(
      AggregatorHolder<T> holder, Attributes attributes, boolean record) {
    if (!holder.tryAcquireForNewSeries()) {
      return null;
    }
    try {
      synchronized (holder) {
        ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> draining = holder.draining;
        if (draining == null) {
          // Retry against the final mapping. The collector may have just handed it back.
          return null;
        }
        DeltaAggregatorHandle<T> handle = holder.aggregatorHandles.get(attributes);
        if (handle == null || (!handle.bound && draining.get(attributes) == handle)) {
          handle = new DeltaAggregatorHandle<>(this, aggregator.createHandle(clock.now()));
          holder.aggregatorHandles.put(attributes, handle);
          currentMapChanged = true;
        }
        if (record) {
          // The caller only enters this path for a missing or shared unbound series.
          // The selected handle is independent of the outgoing collector, and the holder
          // gate prevents the incoming collector from closing it before admission.
          handle.acquireForRecord();
        } else {
          handle.bound = true;
        }
        return handle;
      }
    } finally {
      holder.releaseNewSeries();
    }
  }

  @Override
  public BoundStorageHandle bind(Attributes attributes) {
    Attributes processed = attributesFilter.apply(attributes);
    return new DeltaBoundHandle<>(bindHandle(processed), attributes);
  }

  @SuppressWarnings("ThreadPriorityCheck")
  private DeltaAggregatorHandle<T> bindHandle(Attributes processed) {
    while (true) {
      Attributes attributes = processed;
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
        ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> draining = holder.draining;
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
        if (draining != null) {
          handle = acquireWhileCollecting(holder, attributes, /* record= */ false);
          if (handle == null) {
            continue;
          }
        } else {
          if (handle == null) {
            handle = insertHandle(holder, attributes);
          }
          handle.bound = true;
        }
        hasBoundHandles = true;
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
    reusableMapsMayDiffer |= currentMapChanged;
    currentMapChanged = false;

    // Seed the new holder with the bound handles, so that (a) the bound wrappers survive the swap
    // (their bound instruments hold direct references), (b) a series recorded both bound and
    // unbound continues to share one wrapper, and (c) bound series are collected every interval.
    // In REUSABLE_DATA we ping-pong between two maps, so the bound wrappers are copied into the
    // next
    // map; in IMMUTABLE_DATA the old map is abandoned and the new map starts with only the bound
    // wrappers.
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles =
        holder.aggregatorHandles;
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> previous =
        previousCollectionAggregatorHandles;
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> newHolderHandles =
        previous == null ? new ConcurrentHashMap<>() : previous;
    previousCollectionAggregatorHandles = null;
    AggregatorHolder<T> next = new AggregatorHolder<>(newHolderHandles);
    if (memoryMode == REUSABLE_DATA) {
      next.draining = aggregatorHandles;
    }
    if (hasBoundHandles) {
      aggregatorHandles.forEach((attributes, handle) -> {
        if (handle.bound) {
          newHolderHandles.put(attributes, handle);
        }
      });
    }
    if (previous != null && previous.size() >= maxCardinality && reusableMapsMayDiffer) {
      // A saturated map from two intervals ago may still contain series evicted from the
      // just-finished interval. Drop those idle entries before admitting new measurements.
      previous.forEach((attributes, handle) -> {
        if (!aggregatorHandles.containsKey(attributes)) {
          previous.remove(attributes, handle);
        }
      });
      reusableMapsMayDiffer = previous.size() != aggregatorHandles.size();
    }
    this.aggregatorHolder = next;

    List<T> points;
    if (memoryMode == REUSABLE_DATA) {
      reusableResultList.clear();
      points = reusableResultList;
    } else {
      points = new ArrayList<>(aggregatorHandles.size());
    }

    boolean atCardinalityLimit =
        memoryMode == REUSABLE_DATA && aggregatorHandles.size() >= maxCardinality;

    // Start time for synchronous delta instruments is the later of the previous collection
    // and instrument creation, so newly created instruments never predate their creation.
    long startEpochNanos =
        Math.max(
            registeredReader.getLastCollectEpochNanosOrDefault(instrumentCreationEpochNanos),
            instrumentCreationEpochNanos);

    pointCollector.points = points;
    pointCollector.pruneFrom = atCardinalityLimit ? aggregatorHandles : null;
    pointCollector.startEpochNanos = startEpochNanos;
    pointCollector.epochNanos = epochNanos;
    try {
      aggregatorHandles.forEach(pointCollector);
    } finally {
      pointCollector.points = Collections.emptyList();
      pointCollector.pruneFrom = null;
    }

    if (memoryMode == REUSABLE_DATA) {
      synchronized (next) {
        // A recording in the next interval may have needed a replacement while the old
        // accumulator was being drained. Point both existing map entries to the live handle.
        if (currentMapChanged) {
          aggregatorHandles.forEach((attributes, handle) -> {
            DeltaAggregatorHandle<T> replacement = newHolderHandles.get(attributes);
            if (replacement != null && replacement != handle) {
              aggregatorHandles.replace(attributes, handle, replacement);
            }
          });
        }
        previousCollectionAggregatorHandles = aggregatorHandles;
        next.draining = null;
      }
    }

    if (points.isEmpty() || !enabled) {
      return EmptyMetricData.getInstance();
    }

    return aggregator.toMetricData(
        resource, instrumentationScopeInfo, metricDescriptor, points, DELTA);
  }

  // Collection is serialized. Reuse the visitor to avoid a capturing lambda per collection,
  // including when a retained map has no recorded points.
  private final class PointCollector implements BiConsumer<Attributes, DeltaAggregatorHandle<T>> {
    private List<T> points = Collections.emptyList();
    @Nullable private ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> pruneFrom;
    private long startEpochNanos;
    private long epochNanos;

    @Override
    public void accept(Attributes attributes, DeltaAggregatorHandle<T> handle) {
      if (handle.bound) {
        collectBound(handle, attributes, startEpochNanos, epochNanos, points);
        return;
      }
      // The outgoing holder is already closed. Its unbound handles only need to drain
      // admitted recorders. The incoming holder replaces any of these handles it needs
      // before the handoff ends.
      handle.awaitUnboundRecorders();
      if (handle.handle.hasRecordedValues()) {
        T point =
            handle.handle.aggregateThenMaybeReset(
                startEpochNanos, epochNanos, attributes, /* reset= */ true);
        if (point != null) {
          points.add(point);
        }
        if (memoryMode == IMMUTABLE_DATA) {
          aggregatorHandlePool.offer(handle);
        }
      } else if (pruneFrom != null) {
        pruneFrom.remove(attributes, handle);
        reusableMapsMayDiffer = true;
      }
    }
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
    // The outgoing map may share handles with this holder. Replacements and the end of the
    // handoff are serialized on this holder, so replacements cannot occur once this is null.
    @Nullable private volatile ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> draining;
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
     * which prevents the collector from draining this slot prematurely.
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

    /** Waits for recording slots to drain after the outgoing holder has been closed. */
    @SuppressWarnings("ThreadPriorityCheck")
    void awaitUnboundRecorders() {
      while (state.get() != 0) {
        Thread.yield();
      }
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
