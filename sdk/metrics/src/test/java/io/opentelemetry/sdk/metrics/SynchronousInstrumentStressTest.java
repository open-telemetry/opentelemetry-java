/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.Attributes.empty;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.util.concurrent.Uninterruptibles;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link #stressTest(AggregationTemporality, SyncInstrumentAggregation, MemoryMode,
 * InstrumentValueType)} performs a stress test to confirm simultaneous record and collections do
 * not have concurrency issues like lost writes, partial writes, duplicate writes, etc. All
 * combinations of the following dimensions are tested: aggregation temporality, instrument type
 * (synchronous), memory mode, instrument value type.
 */
class SynchronousInstrumentStressTest {

  private static final String instrumentName = "instrument";
  private static final Duration oneMicrosecond = Duration.ofNanos(1000);
  private static final List<Double> bucketBoundaries =
      ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES;
  private static final double[] bucketBoundariesArr =
      bucketBoundaries.stream().mapToDouble(Double::doubleValue).toArray();
  private static final AttributeKey<String> attributesKey = AttributeKey.stringKey("key");
  private static final Attributes attr1 = Attributes.of(attributesKey, "value1");
  private static final Attributes attr2 = Attributes.of(attributesKey, "value2");
  private static final Attributes attr3 = Attributes.of(attributesKey, "value3");
  private static final Attributes attr4 = Attributes.of(attributesKey, "value4");

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @ParameterizedTest
  @MethodSource("stressTestArgs")
  void stressTest(
      AggregationTemporality aggregationTemporality,
      SyncInstrumentAggregation instrumentType,
      MemoryMode memoryMode,
      InstrumentValueType instrumentValueType) {
    // Initialize metric SDK
    DefaultAggregationSelector aggregationSelector = DefaultAggregationSelector.getDefault();
    if (instrumentType == SyncInstrumentAggregation.HISTOGRAM_EXPONENTIAL_HISTOGRAM) {
      aggregationSelector =
          aggregationSelector.with(
              InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram());
    }
    InMemoryMetricReader reader =
        InMemoryMetricReader.builder()
            .setDefaultAggregationSelector(aggregationSelector)
            .setAggregationTemporalitySelector(unused -> aggregationTemporality)
            .setMemoryMode(memoryMode)
            .build();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(reader).build();
    cleanup.addCloseable(meterProvider);
    Meter meter = meterProvider.get("test");
    Instrument instrument = getInstrument(meter, instrumentType, instrumentValueType);

    // Define list of measurements to record
    // Later, we'll assert that the data collected matches these measurements, with no lost writes,
    // partial writes, duplicate writes, etc.
    int measurementCount = 2000;
    List<Long> measurements = new ArrayList<>();
    for (int i = 0; i < measurementCount; i++) {
      measurements.add((long) i);
    }
    Collections.shuffle(measurements);

    // Define recording threads
    int threadCount = 4;
    List<Thread> recordThreads = new ArrayList<>();
    CountDownLatch latch = new CountDownLatch(threadCount);
    for (int i = 0; i < 4; i++) {
      recordThreads.add(
          new Thread(
              () -> {
                List<Attributes> attributes = Arrays.asList(attr1, attr2, attr3, attr4);
                Collections.shuffle(attributes);
                for (Long measurement : measurements) {
                  for (Attributes attr : attributes) {
                    instrument.record(measurement, attr);
                  }
                  Uninterruptibles.sleepUninterruptibly(oneMicrosecond);
                }
                latch.countDown();
              }));
    }

    // Define collecting thread
    // NOTE: collect makes a copy of MetricData because REUSEABLE_MEMORY mode reuses MetricData
    List<MetricData> collectedMetrics = new ArrayList<>();
    Thread collectThread =
        new Thread(
            () -> {
              while (latch.getCount() != 0) {
                Uninterruptibles.sleepUninterruptibly(oneMicrosecond);
                collectedMetrics.addAll(
                    reader.collectAllMetrics().stream()
                        .map(SynchronousInstrumentStressTest::copy)
                        .collect(toList()));
              }
              collectedMetrics.addAll(
                  reader.collectAllMetrics().stream()
                      .map(SynchronousInstrumentStressTest::copy)
                      .collect(toList()));
            });

    // Start all the threads
    collectThread.start();
    recordThreads.forEach(Thread::start);

    // Wait for the collect thread to end, which collects until the record threads are done
    Uninterruptibles.joinUninterruptibly(collectThread);

    // Assert collected data is consistent with recorded measurements by independently computing the
    // expected aggregated value and comparing to the actual results.
    // NOTE: this does not validate the absence of partial writes for cumulative instruments which
    // track multiple fields. For example, explicit histogram tracks sum and bucket counts. These
    // should be atomically updated such that we never collect the sum without corresponding bucket
    // counts update, or vice verse. This test asserts that the cumulative state at the end is
    // consistent, and interim collects unknowingly see partial writes.
    AtomicLong lastValue = new AtomicLong(0);
    AtomicLong sum = new AtomicLong(0);
    AtomicLong min = new AtomicLong(Long.MAX_VALUE);
    AtomicLong max = new AtomicLong(-1);
    List<Long> bucketCounts = new ArrayList<>();
    for (int i = 0; i < bucketBoundaries.size() + 1; i++) {
      bucketCounts.add(0L);
    }
    AtomicLong totalCount = new AtomicLong(0);
    AtomicLong zeroCount = new AtomicLong(0);
    LongStream.range(0, threadCount)
        .flatMap(i -> measurements.stream().mapToLong(l -> l))
        .forEach(
            measurement -> {
              lastValue.set(measurement);
              sum.addAndGet(measurement);
              min.updateAndGet(v -> Math.min(v, measurement));
              max.updateAndGet(v -> Math.max(v, measurement));
              totalCount.incrementAndGet();
              int bucketIndex =
                  ExplicitBucketHistogramUtils.findBucketIndex(
                      bucketBoundariesArr, (double) measurement);
              bucketCounts.set(bucketIndex, bucketCounts.get(bucketIndex) + 1);
              if (measurement == 0) {
                zeroCount.incrementAndGet();
              }
            });

    boolean isCumulative = aggregationTemporality == AggregationTemporality.CUMULATIVE;
    List<PointData> points =
        Stream.of(attr1, attr2, attr3, attr4)
            .map(attr -> getReducedPointData(collectedMetrics, isCumulative, attr))
            .collect(toList());
    if (instrumentType == SyncInstrumentAggregation.COUNTER_SUM
        || instrumentType == SyncInstrumentAggregation.UP_DOWN_COUNTER_SUM) {
      if (instrumentValueType == InstrumentValueType.DOUBLE) {
        assertThat(points)
            .allSatisfy(
                point ->
                    assertThat(point)
                        .isInstanceOfSatisfying(
                            DoublePointData.class,
                            p -> assertThat(p.getValue()).isEqualTo((double) sum.get())));

      } else {
        assertThat(points)
            .allSatisfy(
                point ->
                    assertThat(point)
                        .isInstanceOfSatisfying(
                            LongPointData.class,
                            p -> assertThat(p.getValue()).isEqualTo(sum.get())));
      }
    } else if (instrumentType == SyncInstrumentAggregation.GAUGE_LAST_VALUE) {
      if (instrumentValueType == InstrumentValueType.DOUBLE) {
        assertThat(points)
            .allSatisfy(
                point ->
                    assertThat(point)
                        .isInstanceOfSatisfying(
                            DoublePointData.class,
                            p -> assertThat(p.getValue()).isEqualTo((double) lastValue.get())));
      } else {
        assertThat(points)
            .allSatisfy(
                point ->
                    assertThat(point)
                        .isInstanceOfSatisfying(
                            LongPointData.class,
                            p -> assertThat(p.getValue()).isEqualTo(lastValue.get())));
      }
    } else if (instrumentType == SyncInstrumentAggregation.HISTOGRAM_EXPLICIT_HISTOGRAM) {
      assertThat(points)
          .allSatisfy(
              point ->
                  assertThat(point)
                      .isInstanceOfSatisfying(
                          HistogramPointData.class,
                          p -> {
                            assertThat(p.getSum()).isEqualTo((double) sum.get());
                            assertThat(p.getMin()).isEqualTo((double) min.get());
                            assertThat(p.getMax()).isEqualTo((double) max.get());
                            assertThat(p.getCount())
                                .isEqualTo(bucketCounts.stream().reduce(0L, Long::sum));
                            assertThat(p.getCounts()).isEqualTo(bucketCounts);
                          }));
    } else if (instrumentType == SyncInstrumentAggregation.HISTOGRAM_EXPONENTIAL_HISTOGRAM) {
      assertThat(points)
          .allSatisfy(
              point ->
                  assertThat(point)
                      .isInstanceOfSatisfying(
                          ExponentialHistogramPointData.class,
                          p -> {
                            assertThat(p.getSum()).isEqualTo((double) sum.get());
                            assertThat(p.getMin()).isEqualTo((double) min.get());
                            assertThat(p.getMax()).isEqualTo((double) max.get());
                            assertThat(p.getZeroCount()).isEqualTo(zeroCount.get());
                            assertThat(
                                    p.getPositiveBuckets().getBucketCounts().stream()
                                        .reduce(0L, Long::sum))
                                .isEqualTo(totalCount.get() - zeroCount.get());
                          }));
    } else {
      throw new IllegalArgumentException();
    }
  }

