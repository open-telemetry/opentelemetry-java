/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;

class BoundInstrumentBaggageTest {

  private static ExtendedLongCounter counterWithBaggageView(InMemoryMetricReader reader) {
    ViewBuilder viewBuilder = View.builder().setAggregation(Aggregation.sum());
    SdkMeterProviderUtil.appendAllBaggageAttributes(viewBuilder);
    SdkMeterProvider provider =
        SdkMeterProvider.builder()
            .registerView(
                InstrumentSelector.builder().setName("counter").build(), viewBuilder.build())
            .registerMetricReader(reader)
            .build();
    Meter meter = provider.get(BoundInstrumentBaggageTest.class.getName());
    return (ExtendedLongCounter) meter.counterBuilder("counter").build();
  }

  private static Context withTenant(String tenant) {
    return Context.root().with(Baggage.builder().put("tenant", tenant).build());
  }

  @Test
  void cumulative_boundAtStartup_recordsUnderDifferentBaggagePerCall() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ExtendedLongCounter counter = counterWithBaggageView(reader);

    // Bind outside any baggage scope, as recommended for "attribute combinations known ahead of
    // time" - except here the view's attributes are NOT known ahead of time, since they come from
    // context.
    BoundLongCounter bound = counter.bind(Attributes.empty());

    bound.add(1, withTenant("acme"));
    bound.add(1, withTenant("globex"));

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasAttributes(
                                                Attributes.builder().put("tenant", "acme").build())
                                            .hasValue(1),
                                    point ->
                                        point
                                            .hasAttributes(
                                                Attributes.builder()
                                                    .put("tenant", "globex")
                                                    .build())
                                            .hasValue(1))));
  }

  @Test
  void delta_boundAtStartup_recordsUnderDifferentBaggagePerCall() {
    InMemoryMetricReader reader = InMemoryMetricReader.createDelta();
    ExtendedLongCounter counter = counterWithBaggageView(reader);

    BoundLongCounter bound = counter.bind(Attributes.empty());

    bound.add(1, withTenant("acme"));
    bound.add(1, withTenant("globex"));

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isDelta()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasAttributes(
                                                Attributes.builder().put("tenant", "acme").build())
                                            .hasValue(1),
                                    point ->
                                        point
                                            .hasAttributes(
                                                Attributes.builder()
                                                    .put("tenant", "globex")
                                                    .build())
                                            .hasValue(1))));
  }

  @Test
  void boundInOneRequest_reusedInAnother_doesNotMisattribute() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ExtendedLongCounter counter = counterWithBaggageView(reader);

    BoundLongCounter bound;
    try (Scope ignored = withTenant("acme").makeCurrent()) {
      bound = counter.bind(Attributes.empty());
      bound.add(1);
    }
    bound.add(1, withTenant("globex"));

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasAttributes(
                                            Attributes.builder().put("tenant", "acme").build())
                                        .hasValue(1),
                                point ->
                                    point
                                        .hasAttributes(
                                            Attributes.builder().put("tenant", "globex").build())
                                        .hasValue(1))));
  }

  @Test
  void repeatedBindUnderDifferentBaggage_createsNoSeriesUntilRecorded() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    ExtendedLongCounter counter = counterWithBaggageView(reader);

    BoundLongCounter boundAcme;
    BoundLongCounter boundGlobex;
    try (Scope ignored = withTenant("acme").makeCurrent()) {
      boundAcme = counter.bind(Attributes.empty());
    }
    try (Scope ignored = withTenant("globex").makeCurrent()) {
      boundGlobex = counter.bind(Attributes.empty());
    }
    try (Scope ignored = withTenant("initrode").makeCurrent()) {
      counter.bind(Attributes.empty()); // never recorded to
    }

    assertThat(reader.collectAllMetrics()).isEmpty();

    boundAcme.add(1, withTenant("acme"));
    boundGlobex.add(1, withTenant("globex"));

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasAttributes(
                                            Attributes.builder().put("tenant", "acme").build())
                                        .hasValue(1),
                                point ->
                                    point
                                        .hasAttributes(
                                            Attributes.builder().put("tenant", "globex").build())
                                        .hasValue(1))));
  }
}
