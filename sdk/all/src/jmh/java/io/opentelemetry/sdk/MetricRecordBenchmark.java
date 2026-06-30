/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static io.opentelemetry.sdk.metrics.InstrumentType.COUNTER;
import static io.opentelemetry.sdk.metrics.InstrumentType.GAUGE;
import static io.opentelemetry.sdk.metrics.InstrumentType.HISTOGRAM;
import static io.opentelemetry.sdk.metrics.InstrumentType.UP_DOWN_COUNTER;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundDoubleCounter;
import io.opentelemetry.api.incubator.metrics.BoundDoubleGauge;
import io.opentelemetry.api.incubator.metrics.BoundDoubleHistogram;
import io.opentelemetry.api.incubator.metrics.BoundDoubleUpDownCounter;
import io.opentelemetry.api.incubator.metrics.BoundLongCounter;
import io.opentelemetry.api.incubator.metrics.BoundLongGauge;
import io.opentelemetry.api.incubator.metrics.BoundLongHistogram;
import io.opentelemetry.api.incubator.metrics.BoundLongUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.ExemplarFilter;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.ThreadParams;

/**
 * This benchmark measures the performance of recording metrics. It includes the following
 * dimensions:
 *
 * <ul>
 *   <li>{@link BenchmarkState#instrumentTypeAndAggregation} composite of {@link InstrumentType} and
 *       {@link Aggregation}, including all relevant combinations for synchronous instruments.
 *   <li>{@link BenchmarkState#aggregationTemporality}
 *   <li>{@link BenchmarkState#cardinality}
 *   <li>{@link BenchmarkState#bound} whether recording goes through bound instruments ({@code
 *       Extended*#bind(Attributes)}) or unbound instruments.
 *   <li>thread count
 *   <li>{@link BenchmarkState#instrumentValueType}, {@link BenchmarkState#memoryMode}, and {@link
 *       BenchmarkState#exemplars} are disabled to reduce combinatorial explosion.
 * </ul>
 *
 * <p>Each operation consists of recording {@link MetricRecordBenchmark#RECORDS_PER_INVOCATION}
 * measurements.
 *
 * <p>The cardinality and thread count dimensions partially overlap. Cardinality dictates how many
 * unique attribute sets (i.e. series) are recorded to, and thread count dictates how many threads
 * are simultaneously recording to those series. For unbound instruments, the record path looks up
 * an aggregation handle for the series corresponding to the measurement's {@link Attributes} in a
 * {@link java.util.concurrent.ConcurrentHashMap}; for bound instruments ({@link
 * BenchmarkState#bound}) that handle is resolved once at bind time, so the record path skips the
 * lookup and attribute processing entirely. The cardinality dictates the size of this map, which
 * has some impact on performance. However, by far the dominant bottleneck is contention. That is,
 * the number of threads simultaneously trying to record to the same series. Increasing the threads
 * increases contention. Increasing cardinality decreases contention, as the threads are now
 * spreading their record activities over more distinct series. The highest contention scenario is
 * cardinality=1, threads=4. Any scenario with threads=1 has zero contention.
 *
 * <p>To make the cardinality dimension meaningful under contention, each thread starts at a
 * staggered offset into the series (see {@link #record}), so threads record to different series
 * rather than marching through the same one in lockstep. A naive shared {@code i % cardinality}
 * index would instead start every thread on the same series, and contention's self-synchronizing
 * effect keeps them aligned, collapsing high-cardinality multi-thread runs into a single rotating
 * hotspot that behaves like cardinality=1.
 *
 * <p>It's useful to characterize the performance of the metrics system under contention, as some
 * high-performance applications may have many threads trying to record to the same series. It's
 * also useful to characterize the performance of the metrics system under low contention, as some
 * high-performance applications may not frequently be trying to concurrently record to the same
 * series yet still care about the overhead of each record operation.
 *
 * <p>{@link AggregationTemporality} can impact performance because additional concurrency controls
 * are needed to ensure there are no duplicate, partial, or lost writes while resetting the set of
 * timeseries each collection.
 */