  private static Stream<Arguments> stressTestArgs() {
    List<Arguments> argumentsList = new ArrayList<>();
    for (AggregationTemporality aggregationTemporality : AggregationTemporality.values()) {
      for (SyncInstrumentAggregation instrumentType : SyncInstrumentAggregation.values()) {
        for (MemoryMode memoryMode : MemoryMode.values()) {
          for (InstrumentValueType instrumentValueType : InstrumentValueType.values()) {
            argumentsList.add(
                Arguments.of(
                    aggregationTemporality, instrumentType, memoryMode, instrumentValueType));
          }
        }
      }
    }
    return argumentsList.stream();
  }

  private static Instrument getInstrument(
      Meter meter,
      SyncInstrumentAggregation instrumentType,
      InstrumentValueType instrumentValueType) {
    switch (instrumentType) {
      case COUNTER_SUM:
        return instrumentValueType == InstrumentValueType.DOUBLE
            ? meter.counterBuilder(instrumentName).ofDoubles().build()::add
            : meter.counterBuilder(instrumentName).build()::add;
      case UP_DOWN_COUNTER_SUM:
        return instrumentValueType == InstrumentValueType.DOUBLE
            ? meter.upDownCounterBuilder(instrumentName).ofDoubles().build()::add
            : meter.upDownCounterBuilder(instrumentName).build()::add;
      case HISTOGRAM_EXPLICIT_HISTOGRAM:
      case HISTOGRAM_EXPONENTIAL_HISTOGRAM:
        return instrumentValueType == InstrumentValueType.DOUBLE
            ? meter
                    .histogramBuilder(instrumentName)
                    .setExplicitBucketBoundariesAdvice(bucketBoundaries)
                    .build()
                ::record
            : meter
                    .histogramBuilder(instrumentName)
                    .setExplicitBucketBoundariesAdvice(bucketBoundaries)
                    .ofLongs()
                    .build()
                ::record;
      case GAUGE_LAST_VALUE:
        return instrumentValueType == InstrumentValueType.DOUBLE
            ? meter.gaugeBuilder(instrumentName).build()::set
            : meter.gaugeBuilder(instrumentName).ofLongs().build()::set;
    }
    throw new IllegalArgumentException();
  }

