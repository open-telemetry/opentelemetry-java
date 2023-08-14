package io.opentelemetry.sdk.metrics.internal.aggregator.prototype;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.aggregator.HistogramCollectBenchmark;
import io.opentelemetry.sdk.metrics.internal.aggregator.HistogramCollectBenchmark.NoopMetricExporter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
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


@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(iterations = 20, batchSize = 100)
@Warmup(iterations = 10, batchSize = 10)
@Fork(1)
public class AsynchronousMetricStorageGarbageCollectionBenchmark {

  @State(value = Scope.Benchmark)
  @SuppressWarnings("SystemOut")
  public static class ThreadState {
    private final int cardinality;
    private final int countersCount;
    @Param public AggregationTemporality aggregationTemporality;
    SdkMeterProvider sdkMeterProvider;
    private final Random random = new Random();
    List<Attributes> attributesList;

    /** Creates a ThreadState. */
    public ThreadState() {
      cardinality = 1000;
      countersCount = 10;
    }

    public ThreadState(int countersCount, int cardinality) {
      this.cardinality = cardinality;
      this.countersCount = countersCount;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Setup
    public void setup() {
      PeriodicMetricReader metricReader =
          PeriodicMetricReader.builder(
                  // Configure an exporter that configures the temporality and aggregation
                  // for the test case, but otherwise drops the data on export
                  new NoopMetricExporter(aggregationTemporality, Aggregation.sum()))
              // Effectively disable periodic reading so reading is only done on #flush()
              .setInterval(Duration.ofSeconds(Integer.MAX_VALUE))
              .build();
      SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
      SdkMeterProviderUtil.registerMetricReaderWithCardinalitySelector(
          builder, metricReader, unused -> cardinality + 1);

      attributesList = AttributesGenerator.generate(cardinality);

      // Disable examplars
      SdkMeterProviderUtil.setExemplarFilter(builder, ExemplarFilter.alwaysOff());

      sdkMeterProvider = builder.build();
      for (int i = 0; i < countersCount; i++) {
        sdkMeterProvider
            .get("meter")
            .counterBuilder("counter" + i)
            .buildWithCallback(
                observableLongMeasurement -> {
                  for (int j = 0; j < attributesList.size(); j++) {
                    Attributes attributes = attributesList.get(j);
                    observableLongMeasurement.record(random.nextInt(10_000), attributes);
                  }
                });
      }
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
    threadState.sdkMeterProvider.forceFlush().join(10, TimeUnit.SECONDS);
  }
}
