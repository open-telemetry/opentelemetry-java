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

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGaugeBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogramBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGaugeBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogramBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongGauge;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.assertj.AbstractPointAssert;
import io.opentelemetry.sdk.testing.assertj.DoublePointAssert;
import io.opentelemetry.sdk.testing.assertj.HistogramPointAssert;
import io.opentelemetry.sdk.testing.assertj.LongPointAssert;
import io.opentelemetry.sdk.testing.assertj.MetricAssert;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

class AttributesAdviceTest {

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
  void instrumentWithoutAdvice(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> pointsAssert) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    Instrument instrument = instrumentFactory.create(meterProvider, "test", null);
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                pointsAssert.hasPointSatisfying(
                    assertThat(metric), point -> point.hasAttributes(ATTRIBUTES)));
  }

  @ParameterizedTest
  @ArgumentsSource(InstrumentsProvider.class)
  void instrumentWithAdvice(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> pointsAssert) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    Instrument instrument =
        instrumentFactory.create(
            meterProvider, "test", asList(stringKey("key1"), stringKey("key2")));
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                pointsAssert.hasPointSatisfying(
                    assertThat(metric),
                    point ->
                        point.hasAttributesSatisfyingExactly(
                            equalTo(stringKey("key1"), "1"), equalTo(stringKey("key2"), "2"))));
  }

  @ParameterizedTest
  @ArgumentsSource(InstrumentsProvider.class)
  void instrumentWithAdviceAndViews(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> pointsAssert) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test").build(),
                View.builder()
                    .setAttributeFilter(key -> "key2".equals(key) || "key3".equals(key))
                    .build())
            .build();

    Instrument instrument =
        instrumentFactory.create(
            meterProvider, "test", asList(stringKey("key1"), stringKey("key2")));
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                pointsAssert.hasPointSatisfying(
                    assertThat(metric),
                    point ->
                        point.hasAttributesSatisfyingExactly(
                            equalTo(stringKey("key2"), "2"), equalTo(stringKey("key3"), "3"))));
  }

  @ParameterizedTest
  @ArgumentsSource(InstrumentsProvider.class)
  void instrumentWithAdviceAndDescriptionViews(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> pointsAssert) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    // Register a view which sets a description. Since any matching view supersedes any instrument
    // advice, the attribute advice is ignored and all attributes are recorded.
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("test").build(),
                View.builder().setDescription("description").build())
            .build();

    Instrument instrument =
        instrumentFactory.create(
            meterProvider, "test", asList(stringKey("key1"), stringKey("key2")));
    instrument.record(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                pointsAssert.hasPointSatisfying(
                    assertThat(metric),
                    point ->
                        point.hasAttributesSatisfyingExactly(
                            equalTo(stringKey("key1"), "1"),
                            equalTo(stringKey("key2"), "2"),
                            equalTo(stringKey("key3"), "3"))));
  }

  @ParameterizedTest
  @ArgumentsSource(InstrumentsProvider.class)
  void instrumentWithAdviceAndBaggage(
      InstrumentFactory instrumentFactory, PointsAssert<AbstractPointAssert<?, ?>> pointsAssert) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    // Register a view which appends a baggage entry. Since any matching view supersedes any
    // instrument advice, the attribute advice is ignored and all attributes + the baggage entry are
    // recorded.
    ViewBuilder viewBuilder = View.builder();
    SdkMeterProviderUtil.appendFilteredBaggageAttributes(
        viewBuilder, name -> name.equals("baggage1"));
    meterProvider =
        builder
            .registerMetricReader(reader)
            .registerView(InstrumentSelector.builder().setName("*").build(), viewBuilder.build())
            .build();

    Instrument instrument =
        instrumentFactory.create(
            meterProvider, "test", asList(stringKey("key1"), stringKey("key2")));
    try (Scope unused =
        Baggage.current().toBuilder()
            .put("baggage1", "value1")
            .put("baggage2", "value2")
            .build()
            .makeCurrent()) {
      instrument.record(1, ATTRIBUTES);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                pointsAssert.hasPointSatisfying(
                    assertThat(metric),
                    point ->
                        point.hasAttributesSatisfyingExactly(
                            equalTo(stringKey("key1"), "1"),
                            equalTo(stringKey("key2"), "2"),
                            equalTo(stringKey("key3"), "3"),
                            equalTo(stringKey("baggage1"), "value1"))));
  }

  static final class InstrumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(
        ParameterDeclarations parameters, ExtensionContext context) {
      return Stream.of(
          // double counter
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, attributesAdvice) -> {
                    DoubleCounterBuilder doubleCounterBuilder =
                        meterProvider.get("meter").counterBuilder(name).ofDoubles();
                    if (attributesAdvice != null) {
                      ((ExtendedDoubleCounterBuilder) doubleCounterBuilder)
                          .setAttributesAdvice(attributesAdvice);
                    }
                    DoubleCounter counter = doubleCounterBuilder.build();
                    return counter::add;
                  },
              (PointsAssert<DoublePointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasDoubleSumSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // long counter
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, attributesAdvice) -> {
                    LongCounterBuilder doubleCounterBuilder =
                        meterProvider.get("meter").counterBuilder(name);
                    if (attributesAdvice != null) {
                      ((ExtendedLongCounterBuilder) doubleCounterBuilder)
                          .setAttributesAdvice(attributesAdvice);
                    }
                    LongCounter counter = doubleCounterBuilder.build();
                    return counter::add;
                  },
              (PointsAssert<LongPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasLongSumSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // double gauge
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, attributesAdvice) -> {
                    DoubleGaugeBuilder doubleGaugeBuilder =
                        meterProvider.get("meter").gaugeBuilder(name);
                    if (attributesAdvice != null) {
                      ((ExtendedDoubleGaugeBuilder) doubleGaugeBuilder)
                          .setAttributesAdvice(attributesAdvice);
                    }
                    DoubleGauge gauge = doubleGaugeBuilder.build();
                    return gauge::set;
                  },
              (PointsAssert<DoublePointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasDoubleGaugeSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // long gauge
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, attributesAdvice) -> {
                    LongGaugeBuilder longGaugeBuilder =
                        meterProvider.get("meter").gaugeBuilder(name).ofLongs();
                    if (attributesAdvice != null) {
                      ((ExtendedLongGaugeBuilder) longGaugeBuilder)
                          .setAttributesAdvice(attributesAdvice);
                    }
                    LongGauge gauge = longGaugeBuilder.build();
                    return gauge::set;
                  },
              (PointsAssert<LongPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasLongGaugeSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // double histogram
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, attributesAdvice) -> {
                    DoubleHistogramBuilder doubleHistogramBuilder =
                        meterProvider.get("meter").histogramBuilder(name);
                    if (attributesAdvice != null) {
                      ((ExtendedDoubleHistogramBuilder) doubleHistogramBuilder)
                          .setAttributesAdvice(attributesAdvice);
                    }
                    DoubleHistogram histogram = doubleHistogramBuilder.build();
                    return histogram::record;
                  },
              (PointsAssert<HistogramPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasHistogramSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // long histogram
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, attributesAdvice) -> {
                    LongHistogramBuilder doubleHistogramBuilder =
                        meterProvider.get("meter").histogramBuilder(name).ofLongs();
                    if (attributesAdvice != null) {
                      ((ExtendedLongHistogramBuilder) doubleHistogramBuilder)
                          .setAttributesAdvice(attributesAdvice);
                    }
                    LongHistogram histogram = doubleHistogramBuilder.build();
                    return histogram::record;
                  },
              (PointsAssert<HistogramPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasHistogramSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // double up down counter
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, attributesAdvice) -> {
                    DoubleUpDownCounterBuilder doubleUpDownCounterBuilder =
                        meterProvider.get("meter").upDownCounterBuilder(name).ofDoubles();
                    if (attributesAdvice != null) {
                      ((ExtendedDoubleUpDownCounterBuilder) doubleUpDownCounterBuilder)
                          .setAttributesAdvice(attributesAdvice);
                    }
                    DoubleUpDownCounter upDownCounter = doubleUpDownCounterBuilder.build();
                    return upDownCounter::add;
                  },
              (PointsAssert<DoublePointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasDoubleSumSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))),
          // long up down counter
          arguments(
              (InstrumentFactory)
                  (meterProvider, name, attributesAdvice) -> {
                    LongUpDownCounterBuilder doubleUpDownCounterBuilder =
                        meterProvider.get("meter").upDownCounterBuilder(name);
                    if (attributesAdvice != null) {
                      ((ExtendedLongUpDownCounterBuilder) doubleUpDownCounterBuilder)
                          .setAttributesAdvice(attributesAdvice);
                    }
                    LongUpDownCounter upDownCounter = doubleUpDownCounterBuilder.build();
                    return upDownCounter::add;
                  },
              (PointsAssert<LongPointAssert>)
                  (metricAssert, assertions) ->
                      metricAssert.hasLongSumSatisfying(
                          sum -> sum.hasPointsSatisfying(assertions))));
    }
  }

  @FunctionalInterface
  interface InstrumentFactory {

    Instrument create(
        SdkMeterProvider meterProvider,
        String name,
        @Nullable List<AttributeKey<?>> attributesAdvice);
  }

  @FunctionalInterface
  interface Instrument {

    void record(long value, Attributes attributes);
  }

  @FunctionalInterface
  interface PointsAssert<P extends AbstractPointAssert<?, ?>> {

    void hasPointSatisfying(MetricAssert metricAssert, Consumer<P> assertion);
  }
}
