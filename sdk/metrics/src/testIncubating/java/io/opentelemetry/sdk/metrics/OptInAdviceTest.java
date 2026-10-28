/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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
import io.opentelemetry.sdk.testing.assertj.DoublePointAssert;
import io.opentelemetry.sdk.testing.assertj.HistogramPointAssert;
import io.opentelemetry.sdk.testing.assertj.LongPointAssert;
import io.opentelemetry.sdk.testing.assertj.MetricAssert;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

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
  @ArgumentsSource(InstrumentsProvider.class)
  void optInInstrumentDisabledByDefault(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> unused) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    TestInstrument instrument = instrumentFactory.create(meterProvider, "test", /* optIn= */ true);
    assertThat(instrument.isEnabled()).isFalse();
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics()).isEmpty();
  }

  @ParameterizedTest
  @ArgumentsSource(InstrumentsProvider.class)
  void optInFalseInstrumentEnabledByDefault(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> pointsAssert) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    TestInstrument instrument = instrumentFactory.create(meterProvider, "test", /* optIn= */ false);
    assertThat(instrument.isEnabled()).isTrue();
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                pointsAssert.hasPointSatisfying(
                    assertThat(metric), point -> point.hasAttributes(ATTRIBUTES)));
  }

  @ParameterizedTest
  @ArgumentsSource(InstrumentsProvider.class)
  void optInInstrumentEnabledByView(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> pointsAssert) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ViewBuilder viewBuilder = View.builder();
    SdkMeterProviderUtil.setEnabled(viewBuilder, /* enabled= */ true);
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(InstrumentSelector.builder().setName("test").build(), viewBuilder.build())
            .build();

    TestInstrument instrument = instrumentFactory.create(meterProvider, "test", /* optIn= */ true);
    assertThat(instrument.isEnabled()).isTrue();
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                pointsAssert.hasPointSatisfying(
                    assertThat(metric), point -> point.hasAttributes(ATTRIBUTES)));
  }

  @ParameterizedTest
  @ArgumentsSource(InstrumentsProvider.class)
  void optInInstrumentNotEnabledByViewWithoutEnabledTrue(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> unused) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test").build(),
                View.builder().setDescription("custom description").build())
            .build();

    TestInstrument instrument = instrumentFactory.create(meterProvider, "test", /* optIn= */ true);
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
  @ArgumentsSource(InstrumentsProvider.class)
  void nonOptInInstrumentDisabledByViewEnabledFalse(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> unused) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ViewBuilder viewBuilder = View.builder();
    SdkMeterProviderUtil.setEnabled(viewBuilder, /* enabled= */ false);
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(InstrumentSelector.builder().setName("test").build(), viewBuilder.build())
            .build();

    TestInstrument instrument = instrumentFactory.create(meterProvider, "test", /* optIn= */ false);
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
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("test_histogram")
                    .hasHistogramSatisfying(
                        hist ->
                            hist.hasPointsSatisfying(
                                point ->
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
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("test_histogram")
                    .hasHistogramSatisfying(
                        hist ->
                            hist.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasBucketBoundaries(1.0, 2.0)
                                        .hasAttributesSatisfyingExactly(
                                            equalTo(stringKey("key3"), "3")))));
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
        .buildWithCallback(
            measurement -> {
              droppedCallbackCount.incrementAndGet();
              measurement.record(1);
            });
    ((ExtendedDoubleGaugeBuilder) meter.gaugeBuilder("dropped_opt_in_gauge"))
        .setOptInAdvice(true)
        .buildWithCallback(
            measurement -> {
              droppedCallbackCount.incrementAndGet();
              measurement.record(1.0);
            });
    ((ExtendedLongUpDownCounterBuilder) meter.upDownCounterBuilder("dropped_opt_in_updown"))
        .setOptInAdvice(true)
        .buildWithCallback(
            measurement -> {
              droppedCallbackCount.incrementAndGet();
              measurement.record(1);
            });
    meter
        .counterBuilder("disabled_by_view")
        .buildWithCallback(
            measurement -> {
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
        .buildWithCallback(
            measurement -> {
              enabledCallbackCount.incrementAndGet();
              measurement.record(42);
            });

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
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

  static final class InstrumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(
        ParameterDeclarations parameters, ExtensionContext context) {
      return Stream.of(
          // double counter
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, optIn) -> {
                    ExtendedDoubleCounter counter =
                        (ExtendedDoubleCounter)
                            ((ExtendedDoubleCounterBuilder)
                                    meterProvider.get("meter").counterBuilder(name).ofDoubles())
                                .setOptInAdvice(optIn)
                                .build();
                    return new TestInstrument() {
                      @Override
                      public void record(long value, Attributes attributes) {
                        counter.add((double) value, attributes);
                      }

                      @Override
                      public boolean isEnabled() {
                        return counter.isEnabled();
                      }
                    };
                  },
              (PointsAssert<DoublePointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasDoubleSumSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // long counter
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, optIn) -> {
                    ExtendedLongCounter counter =
                        (ExtendedLongCounter)
                            ((ExtendedLongCounterBuilder)
                                    meterProvider.get("meter").counterBuilder(name))
                                .setOptInAdvice(optIn)
                                .build();
                    return new TestInstrument() {
                      @Override
                      public void record(long value, Attributes attributes) {
                        counter.add(value, attributes);
                      }

                      @Override
                      public boolean isEnabled() {
                        return counter.isEnabled();
                      }
                    };
                  },
              (PointsAssert<LongPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasLongSumSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // double gauge
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, optIn) -> {
                    ExtendedDoubleGauge gauge =
                        (ExtendedDoubleGauge)
                            ((ExtendedDoubleGaugeBuilder)
                                    meterProvider.get("meter").gaugeBuilder(name))
                                .setOptInAdvice(optIn)
                                .build();
                    return new TestInstrument() {
                      @Override
                      public void record(long value, Attributes attributes) {
                        gauge.set((double) value, attributes);
                      }

                      @Override
                      public boolean isEnabled() {
                        return gauge.isEnabled();
                      }
                    };
                  },
              (PointsAssert<DoublePointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasDoubleGaugeSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // long gauge
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, optIn) -> {
                    ExtendedLongGauge gauge =
                        (ExtendedLongGauge)
                            ((ExtendedLongGaugeBuilder)
                                    meterProvider.get("meter").gaugeBuilder(name).ofLongs())
                                .setOptInAdvice(optIn)
                                .build();
                    return new TestInstrument() {
                      @Override
                      public void record(long value, Attributes attributes) {
                        gauge.set(value, attributes);
                      }

                      @Override
                      public boolean isEnabled() {
                        return gauge.isEnabled();
                      }
                    };
                  },
              (PointsAssert<LongPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasLongGaugeSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // double histogram
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, optIn) -> {
                    ExtendedDoubleHistogram histogram =
                        (ExtendedDoubleHistogram)
                            ((ExtendedDoubleHistogramBuilder)
                                    meterProvider.get("meter").histogramBuilder(name))
                                .setOptInAdvice(optIn)
                                .build();
                    return new TestInstrument() {
                      @Override
                      public void record(long value, Attributes attributes) {
                        histogram.record((double) value, attributes);
                      }

                      @Override
                      public boolean isEnabled() {
                        return histogram.isEnabled();
                      }
                    };
                  },
              (PointsAssert<HistogramPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasHistogramSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // long histogram
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, optIn) -> {
                    ExtendedLongHistogram histogram =
                        (ExtendedLongHistogram)
                            ((ExtendedLongHistogramBuilder)
                                    meterProvider.get("meter").histogramBuilder(name).ofLongs())
                                .setOptInAdvice(optIn)
                                .build();
                    return new TestInstrument() {
                      @Override
                      public void record(long value, Attributes attributes) {
                        histogram.record(value, attributes);
                      }

                      @Override
                      public boolean isEnabled() {
                        return histogram.isEnabled();
                      }
                    };
                  },
              (PointsAssert<HistogramPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasHistogramSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // double up down counter
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, optIn) -> {
                    ExtendedDoubleUpDownCounter upDownCounter =
                        (ExtendedDoubleUpDownCounter)
                            ((ExtendedDoubleUpDownCounterBuilder)
                                    meterProvider
                                        .get("meter")
                                        .upDownCounterBuilder(name)
                                        .ofDoubles())
                                .setOptInAdvice(optIn)
                                .build();
                    return new TestInstrument() {
                      @Override
                      public void record(long value, Attributes attributes) {
                        upDownCounter.add((double) value, attributes);
                      }

                      @Override
                      public boolean isEnabled() {
                        return upDownCounter.isEnabled();
                      }
                    };
                  },
              (PointsAssert<DoublePointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasDoubleSumSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // long up down counter
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, optIn) -> {
                    ExtendedLongUpDownCounter upDownCounter =
                        (ExtendedLongUpDownCounter)
                            ((ExtendedLongUpDownCounterBuilder)
                                    meterProvider.get("meter").upDownCounterBuilder(name))
                                .setOptInAdvice(optIn)
                                .build();
                    return new TestInstrument() {
                      @Override
                      public void record(long value, Attributes attributes) {
                        upDownCounter.add(value, attributes);
                      }

                      @Override
                      public boolean isEnabled() {
                        return upDownCounter.isEnabled();
                      }
                    };
                  },
              (PointsAssert<LongPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasLongSumSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))));
    }
  }

  @FunctionalInterface
  interface InstrumentFactory {

    TestInstrument create(SdkMeterProvider meterProvider, String name, boolean optIn);
  }

  interface TestInstrument {

    void record(long value, Attributes attributes);

    boolean isEnabled();
  }

  @FunctionalInterface
  interface PointsAssert<P extends AbstractPointAssert<?, ?>> {

    void hasPointSatisfying(MetricAssert metricAssert, Consumer<P> assertion);
  }
}
