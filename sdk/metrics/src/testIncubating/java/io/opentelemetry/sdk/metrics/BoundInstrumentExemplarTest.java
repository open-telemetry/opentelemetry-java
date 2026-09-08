/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class BoundInstrumentExemplarTest {

  /**
   * When two bindings collapse to the same delta series, each must record exemplars with its own
   * attributes rather than a later binding's.
   */
  @Test
  void delta_collapsingBindings_keepPerBindExemplarAttributes() {
    InMemoryMetricReader deltaReader = InMemoryMetricReader.createDelta();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .setExemplarFilter(ExemplarFilter.alwaysOn())
            // Drops "drop", collapsing the two attribute sets below to the single series {keep=k}.
            .registerView(
                InstrumentSelector.builder().setName("test-counter").build(),
                View.builder().setAttributeFilter(Collections.singleton("keep")).build())
            .registerMetricReader(deltaReader)
            .build();
    Meter meter = meterProvider.get(BoundInstrumentExemplarTest.class.getName());

    ExtendedLongCounter counter =
        (ExtendedLongCounter) meter.counterBuilder("test-counter").build();

    Attributes first = Attributes.builder().put("keep", "k").put("drop", "first").build();
    Attributes second = Attributes.builder().put("keep", "k").put("drop", "second").build();

    BoundLongCounter boundFirst = counter.bind(first);
    BoundLongCounter boundSecond = counter.bind(second);
    assertThat(boundFirst).isNotSameAs(boundSecond);

    boundFirst.add(1);

    assertThat(deltaReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(1)
                                            .hasAttributes(
                                                Attributes.builder().put("keep", "k").build())
                                            .hasExemplarsSatisfying(
                                                exemplar ->
                                                    exemplar
                                                        .hasValue(1)
                                                        .hasFilteredAttributes(
                                                            Attributes.builder()
                                                                .put("drop", "first")
                                                                .build())))));
  }

  /**
   * Control for the context-deferred bind path exercised by {@code BoundInstrumentBaggageTest}: for
   * a view whose {@code AttributesProcessor} does not use context, binding must still resolve the
   * series eagerly, at bind() time, not on the first record call.
   */
  @Test
  void cumulative_nonContextView_bindResolvesSeriesEagerly() {
    TestClock clock = TestClock.create();
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .setClock(clock)
            .registerView(
                InstrumentSelector.builder().setName("test-counter").build(),
                View.builder().setAttributeFilter(Collections.singleton("keep")).build())
            .registerMetricReader(reader)
            .build();
    Meter meter = meterProvider.get(BoundInstrumentExemplarTest.class.getName());
    ExtendedLongCounter counter =
        (ExtendedLongCounter) meter.counterBuilder("test-counter").build();

    long bindEpochNanos = clock.now();
    BoundLongCounter bound = counter.bind(Attributes.builder().put("keep", "k").build());
    clock.advance(Duration.ofSeconds(5));
    bound.add(1);

    // The cumulative aggregator's creation time (used as the point's start time) is stamped when
    // the series is resolved. If it matches bind time rather than the later add() time, the series
    // was resolved eagerly at bind(), confirming the fast path wasn't silently deferred.
    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .hasPointsSatisfying(
                                    point -> point.hasStartEpochNanos(bindEpochNanos))));
  }
}