public class MetricRecordBenchmark {

  private static final int INITIAL_SEED = 513423236;
  private static final int MAX_THREADS = 4;
  private static final int RECORDS_PER_INVOCATION = BenchmarkUtils.RECORDS_PER_INVOCATION;

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    @Param InstrumentTypeAndAggregation instrumentTypeAndAggregation;

    @Param AggregationTemporality aggregationTemporality;

    @Param({"1", "128"})
    int cardinality;

    // Whether to record through bound instruments (Extended*#bind(Attributes)), which resolve the
    // timeseries once up front, or unbound instruments, which look up the timeseries by Attributes
    // on every record.
    @Param({"false", "true"})
    boolean bound;

    // The following parameters are excluded from the benchmark to reduce combinatorial explosion
    // but can optionally be enabled for adhoc evaluation.

    // InstrumentValueType doesn't materially impact performance. Uncomment to evaluate.
    // @Param
    // InstrumentValueType instrumentValueType;
    InstrumentValueType instrumentValueType = InstrumentValueType.LONG;

    // MemoryMode almost exclusively impacts collect from a performance standpoint. Uncomment to
    // evaluate.
    // @Param
    // MemoryMode memoryMode;
    MemoryMode memoryMode = MemoryMode.REUSABLE_DATA;

    // Exemplars can impact performance, but we skip evaluation to limit test cases. Uncomment to
    // evaluate.
    // @Param({"true", "false"})
    // boolean exemplars;
    boolean exemplars = false;

    OpenTelemetrySdk openTelemetry;
    // Populated when bound == false.
    private Instrument instrument;
    // Populated when bound == true; parallel to attributesList (one bound instrument per series).
    private List<BoundInstrument> boundInstruments;
    List<Long> measurements;
    List<Attributes> attributesList;
    Span span;
    io.opentelemetry.context.Scope contextScope;

    @Setup
    @SuppressWarnings("MustBeClosedChecker")
    public void setup() {
      InstrumentType instrumentType = instrumentTypeAndAggregation.instrumentType;
      Aggregation aggregation = instrumentTypeAndAggregation.aggregation;

      openTelemetry =
          OpenTelemetrySdk.builder()
              .setTracerProvider(SdkTracerProvider.builder().setSampler(Sampler.alwaysOn()).build())
              .setMeterProvider(
                  SdkMeterProvider.builder()
                      .registerMetricReader(
                          InMemoryMetricReader.builder()
                              .setAggregationTemporalitySelector(unused -> aggregationTemporality)
                              .setDefaultAggregationSelector(
                                  DefaultAggregationSelector.getDefault()
                                      .with(instrumentType, aggregation))
                              .setMemoryMode(memoryMode)
                              .build())
                      .setExemplarFilter(
                          exemplars ? ExemplarFilter.traceBased() : ExemplarFilter.alwaysOff())
                      .build())
              .build();

      Meter meter = openTelemetry.getMeter("benchmark");
      Tracer tracer = openTelemetry.getTracer("benchmark");
      span = tracer.spanBuilder("benchmark").startSpan();
      // We suppress warnings on closing here, as we rely on tests to make sure context is closed.
      contextScope = span.makeCurrent();

      Random random = new Random(INITIAL_SEED);
      attributesList = new ArrayList<>(cardinality);
      AttributeKey<String> key = AttributeKey.stringKey("key");
      String last = "aaaaaaaaaaaaaaaaaaaaaaaaaa";
      for (int i = 0; i < cardinality; i++) {
        char[] chars = last.toCharArray();
        chars[random.nextInt(last.length())] = (char) (random.nextInt(26) + 'a');
        last = new String(chars);
        attributesList.add(Attributes.of(key, last));
      }
      Collections.shuffle(attributesList);

      if (bound) {
        boundInstruments =
            bindInstruments(meter, instrumentType, instrumentValueType, attributesList);
      } else {
        instrument = getInstrument(meter, instrumentType, instrumentValueType);
      }

      measurements = new ArrayList<>(RECORDS_PER_INVOCATION);
      for (int i = 0; i < RECORDS_PER_INVOCATION; i++) {
        measurements.add((long) random.nextInt(2000));
      }
      Collections.shuffle(measurements);
    }