  private interface Instrument {
    void record(long value, Attributes attributes);
  }

  private static MetricData copy(MetricData m) {
    switch (m.getType()) {
      case LONG_GAUGE:
        return ImmutableMetricData.createLongGauge(
            m.getResource(),
            m.getInstrumentationScopeInfo(),
            m.getName(),
            m.getDescription(),
            m.getUnit(),
            ImmutableGaugeData.create(
                m.getLongGaugeData().getPoints().stream()
                    .map(
                        p ->
                            ImmutableLongPointData.create(
                                p.getStartEpochNanos(),
                                p.getEpochNanos(),
                                p.getAttributes(),
                                p.getValue(),
                                p.getExemplars()))
                    .collect(toList())));
      case DOUBLE_GAUGE:
        return ImmutableMetricData.createDoubleGauge(
            m.getResource(),
            m.getInstrumentationScopeInfo(),
            m.getName(),
            m.getDescription(),
            m.getUnit(),
            ImmutableGaugeData.create(
                m.getDoubleGaugeData().getPoints().stream()
                    .map(
                        p ->
                            ImmutableDoublePointData.create(
                                p.getStartEpochNanos(),
                                p.getEpochNanos(),
                                p.getAttributes(),
                                p.getValue(),
                                p.getExemplars()))
                    .collect(toList())));
      case LONG_SUM:
        return ImmutableMetricData.createLongSum(
            m.getResource(),
            m.getInstrumentationScopeInfo(),
            m.getName(),
            m.getDescription(),
            m.getUnit(),
            ImmutableSumData.create(
                m.getLongSumData().isMonotonic(),
                m.getLongSumData().getAggregationTemporality(),
                m.getLongSumData().getPoints().stream()
                    .map(
                        p ->
                            ImmutableLongPointData.create(
                                p.getStartEpochNanos(),
                                p.getEpochNanos(),
                                p.getAttributes(),
                                p.getValue(),
                                p.getExemplars()))
                    .collect(toList())));
      case DOUBLE_SUM:
        return ImmutableMetricData.createDoubleSum(
            m.getResource(),
            m.getInstrumentationScopeInfo(),
            m.getName(),
            m.getDescription(),
            m.getUnit(),
            ImmutableSumData.create(
                m.getDoubleSumData().isMonotonic(),
                m.getDoubleSumData().getAggregationTemporality(),
                m.getDoubleSumData().getPoints().stream()
                    .map(
                        p ->
                            ImmutableDoublePointData.create(
                                p.getStartEpochNanos(),
                                p.getEpochNanos(),
                                p.getAttributes(),
                                p.getValue(),
                                p.getExemplars()))
                    .collect(toList())));
      case HISTOGRAM:
        return ImmutableMetricData.createDoubleHistogram(
            m.getResource(),
            m.getInstrumentationScopeInfo(),
            m.getName(),
            m.getDescription(),
            m.getUnit(),
            ImmutableHistogramData.create(
                m.getHistogramData().getAggregationTemporality(),
                m.getHistogramData().getPoints().stream()
                    .map(
                        p ->
                            ImmutableHistogramPointData.create(
                                p.getStartEpochNanos(),
                                p.getEpochNanos(),
                                p.getAttributes(),
                                p.getSum(),
                                p.hasMin(),
                                p.getMin(),
                                p.hasMax(),
                                p.getMax(),
                                p.getBoundaries(),
                                new ArrayList<>(p.getCounts()),
                                p.getExemplars()))
                    .collect(toList())));
      case EXPONENTIAL_HISTOGRAM:
        return ImmutableMetricData.createExponentialHistogram(
            m.getResource(),
            m.getInstrumentationScopeInfo(),
            m.getName(),
            m.getDescription(),
            m.getUnit(),
            ImmutableExponentialHistogramData.create(
                m.getExponentialHistogramData().getAggregationTemporality(),
                m.getExponentialHistogramData().getPoints().stream()
                    .map(
                        p ->
                            ImmutableExponentialHistogramPointData.create(
                                p.getScale(),
                                p.getSum(),
                                p.getZeroCount(),
                                p.hasMin(),
                                p.getMin(),
                                p.hasMax(),
                                p.getMax(),
                                ExponentialHistogramBuckets.create(
                                    p.getPositiveBuckets().getScale(),
                                    p.getPositiveBuckets().getOffset(),
                                    new ArrayList<>(p.getPositiveBuckets().getBucketCounts())),
                                ExponentialHistogramBuckets.create(
                                    p.getNegativeBuckets().getScale(),
                                    p.getNegativeBuckets().getOffset(),
                                    new ArrayList<>(p.getPositiveBuckets().getBucketCounts())),
                                p.getStartEpochNanos(),
                                p.getEpochNanos(),
                                p.getAttributes(),
                                p.getExemplars()))
                    .collect(toList())));
      case SUMMARY:
    }
    throw new IllegalArgumentException();
  }

