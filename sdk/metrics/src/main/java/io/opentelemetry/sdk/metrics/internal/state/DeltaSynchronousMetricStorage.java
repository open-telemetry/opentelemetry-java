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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.annotation.Nullable;

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
        newDeltaHandle = new DeltaAggregatorHandle<>(aggregator.createHandle(clock.now()));
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
  public MetricData collect(
      Resource resource, InstrumentationScopeInfo instrumentationScopeInfo, long epochNanos) {
    ConcurrentHashMap<Attributes, DeltaAggregatorHandle<T>> aggregatorHandles;
    AggregatorHolder<T> holder = this.aggregatorHolder;
    this.aggregatorHolder =
        (memoryMode == REUSABLE_DATA)
            ? new AggregatorHolder<>(previousCollectionAggregatorHandles)
            : new AggregatorHolder<>();

    // Lock out new series creation in the old holder and wait for any in-flight new-series
    // operations to complete. This guarantees the per-handle lock pass below sees every handle
    // that will ever be inserted into holder.aggregatorHandles.
    holder.lockForCollectAndAwait();

    // Lock each handle and wait for any in-flight recorders against it to finish.
    holder.aggregatorHandles.values().forEach(DeltaAggregatorHandle::lockForCollect);
    holder.aggregatorHandles.values().forEach(DeltaAggregatorHandle::awaitRecordersAndUnlock);
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
    // In IMMUTABLE_DATA MemoryMode, this is accomplished by swapping in a new empty holder,
    // abandoning the old map so each new recording in the next interval starts fresh from 0.
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

  private static final class DeltaAggregatorHandle<T extends PointData> {
    final AggregatorHandle<T> handle;
    // Guards per-handle recording using the same even/odd protocol as
    // AggregatorHolder.newSeriesGate,
    // but scoped to a single series:
    //   - Recording threads increment by 2 before recording, decrement by 2 when done.
    //   - The collect thread increments by 1 (making the count odd) as a signal that this
    //     handle is being collected; recorders that observe an odd count release and retry.
    //   - Once all in-flight recordings finish the count returns to 1, and the collect
    //     thread decrements by 1 to restore it to even for the next cycle.
    private final AtomicInteger state = new AtomicInteger(0);

    DeltaAggregatorHandle(AggregatorHandle<T> handle) {
      this.handle = handle;
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
    void awaitRecordersAndUnlock() {
      while (state.get() > 1) {}
      state.addAndGet(-1);
    }
  }
}
