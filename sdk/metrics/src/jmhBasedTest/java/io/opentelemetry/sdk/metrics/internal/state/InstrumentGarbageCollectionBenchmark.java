/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.state.TestInstrumentType.InstrumentTester;
import io.opentelemetry.sdk.metrics.internal.state.TestInstrumentType.TestInstrumentsState;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Run this through {@link InstrumentGarbageCollectionBenchmarkTest}, as it runs it embedded with
 * the GC profiler which what this test designed for (No need for command line run).
 *
 * <p>This test creates 10 asynchronous counters (any asynchronous instrument will do as the code
 * path is almost the same for all async instrument types), and 1000 attribute sets. Each time the
 * test runs, it calls `flush` which effectively calls the callback for each counter. Each such
 * callback records a random number for each of the 1000 attribute sets. The result list ends up in
 * {@link NoopMetricExporter} which does nothing with it.
 *
 * <p>This is repeated 100 times, collectively called Operation in the statistics and each such
 * operation is repeated 20 times - known as Iteration.
 *
 * <p>Each such test is repeated, with a brand new JVM, for all combinations of {@link MemoryMode}
 * and {@link AggregationTemporality}. This is done since each combination has a different code
 * path.
 */
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(iterations = 10, batchSize = 10)
@Warmup(iterations = 10, batchSize = 10)
@Fork(1)
public class InstrumentGarbageCollectionBenchmark {

  @State(value = Scope.Benchmark)
  public static class ThreadState {
    private final int cardinality;
    private final int instrumentCount;
    @Param public TestInstrumentType testInstrumentType;
    @Param public AggregationTemporality aggregationTemporality;
    @Param public MemoryMode memoryMode;
    SdkMeterProvider sdkMeterProvider;
    private final Random random = new Random();
    List<Attributes> attributesList;
    private TestInstrumentsState testInstrumentsState;
    private InstrumentTester instrumentTester;

    /** Creates a ThreadState. */
    @SuppressWarnings("unused")
    public ThreadState() {
      cardinality = 1000;
      instrumentCount = 10;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Setup
    public void setup() {
      instrumentTester = testInstrumentType.createInstrumentTester();
      PeriodicMetricReader metricReader =
          PeriodicMetricReader.builder(
                  // Configure an exporter that configures the temporality and aggregation
                  // for the test case, but otherwise drops the data on export
                  new NoopMetricExporter(
                      aggregationTemporality, instrumentTester.testedAggregation(), memoryMode))
              // Effectively disable periodic reading so reading is only done on #flush()
              .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
              .build();
      SdkMeterProviderBuilder builder =
          SdkMeterProvider.builder().registerMetricReader(metricReader, unused -> cardinality + 1);

      attributesList = AttributesGenerator.generate(cardinality);

      // Disable exemplars
      SdkMeterProviderUtil.setExemplarFilter(builder, ExemplarFilter.alwaysOff());

      sdkMeterProvider = builder.build();
      testInstrumentsState =
          instrumentTester.buildInstruments(
              instrumentCount, sdkMeterProvider, attributesList, random);
    }

    @TearDown
    public void tearDown() {
      sdkMeterProvider.shutdown().join(10, TimeUnit.SECONDS);
    }
  }

  /**
   * Collects all asynchronous instruments metric data.
   *
   * @param threadState thread-state
   */
  @Benchmark
  @Threads(value = 1)
  public void recordAndCollect(ThreadState threadState) {
    threadState.instrumentTester.recordValuesInInstruments(
        threadState.testInstrumentsState, threadState.attributesList, threadState.random);
    threadState.sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
  }
}
