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
}
