/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.concurrent.AdderUtil;
import io.opentelemetry.sdk.metrics.internal.concurrent.DoubleAdder;
import io.opentelemetry.sdk.metrics.internal.concurrent.LongAdder;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.MutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import javax.annotation.Nullable;

/**
 * Aggregator that generates explicit bucket histograms.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DoubleExplicitBucketHistogramAggregator
    implements Aggregator<HistogramPointData> {
  private final double[] boundaries;
  private final boolean recordMinMax;
  private final MemoryMode memoryMode;

  // a cache for converting to MetricData
  private final List<Double> boundaryList;

  private final ExemplarReservoirFactory reservoirFactory;

  /**
   * Constructs an explicit bucket histogram aggregator.
   *
   * @param boundaries Bucket boundaries, in-order.
   * @param recordMinMax whether to record min and max values
   * @param reservoirFactory Supplier of exemplar reservoirs per-stream.
   * @param memoryMode The {@link MemoryMode} to use in this aggregator.
   */
  public DoubleExplicitBucketHistogramAggregator(
      double[] boundaries,
      boolean recordMinMax,
      ExemplarReservoirFactory reservoirFactory,
      MemoryMode memoryMode) {
    this.boundaries = boundaries;
    this.recordMinMax = recordMinMax;
    this.memoryMode = memoryMode;

    List<Double> boundaryList = new ArrayList<>(this.boundaries.length);
    for (double v : this.boundaries) {
      boundaryList.add(v);
    }
    this.boundaryList = Collections.unmodifiableList(boundaryList);
    this.reservoirFactory = reservoirFactory;
  }

  @Override
  public AggregatorHandle<HistogramPointData> createHandle(long creationEpochNanos) {
    return new Handle(
        creationEpochNanos, boundaryList, boundaries, recordMinMax, reservoirFactory, memoryMode);
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor metricDescriptor,
      Collection<HistogramPointData> pointData,
      AggregationTemporality temporality) {
    return ImmutableMetricData.createDoubleHistogram(
        resource,
        instrumentationScopeInfo,
        metricDescriptor.getName(),
        metricDescriptor.getDescription(),
        metricDescriptor.getSourceInstrument().getUnit(),
        ImmutableHistogramData.create(temporality, pointData));
  }

  /**
   * Lock-free histogram handle inspired by <a
   * href="https://github.com/prometheus/client_java/blob/565a58396c92ddfbe1b64de37c40a0a8c165a612/prometheus-metrics-core/src/main/java/io/prometheus/metrics/core/metrics/Histogram.java">prometheus/client_java</a>.
   *
   * <p>Bucket counts and running sum use {@link LongAdder} / {@link DoubleAdder}. Min and max use
   * CAS loops on volatile long bit patterns with a fast-exit for non-extremes. Total count is
   * derived from bucket counts at collect.
   *
   * <p>A thread-striped {@link AtomicLong} array coordinates record and collect. Its sign bit
   * signals "collect in progress". Recorders back out and spin when they observe it. This prevents
   * an observation's writes from being split across collections.
   *
   * <p>Within {@link #doRecordDouble} the bucket increment is the last write. Its internal volatile
   * write publishes the prior sum/min/max writes, so the collector's wait for {@code
   * sum(bucketCounts) >= expected} doubles as a barrier for the whole observation.
   */
  static final class Handle extends AggregatorHandle<HistogramPointData> {
    private static final long COLLECT_BIT = 1L << 63;
    private static final long MIN_INIT_BITS = Double.doubleToRawLongBits(Double.POSITIVE_INFINITY);
    private static final long MAX_INIT_BITS = Double.doubleToRawLongBits(Double.NEGATIVE_INFINITY);
    private static final AtomicLongFieldUpdater<Handle> MIN_BITS =
        AtomicLongFieldUpdater.newUpdater(Handle.class, "minBits");
    private static final AtomicLongFieldUpdater<Handle> MAX_BITS =
        AtomicLongFieldUpdater.newUpdater(Handle.class, "maxBits");

    private final List<Double> boundaryList;
    private final double[] boundaries;
    private final boolean recordMinMax;

    private final LongAdder[] bucketCounts;
    private final DoubleAdder sum = AdderUtil.createDoubleAdder();

    // Min / max as raw double bits so they can be CAS-updated via AtomicLongFieldUpdater. Updated
    // via CAS loops that fast-exit when the observation isn't a new extreme — the common
    // steady-state case has no memory write.
    @SuppressWarnings("UnusedVariable")
    private volatile long minBits = MIN_INIT_BITS;

    @SuppressWarnings("UnusedVariable")
    private volatile long maxBits = MAX_INIT_BITS;

    // Power-of-2 length so the stripe probe is a bitwise AND with stripeMask.
    private final AtomicLong[] stripedStartedCounter;
    private final int stripeMask;

    // Observations that have made it into buckets across all time (current + previously
    // drained). Reconciles bucketCounts (drained on delta reset) with cumulativeStarted (never
    // resets). Cumulative: stays 0. Delta: += drained totalCount each cycle. Collector-only.
    private long cumulativeDrainedCount;

    private final long[] countsScratch;

    // Non-null only when MemoryMode == REUSABLE_DATA.
    @Nullable private final MutableHistogramPointData reusablePoint;

    Handle(
        long creationEpochNanos,
        List<Double> boundaryList,
        double[] boundaries,
        boolean recordMinMax,
        ExemplarReservoirFactory reservoirFactory,
        MemoryMode memoryMode) {
      super(creationEpochNanos, reservoirFactory, /* isDoubleType= */ true);
      this.boundaryList = boundaryList;
      this.boundaries = boundaries;
      this.recordMinMax = recordMinMax;
      int bucketCount = boundaries.length + 1;
      this.bucketCounts = new LongAdder[bucketCount];
      for (int i = 0; i < bucketCount; i++) {
        this.bucketCounts[i] = AdderUtil.createLongAdder();
      }
      // Sized to NCPUS (rounded up to a power of 2 so the probe mask compiles to a bitwise AND).
      // NCPUS is an upper bound on threads simultaneously executing, which bounds the useful
      // stripe count for handling per-handle contention.
      int stripes = roundUpToPowerOfTwo(Runtime.getRuntime().availableProcessors());
      this.stripedStartedCounter = new AtomicLong[stripes];
      for (int i = 0; i < stripes; i++) {
        this.stripedStartedCounter[i] = new AtomicLong();
      }
      this.stripeMask = stripes - 1;
      this.countsScratch = new long[bucketCount];
      if (memoryMode == MemoryMode.REUSABLE_DATA) {
        this.reusablePoint = new MutableHistogramPointData(bucketCount);
      } else {
        this.reusablePoint = null;
      }
    }

    @Override
    public void recordLong(long value, Attributes attributes, Context context) {
      // There is no LongExplicitBucketHistogramAggregator. Redirect to recordDouble so
      // LongHistogram measurements route through this handle.
      super.recordDouble((double) value, attributes, context);
    }

    @Override
    @SuppressWarnings("ThreadPriorityCheck")
    protected void doRecordDouble(double value) {
      int bucketIndex = ExplicitBucketHistogramUtils.findBucketIndex(this.boundaries, value);

      // Reserve a pre-flip slot on our stripe via CAS. Increment only when the bit is clear at
      // the time of the CAS, otherwise spin until the collector's Phase 4 clears the bit and
      // retry.
      AtomicLong stripe =
          stripedStartedCounter[System.identityHashCode(Thread.currentThread()) & stripeMask];
      while (true) {
        long current = stripe.get();
        if ((current & COLLECT_BIT) != 0) {
          while ((stripe.get() & COLLECT_BIT) != 0) {
            Thread.yield();
          }
          continue;
        }
        if (stripe.compareAndSet(current, current + 1)) {
          break;
        }
        // CAS lost the race (either the bit was just set or another recorder incremented);
        // loop and reevaluate.
      }

      sum.add(value);
      if (recordMinMax) {
        updateMin(value);
        updateMax(value);
      }
      bucketCounts[bucketIndex].increment();
    }

    /** Fast-exits without a CAS when {@code value} is not smaller than the current min. */
    private void updateMin(double value) {
      long newBits = Double.doubleToRawLongBits(value);
      long cur;
      do {
        cur = minBits;
        if (value >= Double.longBitsToDouble(cur)) {
          return;
        }
      } while (!MIN_BITS.compareAndSet(this, cur, newBits));
    }

    /** Fast-exits without a CAS when {@code value} is not larger than the current max. */
    private void updateMax(double value) {
      long newBits = Double.doubleToRawLongBits(value);
      long cur;
      do {
        cur = maxBits;
        if (value <= Double.longBitsToDouble(cur)) {
          return;
        }
      } while (!MAX_BITS.compareAndSet(this, cur, newBits));
    }

    @Override
    @SuppressWarnings("ThreadPriorityCheck")
    protected HistogramPointData doAggregateThenMaybeResetDoubles(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<DoubleExemplarData> exemplars,
        boolean reset) {
      // Phase 1: set the collect bit on every stripe. Capture the pre-flip cumulative count.
      long cumulativeStarted = 0;
      for (AtomicLong stripe : stripedStartedCounter) {
        cumulativeStarted += stripe.getAndAdd(COLLECT_BIT) & ~COLLECT_BIT;
      }

      // Phase 2: wait for pre-flip recorders to publish their bucket increments. Post-flip
      // recorders are spinning on the collect bit, so nothing new arrives.
      while (bucketSumTotal() + cumulativeDrainedCount < cumulativeStarted) {
        Thread.yield();
      }

      // Phase 3: snapshot (and reset if delta) with recorders quiescent.
      long totalCount = 0;
      for (int i = 0; i < bucketCounts.length; i++) {
        long c = reset ? bucketCounts[i].sumThenReset() : bucketCounts[i].sum();
        countsScratch[i] = c;
        totalCount += c;
      }
      if (reset) {
        cumulativeDrainedCount += totalCount;
      }
      double totalSum = reset ? sum.sumThenReset() : sum.sum();

      double snapshotMin = Double.POSITIVE_INFINITY;
      double snapshotMax = Double.NEGATIVE_INFINITY;
      if (recordMinMax) {
        long minSnapshot = reset ? MIN_BITS.getAndSet(this, MIN_INIT_BITS) : minBits;
        long maxSnapshot = reset ? MAX_BITS.getAndSet(this, MAX_INIT_BITS) : maxBits;
        snapshotMin = Double.longBitsToDouble(minSnapshot);
        snapshotMax = Double.longBitsToDouble(maxSnapshot);
      }

      // Phase 4: clear the collect bit. addAndGet(COLLECT_BIT) toggles the sign bit off via
      // two's-complement overflow. Spinning recorders resume.
      for (AtomicLong stripe : stripedStartedCounter) {
        stripe.addAndGet(COLLECT_BIT);
      }

      HistogramPointData pointData;
      if (reusablePoint == null) {
        pointData =
            ImmutableHistogramPointData.create(
                startEpochNanos,
                epochNanos,
                attributes,
                totalSum,
                recordMinMax && totalCount > 0,
                recordMinMax ? snapshotMin : 0,
                recordMinMax && totalCount > 0,
                recordMinMax ? snapshotMax : 0,
                boundaryList,
                PrimitiveLongList.wrap(Arrays.copyOf(countsScratch, countsScratch.length)),
                exemplars);
      } else /* REUSABLE_DATA */ {
        pointData =
            reusablePoint.set(
                startEpochNanos,
                epochNanos,
                attributes,
                totalSum,
                recordMinMax && totalCount > 0,
                recordMinMax ? snapshotMin : 0,
                recordMinMax && totalCount > 0,
                recordMinMax ? snapshotMax : 0,
                boundaryList,
                countsScratch,
                exemplars);
      }
      return pointData;
    }

    private long bucketSumTotal() {
      long total = 0;
      for (LongAdder adder : bucketCounts) {
        total += adder.sum();
      }
      return total;
    }

    /** Smallest power of 2 >= {@code n}, with a floor of 1. */
    private static int roundUpToPowerOfTwo(int n) {
      if (n <= 1) {
        return 1;
      }
      int highest = Integer.highestOneBit(n);
      return highest == n ? highest : highest << 1;
    }
  }
}
