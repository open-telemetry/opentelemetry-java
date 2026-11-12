/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ContextAttributes;
import io.opentelemetry.api.incubator.metrics.BoundDoubleCounter;
import io.opentelemetry.api.incubator.metrics.BoundLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ContextScopedAttributesMetricTest {

  private InMemoryMetricReader reader;
  private SdkMeterProviderBuilder builder;

  @BeforeEach
  void setUp() {
    reader = InMemoryMetricReader.create();
    builder = SdkMeterProvider.builder().registerMetricReader(reader);
  }

  @Test
  void disabledByDefault() {
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    LongCounter counter = meter.counterBuilder("counter").build();

    Context ctx = ContextAttributes.set(Context.current(), Attributes.of(stringKey("k1"), "v1"));
    try (Scope ignored = ctx.makeCurrent()) {
      counter.add(1);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("counter")
                .hasLongSumPointsSatisfying(
                    point -> point.hasValue(1).hasAttributes(Attributes.empty())));
  }

  @Test
  void enabled_mergesWithExplicitPrecedenceAndAppliesViewFilter() {
    builder.registerView(
        InstrumentSelector.builder().setName("filtered_counter").build(),
        View.builder().setAttributeFilter(Collections.singleton("allowed_ctx_key")).build());
    SdkMeterProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    LongCounter counter = meter.counterBuilder("counter").build();
    LongCounter filteredCounter = meter.counterBuilder("filtered_counter").build();

    Context ctx =
        ContextAttributes.set(
            Context.current(),
            Attributes.of(
                stringKey("k1"),
                "from-context",
                stringKey("k2"),
                "from-context",
                stringKey("allowed_ctx_key"),
                "allowed"));
    try (Scope ignored = ctx.makeCurrent()) {
      counter.add(5, Attributes.of(stringKey("k2"), "explicit", stringKey("k3"), "v3"));
      filteredCounter.add(3, Attributes.of(stringKey("unallowed_explicit"), "dropped"));
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("counter")
                    .hasLongSumPointsSatisfying(point ->
                        point
                            .hasValue(5)
                            .hasAttributes(
                                attributeEntry("k1", "from-context"),
                                attributeEntry("k2", "explicit"),
                                attributeEntry("k3", "v3"),
                                attributeEntry("allowed_ctx_key", "allowed"))),
            metric ->
                assertThat(metric)
                    .hasName("filtered_counter")
                    .hasLongSumPointsSatisfying(point ->
                        point
                            .hasValue(3)
                            .hasAttributes(attributeEntry("allowed_ctx_key", "allowed"))));
  }

  @ParameterizedTest
  @EnumSource(AggregationTemporality.class)
  void boundLongCounter(AggregationTemporality temporality) {
    InMemoryMetricReader localReader =
        temporality == AggregationTemporality.DELTA
            ? InMemoryMetricReader.createDelta()
            : InMemoryMetricReader.create();
    SdkMeterProviderBuilder localBuilder =
        SdkMeterProvider.builder().registerMetricReader(localReader);
    SdkMeterProviderUtil.setContextScopedAttributesFilter(localBuilder, (scope, key) -> true);
    SdkMeterProvider provider = localBuilder.build();
    Meter meter = provider.get("test");
    ExtendedLongCounter counter =
        (ExtendedLongCounter) meter.counterBuilder("bound_counter").build();
    BoundLongCounter bound =
        counter.bind(
            Attributes.of(stringKey("k_bound"), "bound", stringKey("k_clash"), "bound-win"));

    bound.add(3);

    Context ctx =
        ContextAttributes.set(
            Context.current(),
            Attributes.of(stringKey("k_ctx"), "ctx", stringKey("k_clash"), "ctx-lose"));
    try (Scope ignored = ctx.makeCurrent()) {
      bound.add(7);
    }

    assertThat(localReader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("bound_counter")
                .hasLongSumPointsSatisfying(point ->
                    point
                        .hasValue(10)
                        .hasAttributes(
                            attributeEntry("k_bound", "bound"),
                            attributeEntry("k_clash", "bound-win"))));
  }

  @ParameterizedTest
  @EnumSource(AggregationTemporality.class)
  void boundDoubleCounter(AggregationTemporality temporality) {
    InMemoryMetricReader localReader =
        temporality == AggregationTemporality.DELTA
            ? InMemoryMetricReader.createDelta()
            : InMemoryMetricReader.create();
    SdkMeterProviderBuilder localBuilder =
        SdkMeterProvider.builder().registerMetricReader(localReader);
    SdkMeterProviderUtil.setContextScopedAttributesFilter(localBuilder, (scope, key) -> true);
    SdkMeterProvider provider = localBuilder.build();
    Meter meter = provider.get("test");
    ExtendedDoubleCounter counter =
        (ExtendedDoubleCounter) meter.counterBuilder("bound_double").ofDoubles().build();
    BoundDoubleCounter bound =
        counter.bind(
            Attributes.of(stringKey("k_bound"), "bound", stringKey("k_clash"), "bound-win"));

    bound.add(1.5);

    Context ctx =
        ContextAttributes.set(
            Context.current(),
            Attributes.of(stringKey("k_ctx"), "ctx", stringKey("k_clash"), "ctx-lose"));
    try (Scope ignored = ctx.makeCurrent()) {
      bound.add(2.5);
    }

    assertThat(localReader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("bound_double")
                .hasDoubleSumPointsSatisfying(point ->
                    point
                        .hasValue(4.0)
                        .hasAttributes(
                            attributeEntry("k_bound", "bound"),
                            attributeEntry("k_clash", "bound-win"))));
  }

  @Test
  void doubleCounter_explicitContext() {
    SdkMeterProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    DoubleCounter counter = meter.counterBuilder("double_counter").ofDoubles().build();

    Context explicitCtx =
        ContextAttributes.set(
            Context.root(),
            Attributes.of(stringKey("k_ctx"), "ctx", stringKey("k_clash"), "ctx-lose"));

    counter.add(
        2.5,
        Attributes.of(stringKey("k_explicit"), "val", stringKey("k_clash"), "explicit-win"),
        explicitCtx);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("double_counter")
                .hasDoubleSumPointsSatisfying(point ->
                    point
                        .hasValue(2.5)
                        .hasAttributes(
                            attributeEntry("k_explicit", "val"),
                            attributeEntry("k_clash", "explicit-win"),
                            attributeEntry("k_ctx", "ctx"))));
  }

  @Test
  void boundCounter_droppedInstrument() {
    builder.registerView(
        InstrumentSelector.builder().setName("dropped_*").build(),
        View.builder().setAggregation(Aggregation.drop()).build());
    SdkMeterProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");

    ExtendedLongCounter longCounter =
        (ExtendedLongCounter) meter.counterBuilder("dropped_long").build();
    BoundLongCounter boundLong = longCounter.bind(Attributes.empty());
    boundLong.add(10);

    ExtendedDoubleCounter doubleCounter =
        (ExtendedDoubleCounter) meter.counterBuilder("dropped_double").ofDoubles().build();
    BoundDoubleCounter boundDouble = doubleCounter.bind(Attributes.empty());
    boundDouble.add(10.5);

    assertThat(reader.collectAllMetrics()).isEmpty();
  }

  @Test
  void unboundCounter_emptyAttributesUsesContextAttributesDirectly() {
    SdkMeterProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    LongCounter counter = meter.counterBuilder("counter_empty_attrs").build();

    Context ctx =
        ContextAttributes.set(Context.current(), Attributes.of(stringKey("k_ctx"), "ctx"));
    try (Scope ignored = ctx.makeCurrent()) {
      counter.add(1, Attributes.empty());
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("counter_empty_attrs")
                .hasLongSumPointsSatisfying(
                    point -> point.hasValue(1).hasAttributes(attributeEntry("k_ctx", "ctx"))));
  }

  @Test
  void doubleCounter_nanDropped() {
    SdkMeterProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    DoubleCounter counter = meter.counterBuilder("double_nan").ofDoubles().build();

    Context ctx =
        ContextAttributes.set(Context.current(), Attributes.of(stringKey("k_ctx"), "ctx"));
    try (Scope ignored = ctx.makeCurrent()) {
      counter.add(Double.NaN, Attributes.empty());
    }

    ExtendedDoubleCounter extendedCounter = (ExtendedDoubleCounter) counter;
    BoundDoubleCounter bound = extendedCounter.bind(Attributes.empty());
    try (Scope ignored = ctx.makeCurrent()) {
      bound.add(Double.NaN);
    }

    assertThat(reader.collectAllMetrics()).isEmpty();
  }

  @Test
  void boundAndUnbound_isolationInSameContext() {
    SdkMeterProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");

    LongCounter unboundCounter = meter.counterBuilder("unbound_requests").build();
    ExtendedLongCounter extendedCounter =
        (ExtendedLongCounter) meter.counterBuilder("bound_infra").build();
    BoundLongCounter boundCounter =
        extendedCounter.bind(Attributes.of(stringKey("pool"), "shared-infra"));

    Context ctx =
        ContextAttributes.set(Context.current(), Attributes.of(stringKey("tenant.id"), "acme"));

    try (Scope ignored = ctx.makeCurrent()) {
      unboundCounter.add(1, Attributes.of(stringKey("route"), "/api"));
      boundCounter.add(1);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("unbound_requests")
                    .hasLongSumPointsSatisfying(point ->
                        point
                            .hasValue(1)
                            .hasAttributes(
                                attributeEntry("route", "/api"),
                                attributeEntry("tenant.id", "acme"))),
            metric ->
                assertThat(metric)
                    .hasName("bound_infra")
                    .hasLongSumPointsSatisfying(point ->
                        point.hasValue(1).hasAttributes(attributeEntry("pool", "shared-infra"))));
  }

  @Test
  void sameNameDifferentType_explicitWins() {
    SdkMeterProviderUtil.setContextScopedAttributesFilter(builder, (scope, key) -> true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");

    LongCounter counter = meter.counterBuilder("collision_counter").build();
    Context ctx =
        ContextAttributes.set(Context.current(), Attributes.of(stringKey("code"), "context_ok"));

    try (Scope ignored = ctx.makeCurrent()) {
      counter.add(1, Attributes.of(longKey("code"), 200L));
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(metric ->
            assertThat(metric)
                .hasName("collision_counter")
                .hasLongSumPointsSatisfying(
                    point -> point.hasValue(1).hasAttributes(attributeEntry("code", 200L))));
  }

  @Test
  void filterPredicate_filtersByScopeAndKey() {
    SdkMeterProviderUtil.setContextScopedAttributesFilter(
        builder,
        (scope, key) ->
            scope.getName().equals("allowed-meter") && key.getKey().startsWith("allowed."));
    SdkMeterProvider provider = builder.build();

    Meter allowedMeter = provider.get("allowed-meter");
    Meter otherMeter = provider.get("other-meter");

    LongCounter allowedCounter = allowedMeter.counterBuilder("m1").build();
    LongCounter otherCounter = otherMeter.counterBuilder("m2").build();

    Context ctx =
        ContextAttributes.set(
            Context.current(),
            Attributes.of(
                stringKey("allowed.k"), "v1",
                stringKey("denied.k"), "v2"));

    try (Scope ignored = ctx.makeCurrent()) {
      allowedCounter.add(1);
      otherCounter.add(1);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("m1")
                    .hasLongSumPointsSatisfying(point ->
                        point.hasValue(1).hasAttributes(attributeEntry("allowed.k", "v1"))),
            metric ->
                assertThat(metric)
                    .hasName("m2")
                    .hasLongSumPointsSatisfying(
                        point -> point.hasValue(1).hasAttributes(Attributes.empty())));
  }
}