    @TearDown
    public void tearDown() {
      contextScope.close();
      span.end();
      openTelemetry.shutdown();
    }
  }

  @Benchmark
  @Group("threads1")
  @GroupThreads(1)
  @Fork(3)
  @Warmup(iterations = 3, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OperationsPerInvocation(RECORDS_PER_INVOCATION)
  public void record_SingleThread(BenchmarkState benchmarkState, ThreadParams threadParams) {
    record(benchmarkState, threadParams);
  }

  @Benchmark
  @Group("threads" + MAX_THREADS)
  @GroupThreads(MAX_THREADS)
  @Fork(3)
  @Warmup(iterations = 3, time = 1)
  @Measurement(iterations = 10, time = 1)
  @OperationsPerInvocation(RECORDS_PER_INVOCATION)
  public void record_MultipleThreads(BenchmarkState benchmarkState, ThreadParams threadParams) {
    record(benchmarkState, threadParams);
  }

  private static void record(BenchmarkState benchmarkState, ThreadParams threadParams) {
    int cardinality = benchmarkState.attributesList.size();
    // Stagger each thread's starting series so threads don't march through the same series in
    // lockstep (which would collapse high-cardinality multi-thread runs into a single hotspot).
    // Single thread / cardinality=1 => offset 0, i.e. plain sequential access.
    int offset = threadParams.getThreadIndex() * (cardinality / threadParams.getThreadCount());
    if (benchmarkState.bound) {
      // Bound: record straight to the pre-resolved bound instrument for the series (no per-record
      // Attributes lookup). Indexed identically to the unbound path so the access pattern matches.
      List<BoundInstrument> boundInstruments = benchmarkState.boundInstruments;
      for (int i = 0; i < RECORDS_PER_INVOCATION; i++) {
        long value = benchmarkState.measurements.get(i % benchmarkState.measurements.size());
        boundInstruments.get((i + offset) % cardinality).record(value);
      }
    } else {
      for (int i = 0; i < RECORDS_PER_INVOCATION; i++) {
        Attributes attributes = benchmarkState.attributesList.get((i + offset) % cardinality);
        long value = benchmarkState.measurements.get(i % benchmarkState.measurements.size());
        benchmarkState.instrument.record(value, attributes);
      }
    }
  }

  @SuppressWarnings("ImmutableEnumChecker")
  public enum InstrumentTypeAndAggregation {
    COUNTER_SUM(COUNTER, Aggregation.sum()),
    UP_DOWN_COUNTER_SUM(UP_DOWN_COUNTER, Aggregation.sum()),
    GAUGE_LAST_VALUE(GAUGE, Aggregation.lastValue()),
    HISTOGRAM_EXPLICIT(HISTOGRAM, Aggregation.explicitBucketHistogram()),
    HISTOGRAM_BASE2_EXPONENTIAL(HISTOGRAM, Aggregation.base2ExponentialBucketHistogram());

    InstrumentTypeAndAggregation(InstrumentType instrumentType, Aggregation aggregation) {
      this.instrumentType = instrumentType;
      this.aggregation = aggregation;
    }

    private final InstrumentType instrumentType;
    private final Aggregation aggregation;
  }

  private interface Instrument {
    void record(long value, Attributes attributes);
  }

  private static Instrument getInstrument(
      Meter meter, InstrumentType instrumentType, InstrumentValueType instrumentValueType) {
    String name = "instrument";
    switch (instrumentType) {
      case COUNTER:
        return instrumentValueType == InstrumentValueType.DOUBLE
            ? meter.counterBuilder(name).ofDoubles().build()::add
            : meter.counterBuilder(name).build()::add;
      case UP_DOWN_COUNTER:
        return instrumentValueType == InstrumentValueType.DOUBLE
            ? meter.upDownCounterBuilder(name).ofDoubles().build()::add
            : meter.upDownCounterBuilder(name).build()::add;
      case HISTOGRAM:
        return instrumentValueType == InstrumentValueType.DOUBLE
            ? meter.histogramBuilder(name).build()::record
            : meter.histogramBuilder(name).ofLongs().build()::record;
      case GAUGE:
        return instrumentValueType == InstrumentValueType.DOUBLE
            ? meter.gaugeBuilder(name).build()::set
            : meter.gaugeBuilder(name).ofLongs().build()::set;
      case OBSERVABLE_COUNTER:
      case OBSERVABLE_UP_DOWN_COUNTER:
      case OBSERVABLE_GAUGE:
    }
    throw new IllegalArgumentException();
  }

  @FunctionalInterface
  private interface BoundInstrument {
    void record(long value);
  }

  /**
   * Builds the instrument, then binds one {@link BoundInstrument} per series in {@code
   * attributesList}, returned in the same order so the record loop can index it identically to the
   * unbound path.
   */
  private static List<BoundInstrument> bindInstruments(
      Meter meter,
      InstrumentType instrumentType,
      InstrumentValueType instrumentValueType,
      List<Attributes> attributesList) {
    boolean isDouble = instrumentValueType == InstrumentValueType.DOUBLE;
    String name = "instrument";
    List<BoundInstrument> result = new ArrayList<>(attributesList.size());
    switch (instrumentType) {
      case COUNTER:
        if (isDouble) {
          ExtendedDoubleCounter instrument =
              (ExtendedDoubleCounter) meter.counterBuilder(name).ofDoubles().build();
          for (Attributes attributes : attributesList) {
            BoundDoubleCounter bound = instrument.bind(attributes);
            result.add(bound::add);
          }
        } else {
          ExtendedLongCounter instrument = (ExtendedLongCounter) meter.counterBuilder(name).build();
          for (Attributes attributes : attributesList) {
            BoundLongCounter bound = instrument.bind(attributes);
            result.add(bound::add);
          }
        }
        return result;
      case UP_DOWN_COUNTER:
        if (isDouble) {
          ExtendedDoubleUpDownCounter instrument =
              (ExtendedDoubleUpDownCounter) meter.upDownCounterBuilder(name).ofDoubles().build();
          for (Attributes attributes : attributesList) {
            BoundDoubleUpDownCounter bound = instrument.bind(attributes);
            result.add(bound::add);
          }
        } else {
          ExtendedLongUpDownCounter instrument =
              (ExtendedLongUpDownCounter) meter.upDownCounterBuilder(name).build();
          for (Attributes attributes : attributesList) {
            BoundLongUpDownCounter bound = instrument.bind(attributes);
            result.add(bound::add);
          }
        }
        return result;
      case HISTOGRAM:
        if (isDouble) {
          ExtendedDoubleHistogram instrument =
              (ExtendedDoubleHistogram) meter.histogramBuilder(name).build();
          for (Attributes attributes : attributesList) {
            BoundDoubleHistogram bound = instrument.bind(attributes);
            result.add(bound::record);
          }
        } else {
          ExtendedLongHistogram instrument =
              (ExtendedLongHistogram) meter.histogramBuilder(name).ofLongs().build();
          for (Attributes attributes : attributesList) {
            BoundLongHistogram bound = instrument.bind(attributes);
            result.add(bound::record);
          }
        }
        return result;
      case GAUGE:
        if (isDouble) {
          ExtendedDoubleGauge instrument = (ExtendedDoubleGauge) meter.gaugeBuilder(name).build();
          for (Attributes attributes : attributesList) {
            BoundDoubleGauge bound = instrument.bind(attributes);
            result.add(bound::set);
          }
        } else {
          ExtendedLongGauge instrument =
              (ExtendedLongGauge) meter.gaugeBuilder(name).ofLongs().build();
          for (Attributes attributes : attributesList) {
            BoundLongGauge bound = instrument.bind(attributes);
            result.add(bound::set);
          }
        }
        return result;
      case OBSERVABLE_COUNTER:
      case OBSERVABLE_UP_DOWN_COUNTER:
      case OBSERVABLE_GAUGE:
    }
    throw new IllegalArgumentException();
  }
}
