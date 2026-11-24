/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGaugeBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogramBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGaugeBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogramBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.assertj.AbstractPointAssert;
import io.opentelemetry.sdk.testing.assertj.MetricAssert;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OptInAdviceTest {

  private static final Attributes ATTRIBUTES =
      Attributes.builder()
          .put(stringKey("key1"), "1")
          .put(stringKey("key2"), "2")
          .put(stringKey("key3"), "3")
          .build();

  private SdkMeterProvider meterProvider = SdkMeterProvider.builder().build();

  @AfterEach
  void cleanup() {
    meterProvider.close();
  }

  @ParameterizedTest
  @MethodSource("instruments")
  void optInInstrumentDisabledByDefault(InstrumentFactory instrumentFactory) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    TestInstrument instrument =
        instrumentFactory.create(meterProvider.get("meter"), "test", /* optIn= */ true);
    assertThat(instrument.isEnabled()).isFalse();
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("instruments")
  void optInFalseInstrumentEnabledByDefault(InstrumentFactory instrumentFactory) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    TestInstrument instrument =
        instrumentFactory.create(meterProvider.get("meter"), "test", /* optIn= */ false);
    assertThat(instrument.isEnabled()).isTrue();
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(metric -> instrument.assertHasAttributes(assertThat(metric), ATTRIBUTES));
  }

  @ParameterizedTest
  @MethodSource("instruments")
  void optInInstrumentEnabledByView(InstrumentFactory instrumentFactory) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ViewBuilder viewBuilder = View.builder();
    SdkMeterProviderUtil.setEnabled(viewBuilder, /* enabled= */ true);
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(InstrumentSelector.builder().setName("test").build(), viewBuilder.build())
            .build();

    TestInstrument instrument =
        instrumentFactory.create(meterProvider.get("meter"), "test", /* optIn= */ true);
    assertThat(instrument.isEnabled()).isTrue();
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(metric -> instrument.assertHasAttributes(assertThat(metric), ATTRIBUTES));
  }

  @ParameterizedTest
  @MethodSource("instruments")
  void optInInstrumentNotEnabledByViewWithoutEnabledTrue(InstrumentFactory instrumentFactory) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test").build(),
                View.builder().setDescription("custom description").build())
            .build();

    TestInstrument instrument =
        instrumentFactory.create(meterProvider.get("meter"), "test", /* optIn= */ true);
    assertThat(instrument.isEnabled()).isFalse();
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics()).isEmpty();
  }

  @Test
  void optInCounterNotEnabledByViewWithAggregationOnly() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test_counter").build(),
                View.builder().setAggregation(Aggregation.sum()).build())
            .build();

    ExtendedLongCounter counter =
        (ExtendedLongCounter)
            ((ExtendedLongCounterBuilder) meterProvider.get("meter").counterBuilder("test_counter"))
                .setOptInAdvice(true)
                .build();
    assertThat(counter.isEnabled()).isFalse();
    counter.add(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("instruments")
  void nonOptInInstrumentDisabledByViewEnabledFalse(InstrumentFactory instrumentFactory) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ViewBuilder viewBuilder = View.builder();
    SdkMeterProviderUtil.setEnabled(viewBuilder, /* enabled= */ false);
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(InstrumentSelector.builder().setName("test").build(), viewBuilder.build())
            .build();

    TestInstrument instrument =
        instrumentFactory.create(meterProvider.get("meter"), "test", /* optIn= */ false);
    assertThat(instrument.isEnabled()).isFalse();
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics()).isEmpty();
  }

  @Test
  void nonOptInCounterDisabledByViewEnabledFalseOverridesAggregation() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ViewBuilder viewBuilder =
        View.builder()
            .setAggregation(
                Aggregation.explicitBucketHistogram(
                    ExplicitBucketHistogramOptions.builder().build()));
    SdkMeterProviderUtil.setEnabled(viewBuilder, /* enabled= */ false);
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test_counter").build(), viewBuilder.build())
            .build();

    ExtendedLongCounter counter =
        (ExtendedLongCounter)
            ((ExtendedLongCounterBuilder) meterProvider.get("meter").counterBuilder("test_counter"))
                .setOptInAdvice(false)
                .build();
    assertThat(counter.isEnabled()).isFalse();
    counter.add(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics()).isEmpty();
  }

  @Test
  void optInRespectsOtherAdvisoryParametersWhenEnabledByView() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ViewBuilder enabledViewBuilder = View.builder();
    SdkMeterProviderUtil.setEnabled(enabledViewBuilder, /* enabled= */ true);
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test_histogram").build(),
                enabledViewBuilder.build())
            .build();

    DoubleHistogram histogram =
        ((ExtendedDoubleHistogramBuilder)
                meterProvider.get("meter").histogramBuilder("test_histogram"))
            .setOptInAdvice(true)
            .setAttributesAdvice(asList(stringKey("key1"), stringKey("key2")))
            .setExplicitBucketBoundariesAdvice(asList(5.0, 10.0, 25.0))
            .build();

    histogram.record(7.0, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("test_histogram")
                .hasHistogramSatisfying(hist ->
                    hist.hasPointsSatisfying(point ->
                        point
                            .hasBucketBoundaries(5.0, 10.0, 25.0)
                            .hasAttributesSatisfyingExactly(
                                equalTo(stringKey("key1"), "1"),
                                equalTo(stringKey("key2"), "2")))));
  }

  @Test
  void optInAdvisoryParametersOverriddenByViewWhenConfigured() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ViewBuilder enabledViewBuilder =
        View.builder()
            .setAttributeFilter(Collections.singleton("key3"))
            .setAggregation(
                Aggregation.explicitBucketHistogram(
                    ExplicitBucketHistogramOptions.builder()
                        .setBucketBoundaries(asList(1.0, 2.0))
                        .build()));
    SdkMeterProviderUtil.setEnabled(enabledViewBuilder, /* enabled= */ true);
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test_histogram").build(),
                enabledViewBuilder.build())
            .build();

    DoubleHistogram histogram =
        ((ExtendedDoubleHistogramBuilder)
                meterProvider.get("meter").histogramBuilder("test_histogram"))
            .setOptInAdvice(true)
            .setAttributesAdvice(asList(stringKey("key1"), stringKey("key2")))
            .setExplicitBucketBoundariesAdvice(asList(5.0, 10.0, 25.0))
            .build();

    histogram.record(1.5, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("test_histogram")
                .hasHistogramSatisfying(hist ->
                    hist.hasPointsSatisfying(point ->
                        point
                            .hasBucketBoundaries(1.0, 2.0)
                            .hasAttributesSatisfyingExactly(equalTo(stringKey("key3"), "3")))));
  }

  @Test
  void optInAsynchronousInstruments() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ViewBuilder enabledViewBuilder = View.builder();
    SdkMeterProviderUtil.setEnabled(enabledViewBuilder, /* enabled= */ true);
    ViewBuilder disabledViewBuilder = View.builder();
    SdkMeterProviderUtil.setEnabled(disabledViewBuilder, /* enabled= */ false);
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("enabled_opt_in_*").build(),
                enabledViewBuilder.build())
            .registerView(
                InstrumentSelector.builder().setName("disabled_by_view").build(),
                disabledViewBuilder.build())
            .build();

    Meter meter = meterProvider.get("meter");
    AtomicInteger droppedCallbackCount = new AtomicInteger();
    AtomicInteger enabledCallbackCount = new AtomicInteger();

    ((ExtendedLongCounterBuilder) meter.counterBuilder("dropped_opt_in_counter"))
        .setOptInAdvice(true)
        .buildWithCallback(measurement -> {
          droppedCallbackCount.incrementAndGet();
          measurement.record(1);
        });
    ((ExtendedDoubleGaugeBuilder) meter.gaugeBuilder("dropped_opt_in_gauge"))
        .setOptInAdvice(true)
        .buildWithCallback(measurement -> {
          droppedCallbackCount.incrementAndGet();
          measurement.record(1.0);
        });
    ((ExtendedLongUpDownCounterBuilder) meter.upDownCounterBuilder("dropped_opt_in_updown"))
        .setOptInAdvice(true)
        .buildWithCallback(measurement -> {
          droppedCallbackCount.incrementAndGet();
          measurement.record(1);
        });
    meter
        .counterBuilder("disabled_by_view")
        .buildWithCallback(measurement -> {
          droppedCallbackCount.incrementAndGet();
          measurement.record(1);
        });

    ObservableDoubleMeasurement droppedBatchMeasurement =
        ((ExtendedDoubleCounterBuilder) meter.counterBuilder("dropped_opt_in_batch").ofDoubles())
            .setOptInAdvice(true)
            .buildObserver();
    meter.batchCallback(
        () -> {
          droppedCallbackCount.incrementAndGet();
          droppedBatchMeasurement.record(1.0);
        },
        droppedBatchMeasurement);

    ((ExtendedLongCounterBuilder) meter.counterBuilder("enabled_opt_in_counter"))
        .setOptInAdvice(true)
        .buildWithCallback(measurement -> {
          enabledCallbackCount.incrementAndGet();
          measurement.record(42);
        });

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("enabled_opt_in_counter")
                .hasLongSumSatisfying(
                    sum -> sum.hasPointsSatisfying(point -> point.hasValue(42L))));
    assertThat(droppedCallbackCount.get()).isEqualTo(0);
    assertThat(enabledCallbackCount.get()).isEqualTo(1);
  }

  @Test
  void optInPreservedAcrossBuilderSwaps() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    Meter meter = meterProvider.get("meter");

    ExtendedDoubleCounter doubleCounter =
        (ExtendedDoubleCounter)
            ((ExtendedLongCounterBuilder) meter.counterBuilder("double_counter"))
                .setOptInAdvice(true)
                .ofDoubles()
                .build();
    ExtendedLongGauge longGauge =
        (ExtendedLongGauge)
            ((ExtendedDoubleGaugeBuilder) meter.gaugeBuilder("long_gauge"))
                .setOptInAdvice(true)
                .ofLongs()
                .build();
    ExtendedLongHistogram longHistogram =
        (ExtendedLongHistogram)
            ((ExtendedDoubleHistogramBuilder) meter.histogramBuilder("long_histogram"))
                .setOptInAdvice(true)
                .ofLongs()
                .build();
    ExtendedDoubleUpDownCounter doubleUpDownCounter =
        (ExtendedDoubleUpDownCounter)
            ((ExtendedLongUpDownCounterBuilder) meter.upDownCounterBuilder("double_updown"))
                .setOptInAdvice(true)
                .ofDoubles()
                .build();

    assertThat(doubleCounter.isEnabled()).isFalse();
    assertThat(longGauge.isEnabled()).isFalse();
    assertThat(longHistogram.isEnabled()).isFalse();
    assertThat(doubleUpDownCounter.isEnabled()).isFalse();

    doubleCounter.add(1.0);
    longGauge.set(1L);
    longHistogram.record(1L);
    doubleUpDownCounter.add(1.0);

    assertThat(reader.collectAllMetrics()).isEmpty();
  }

  static Stream<Arguments> instruments() {
    return Stream.of(
        instrument(
            "double counter",
            (meter, name, optIn) -> {
              ExtendedDoubleCounter counter =
                  (ExtendedDoubleCounter)
                      ((ExtendedDoubleCounterBuilder) meter.counterBuilder(name).ofDoubles())
                          .setOptInAdvice(optIn)
                          .build();
              return new TestInstrument(
                  counter::add,
                  counter::isEnabled,
                  (m, a) -> m.hasDoubleSumPointsSatisfying(hasAttributes(a)));
            }),
        instrument(
            "long counter",
            (meter, name, optIn) -> {
              ExtendedLongCounter counter =
                  (ExtendedLongCounter)
                      ((ExtendedLongCounterBuilder) meter.counterBuilder(name))
                          .setOptInAdvice(optIn)
                          .build();
              return new TestInstrument(
                  counter::add,
                  counter::isEnabled,
                  (m, a) -> m.hasLongSumPointsSatisfying(hasAttributes(a)));
            }),
        instrument(
            "double gauge",
            (meter, name, optIn) -> {
              ExtendedDoubleGauge gauge =
                  (ExtendedDoubleGauge)
                      ((ExtendedDoubleGaugeBuilder) meter.gaugeBuilder(name))
                          .setOptInAdvice(optIn)
                          .build();
              return new TestInstrument(
                  gauge::set,
                  gauge::isEnabled,
                  (m, a) -> m.hasDoubleGaugePointsSatisfying(hasAttributes(a)));
            }),
        instrument(
            "long gauge",
            (meter, name, optIn) -> {
              ExtendedLongGauge gauge =
                  (ExtendedLongGauge)
                      ((ExtendedLongGaugeBuilder) meter.gaugeBuilder(name).ofLongs())
                          .setOptInAdvice(optIn)
                          .build();
              return new TestInstrument(
                  gauge::set,
                  gauge::isEnabled,
                  (m, a) -> m.hasLongGaugePointsSatisfying(hasAttributes(a)));
            }),
        instrument(
            "double histogram",
            (meter, name, optIn) -> {
              ExtendedDoubleHistogram histogram =
                  (ExtendedDoubleHistogram)
                      ((ExtendedDoubleHistogramBuilder) meter.histogramBuilder(name))
                          .setOptInAdvice(optIn)
                          .build();
              return new TestInstrument(
                  histogram::record,
                  histogram::isEnabled,
                  (m, a) -> m.hasHistogramPointsSatisfying(hasAttributes(a)));
            }),
        instrument(
            "long histogram",
            (meter, name, optIn) -> {
              ExtendedLongHistogram histogram =
                  (ExtendedLongHistogram)
                      ((ExtendedLongHistogramBuilder) meter.histogramBuilder(name).ofLongs())
                          .setOptInAdvice(optIn)
                          .build();
              return new TestInstrument(
                  histogram::record,
                  histogram::isEnabled,
                  (m, a) -> m.hasHistogramPointsSatisfying(hasAttributes(a)));
            }),
        instrument(
            "double up down counter",
            (meter, name, optIn) -> {
              ExtendedDoubleUpDownCounter upDownCounter =
                  (ExtendedDoubleUpDownCounter)
                      ((ExtendedDoubleUpDownCounterBuilder)
                              meter.upDownCounterBuilder(name).ofDoubles())
                          .setOptInAdvice(optIn)
                          .build();
              return new TestInstrument(
                  upDownCounter::add,
                  upDownCounter::isEnabled,
                  (m, a) -> m.hasDoubleSumPointsSatisfying(hasAttributes(a)));
            }),
        instrument(
            "long up down counter",
            (meter, name, optIn) -> {
              ExtendedLongUpDownCounter upDownCounter =
                  (ExtendedLongUpDownCounter)
                      ((ExtendedLongUpDownCounterBuilder) meter.upDownCounterBuilder(name))
                          .setOptInAdvice(optIn)
                          .build();
              return new TestInstrument(
                  upDownCounter::add,
                  upDownCounter::isEnabled,
                  (m, a) -> m.hasLongSumPointsSatisfying(hasAttributes(a)));
            }));
  }

  private static Arguments instrument(String name, InstrumentFactory factory) {
    return argumentSet(name, factory);
  }

  private static <P extends AbstractPointAssert<?, ?>> Consumer<P> hasAttributes(
      Attributes attributes) {
    return point -> point.hasAttributes(attributes);
  }

  @FunctionalInterface
  private interface InstrumentFactory {

    TestInstrument create(Meter meter, String name, boolean optIn);
  }

  private static final class TestInstrument {

    private final BiConsumer<Long, Attributes> recorder;
    private final Supplier<Boolean> enabledSupplier;
    private final BiConsumer<MetricAssert, Attributes> attributesAssertion;

    TestInstrument(
        BiConsumer<Long, Attributes> recorder,
        Supplier<Boolean> enabledSupplier,
        BiConsumer<MetricAssert, Attributes> attributesAssertion) {
      this.recorder = recorder;
      this.enabledSupplier = enabledSupplier;
      this.attributesAssertion = attributesAssertion;
    }

    void record(long value, Attributes attributes) {
      recorder.accept(value, attributes);
    }

    boolean isEnabled() {
      return enabledSupplier.get();
    }

    void assertHasAttributes(MetricAssert metric, Attributes attributes) {
      attributesAssertion.accept(metric, attributes);
    }
  }
}
