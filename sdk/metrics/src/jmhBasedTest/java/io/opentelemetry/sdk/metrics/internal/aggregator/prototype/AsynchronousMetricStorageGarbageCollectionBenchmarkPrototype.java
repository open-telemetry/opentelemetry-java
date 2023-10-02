package io.opentelemetry.sdk.metrics.internal.aggregator.prototype;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MetricFilter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import java.time.Duration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
public class AsynchronousMetricStorageGarbageCollectionBenchmarkPrototype {

  public enum Filter {
    NO_FILTER,
    WITH_FILTER
  }
  @State(value = Scope.Benchmark)
  @SuppressWarnings("SystemOut")
  public static class ThreadState {
    private final int cardinality;
    private final int countersCount;
    private final int attributesToAllow;
    private final int countersToAllow;
    @Param public AggregationTemporality aggregationTemporality;
    public MemoryMode memoryMode = MemoryMode.REUSABLE_DATA;
    @Param public Filter filter;
    SdkMeterProvider sdkMeterProvider;
    private final Random random = new Random();
    List<Attributes> attributesList;

    /** Creates a ThreadState. */
    @SuppressWarnings("unused")
    public ThreadState() {
      cardinality = 1000;
      countersCount = 10;
      attributesToAllow = 50;
      countersToAllow = 1;
    }

    public ThreadState(
        int countersCount,
        int cardinality,
        int attributesToAllow,
        int countersToAllow) {
      this.cardinality = cardinality;
      this.countersCount = countersCount;
      this.attributesToAllow = attributesToAllow;
      this.countersToAllow = countersToAllow;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Setup
    public void setup() {
      PeriodicMetricReader metricReader =
          PeriodicMetricReader.builder(
                  // Configure an exporter that configures the temporality and aggregation
                  // for the test case, but otherwise drops the data on export
                  new NoopMetricExporter(aggregationTemporality, Aggregation.sum(), memoryMode))
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
            .counterBuilder(counterName(i))
            .buildWithCallback(
                observableLongMeasurement -> {
                  for (int j = 0; j < attributesList.size(); j++) {
                    Attributes attributes = attributesList.get(j);
                    observableLongMeasurement.record(random.nextInt(10_000), attributes);
                  }
                });
      }
      if (filter == Filter.WITH_FILTER) {
        metricReader.setMetricFilter(
            new TestMetricFilter(attributesList, attributesToAllow, countersToAllow));
      }
    }

    @TearDown
    public void tearDown() {
      sdkMeterProvider.shutdown().join(10, TimeUnit.SECONDS);
    }
  }

  private static String counterName(int i) {
    return "counter" + i;
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

  static class TestMetricFilter implements MetricFilter {

    public static final AttributeKey<String> KEY_ATTRIBUTE_NAME = AttributeKey.stringKey("key");
    private final Set<String> allowsCounterNames = new HashSet<>();
    private final Set<String> allowsKeyAttributeNames = new HashSet<>();

    public TestMetricFilter(
        List<Attributes> generatedAttributes,
        int numOfAttributesToAllow,
        int allowCounterUpTo) {
      for (int i = 0; i < allowCounterUpTo; i++) {
        allowsCounterNames.add(counterName(i));
      }
      Iterator<Attributes> iterator = generatedAttributes.iterator();
      for (int i = 0; i < numOfAttributesToAllow; i++) {
        allowsKeyAttributeNames.add(iterator.next().get(KEY_ATTRIBUTE_NAME));
      }
    }

    @Override
    public InstrumentFilterResult filterInstrument(
        InstrumentationScopeInfo instrumentationScopeInfo,
        String name,
        InstrumentType metricDataType,
        String unit) {

      if (allowsCounterNames.contains(name)) {
        return InstrumentFilterResult.ALLOW_SOME_ATTRIBUTES;
      } else {
        return InstrumentFilterResult.REJECT_ALL_ATTRIBUTES;
      }
    }

    @Override
    public boolean allowInstrumentAttributes(InstrumentationScopeInfo instrumentationScopeInfo,
        String name, InstrumentType metricDataType, String unit, Attributes attributes) {

      return (allowsCounterNames.contains(name)
          && allowsKeyAttributeNames.contains(attributes.get(KEY_ATTRIBUTE_NAME)));
    }
  }
}