  /**
   * Reduce a list of metric data assumed to be uniform and for a single instrument to a single
   * point data. If cumulative, return the last point data. If delta, merge the data points.
   */
  private static PointData getReducedPointData(
      List<MetricData> metrics, boolean isCumulative, Attributes attributes) {
    metrics.stream()
        .forEach(metricData -> assertThat(metricData.getName()).isEqualTo(instrumentName));
    MetricData first = metrics.get(0);
    switch (first.getType()) {
      case LONG_GAUGE:
        List<LongPointData> lgaugePoints =
            metrics.stream()
                .flatMap(m -> m.getLongGaugeData().getPoints().stream())
                .filter(p -> attributes.equals(p.getAttributes()))
                .collect(toList());
        return lgaugePoints.get(lgaugePoints.size() - 1);
      case DOUBLE_GAUGE:
        List<DoublePointData> dgaugePoints =
            metrics.stream()
                .flatMap(m -> m.getDoubleGaugeData().getPoints().stream())
                .filter(p -> attributes.equals(p.getAttributes()))
                .collect(toList());
        return dgaugePoints.get(dgaugePoints.size() - 1);
      case LONG_SUM:
        List<LongPointData> lsumPoints =
            metrics.stream()
                .flatMap(m -> m.getLongSumData().getPoints().stream())
                .filter(p -> attributes.equals(p.getAttributes()))
                .collect(toList());
        return isCumulative
            ? lsumPoints.get(lsumPoints.size() - 1)
            : lsumPoints.stream()
                .reduce(
                    ImmutableLongPointData.create(0, 0, empty(), 0),
                    (p1, p2) ->
                        ImmutableLongPointData.create(
                            0, 0, empty(), p1.getValue() + p2.getValue(), emptyList()));
      case DOUBLE_SUM:
        List<DoublePointData> dsumPoints =
            metrics.stream()
                .flatMap(m -> m.getDoubleSumData().getPoints().stream())
                .filter(p -> attributes.equals(p.getAttributes()))
                .collect(toList());
        return isCumulative
            ? dsumPoints.get(dsumPoints.size() - 1)
            : dsumPoints.stream()
                .reduce(
                    ImmutableDoublePointData.create(0, 0, empty(), 0),
                    (p1, p2) ->
                        ImmutableDoublePointData.create(
                            0, 0, empty(), p1.getValue() + p2.getValue(), emptyList()));
      case HISTOGRAM:
        List<HistogramPointData> histPoints =
            metrics.stream()
                .flatMap(m -> m.getHistogramData().getPoints().stream())
                .filter(p -> attributes.equals(p.getAttributes()))
                .collect(toList());
        return isCumulative
            ? histPoints.get(histPoints.size() - 1)
            : histPoints.stream()
                .reduce(
                    ImmutableHistogramPointData.create(
                        0,
                        0,
                        empty(),
                        0,
                        /* hasMin= */ true,
                        0,
                        /* hasMax= */ true,
                        0,
                        emptyList(),
                        singletonList(0L)),
                    (p1, p2) ->
                        ImmutableHistogramPointData.create(
                            0,
                            0,
                            empty(),
                            p1.getSum() + p2.getSum(),
                            p1.hasMin() || p2.hasMin(),
                            Math.min(p1.getMin(), p2.getMin()),
                            p2.hasMax() || p1.hasMax(),
                            Math.max(p1.getMax(), p2.getMax()),
                            p2.getBoundaries(),
                            mergeBuckets(p1.getCounts(), p2.getCounts())));
      case EXPONENTIAL_HISTOGRAM:
        List<ExponentialHistogramPointData> expoHistPoints =
            metrics.stream()
                .flatMap(m -> m.getExponentialHistogramData().getPoints().stream())
                .filter(p -> attributes.equals(p.getAttributes()))
                .collect(toList());
        return isCumulative
            ? expoHistPoints.get(expoHistPoints.size() - 1)
            : expoHistPoints.stream()
                .reduce(
                    // NOTE: we're only testing the correctness of sum, count, min, and max, so we
                    // skip the complexity of correctly merge which involves re-bucketing when the
                    // scale changes. The result is bucket counts with meaningless values, but
                    // correct aggregate counts.
                    ImmutableExponentialHistogramPointData.create(
                        0,
                        0,
                        0,
                        /* hasMin= */ true,
                        0,
                        /* hasMax= */ true,
                        0,
                        ExponentialHistogramBuckets.create(0, 0, emptyList()),
                        ExponentialHistogramBuckets.create(0, 0, emptyList()),
                        0,
                        0,
                        empty(),
                        emptyList()),
                    (p1, p2) ->
                        ImmutableExponentialHistogramPointData.create(
                            Math.min(p1.getScale(), p2.getScale()),
                            p1.getSum() + p2.getSum(),
                            p1.getZeroCount() + p2.getZeroCount(),
                            p1.hasMin() || p2.hasMin(),
                            Math.min(p1.getMin(), p2.getMin()),
                            p1.hasMax() || p2.hasMax(),
                            Math.max(p1.getMax(), p2.getMax()),
                            ExponentialHistogramBuckets.create(
                                0,
                                0,
                                mergeBuckets(
                                    p1.getPositiveBuckets().getBucketCounts(),
                                    p2.getPositiveBuckets().getBucketCounts())),
                            ExponentialHistogramBuckets.create(
                                0,
                                0,
                                mergeBuckets(
                                    p1.getNegativeBuckets().getBucketCounts(),
                                    p2.getNegativeBuckets().getBucketCounts())),
                            0,
                            0,
                            empty(),
                            emptyList()));
      case SUMMARY:
    }
    throw new IllegalArgumentException();
  }

  private static List<Long> mergeBuckets(List<Long> l1, List<Long> l2) {
    int size = Math.max(l1.size(), l2.size());
    List<Long> merged = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      long mergedCount = 0;
      if (i < l1.size()) {
        mergedCount += l1.get(i);
      }
      if (i < l2.size()) {
        mergedCount += l2.get(i);
      }
      merged.add(mergedCount);
    }
    return merged;
  }

  /**
   * Enum that is the composite of the instrument type and aggregation. {@link InstrumentType} would
   * be preferred, but doesn't include an option for the exponential histogram aggregation.
   */
  private enum SyncInstrumentAggregation {
    COUNTER_SUM,
    UP_DOWN_COUNTER_SUM,
    GAUGE_LAST_VALUE,
    HISTOGRAM_EXPLICIT_HISTOGRAM,
    HISTOGRAM_EXPONENTIAL_HISTOGRAM
  }
}
