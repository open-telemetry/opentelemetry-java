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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * This benchmark measures the performance of recording metrics and includes the following
 * dimensions:
 *
 * <ul>
 *   <li>{@link BenchmarkState#instrumentTypeAndAggregation} composite of {@link InstrumentType} and
 *       {@link Aggregation}, including all relevant combinations for synchronous instruments.
 *   <li>{@link BenchmarkState#aggregationTemporality}
 *   <li>{@link BenchmarkState#cardinality}
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
 * are simultaneously recording to those series. In all cases, the record path needs to look up an
 * aggregation handle for the series corresponding to the measurement's {@link Attributes} in a
 * {@link java.util.concurrent.ConcurrentHashMap}. That will be the case until otel adds support for
 * <a href="https://github.com/open-telemetry/opentelemetry-specification/issues/4126">bound
 * instruments</a>. The cardinality dictates the size of this map, which has some impact on
 * performance. However, by far the dominant bottleneck is contention. That is, the number of
 * threads simultaneously trying to record to the same series. Increasing the threads increases
 * contention. Increasing cardinality decreases contention, as the threads are now spreading their
 * record activities over more distinct series. The highest contention scenario is cardinality=1,
 * threads=4. Any scenario with threads=1 has zero contention.
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

    @Param({"1", "100"})
    int cardinality;

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
    Instrument instrument;
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
      instrument = getInstrument(meter, instrumentType, instrumentValueType);
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
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  public void record_SingleThread(BenchmarkState benchmarkState) {
    record(benchmarkState);
  }

  @Benchmark
  @Group("threads" + MAX_THREADS)
  @GroupThreads(MAX_THREADS)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  public void record_MultipleThreads(BenchmarkState benchmarkState) {
    record(benchmarkState);
  }

  private static void record(BenchmarkState benchmarkState) {
    for (int i = 0; i < RECORDS_PER_INVOCATION; i++) {
      Attributes attributes =
          benchmarkState.attributesList.get(i % benchmarkState.attributesList.size());
      long value = benchmarkState.measurements.get(i % benchmarkState.measurements.size());
      benchmarkState.instrument.record(value, attributes);
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
}
