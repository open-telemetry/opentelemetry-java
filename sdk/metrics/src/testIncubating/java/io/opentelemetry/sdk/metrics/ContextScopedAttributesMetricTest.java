/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

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
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ContextScopedAttributesMetricTest {

  @Test
  void contextScopedAttributes_disabledByDefault() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    Meter meter = provider.get("test");
    LongCounter counter = meter.counterBuilder("counter").build();

    Context ctx = ContextAttributes.add(Context.current(), Attributes.of(stringKey("k1"), "v1"));
    try (Scope ignored = ctx.makeCurrent()) {
      counter.add(1);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point -> point.hasValue(1).hasAttributes(Attributes.empty()))));
  }

  @Test
  void contextScopedAttributes_enabled_mergesWithExplicitPrecedenceAndAppliesViewFilter() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("filtered_counter").build(),
                View.builder()
                    .setAttributeFilter(Collections.singleton("allowed_ctx_key"))
                    .build());
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    LongCounter counter = meter.counterBuilder("counter").build();
    LongCounter filteredCounter = meter.counterBuilder("filtered_counter").build();

    Context ctx =
        ContextAttributes.add(
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
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(5)
                                        .hasAttributes(
                                            attributeEntry("k1", "from-context"),
                                            attributeEntry("k2", "explicit"),
                                            attributeEntry("k3", "v3"),
                                            attributeEntry("allowed_ctx_key", "allowed")))),
            metric ->
                assertThat(metric)
                    .hasName("filtered_counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(3)
                                        .hasAttributes(
                                            attributeEntry("allowed_ctx_key", "allowed")))));
  }

  @Test
  void contextScopedAttributes_boundCounter_cumulative() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    ExtendedLongCounter counter =
        (ExtendedLongCounter) meter.counterBuilder("bound_counter").build();
    BoundLongCounter bound =
        counter.bind(
            Attributes.of(stringKey("k_bound"), "bound", stringKey("k_clash"), "bound-win"));

    Context ctx =
        ContextAttributes.add(
            Context.current(),
            Attributes.of(stringKey("k_ctx"), "ctx", stringKey("k_clash"), "ctx-lose"));
    try (Scope ignored = ctx.makeCurrent()) {
      bound.add(10);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("bound_counter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(10)
                                        .hasAttributes(
                                            attributeEntry("k_bound", "bound"),
                                            attributeEntry("k_clash", "bound-win")))));
  }

  @Test
  void contextScopedAttributes_boundCounter_delta() {
    InMemoryMetricReader reader = InMemoryMetricReader.createDelta();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    ExtendedLongCounter counter =
        (ExtendedLongCounter) meter.counterBuilder("bound_counter_delta").build();
    BoundLongCounter bound =
        counter.bind(
            Attributes.of(stringKey("k_bound"), "bound", stringKey("k_clash"), "bound-win"));

    Context ctx =
        ContextAttributes.add(
            Context.current(),
            Attributes.of(stringKey("k_ctx"), "ctx", stringKey("k_clash"), "ctx-lose"));
    try (Scope ignored = ctx.makeCurrent()) {
      bound.add(7);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("bound_counter_delta")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(7)
                                        .hasAttributes(
                                            attributeEntry("k_bound", "bound"),
                                            attributeEntry("k_clash", "bound-win")))));
  }

  @Test
  void contextScopedAttributes_doubleCounter_explicitContext() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    DoubleCounter counter = meter.counterBuilder("double_counter").ofDoubles().build();

    Context explicitCtx =
        ContextAttributes.add(
            Context.root(),
            Attributes.of(stringKey("k_ctx"), "ctx", stringKey("k_clash"), "ctx-lose"));

    counter.add(
        2.5,
        Attributes.of(stringKey("k_explicit"), "val", stringKey("k_clash"), "explicit-win"),
        explicitCtx);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("double_counter")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(2.5)
                                        .hasAttributes(
                                            attributeEntry("k_explicit", "val"),
                                            attributeEntry("k_clash", "explicit-win"),
                                            attributeEntry("k_ctx", "ctx")))));
  }

  @Test
  void contextScopedAttributes_boundCounter_cumulative_noContextAttributes() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    ExtendedLongCounter counter =
        (ExtendedLongCounter) meter.counterBuilder("bound_counter_no_ctx").build();
    BoundLongCounter bound = counter.bind(Attributes.of(stringKey("k_bound"), "bound"));

    bound.add(10);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("bound_counter_no_ctx")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(10)
                                        .hasAttributes(attributeEntry("k_bound", "bound")))));
  }

  @Test
  void contextScopedAttributes_boundCounter_delta_noContextAttributes() {
    InMemoryMetricReader reader = InMemoryMetricReader.createDelta();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    ExtendedLongCounter counter =
        (ExtendedLongCounter) meter.counterBuilder("bound_counter_delta_no_ctx").build();
    BoundLongCounter bound = counter.bind(Attributes.of(stringKey("k_bound"), "bound"));

    bound.add(7);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("bound_counter_delta_no_ctx")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(7)
                                        .hasAttributes(attributeEntry("k_bound", "bound")))));
  }

  @Test
  void contextScopedAttributes_boundDoubleCounter_cumulative() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    ExtendedDoubleCounter counter =
        (ExtendedDoubleCounter) meter.counterBuilder("bound_double_cumulative").ofDoubles().build();
    BoundDoubleCounter bound =
        counter.bind(
            Attributes.of(stringKey("k_bound"), "bound", stringKey("k_clash"), "bound-win"));

    bound.add(1.5);

    Context ctx =
        ContextAttributes.add(
            Context.current(),
            Attributes.of(stringKey("k_ctx"), "ctx", stringKey("k_clash"), "ctx-lose"));
    try (Scope ignored = ctx.makeCurrent()) {
      bound.add(2.5);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("bound_double_cumulative")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(4.0)
                                        .hasAttributes(
                                            attributeEntry("k_bound", "bound"),
                                            attributeEntry("k_clash", "bound-win")))));
  }

  @Test
  void contextScopedAttributes_boundDoubleCounter_delta() {
    InMemoryMetricReader reader = InMemoryMetricReader.createDelta();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    ExtendedDoubleCounter counter =
        (ExtendedDoubleCounter) meter.counterBuilder("bound_double_delta").ofDoubles().build();
    BoundDoubleCounter bound =
        counter.bind(
            Attributes.of(stringKey("k_bound"), "bound", stringKey("k_clash"), "bound-win"));

    bound.add(1.5);

    Context ctx =
        ContextAttributes.add(
            Context.current(),
            Attributes.of(stringKey("k_ctx"), "ctx", stringKey("k_clash"), "ctx-lose"));
    try (Scope ignored = ctx.makeCurrent()) {
      bound.add(2.5);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("bound_double_delta")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(4.0)
                                        .hasAttributes(
                                            attributeEntry("k_bound", "bound"),
                                            attributeEntry("k_clash", "bound-win")))));
  }

  @Test
  void contextScopedAttributes_boundCounter_droppedInstrument() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setName("dropped_*").build(),
                View.builder().setAggregation(Aggregation.drop()).build());
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
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
  void contextScopedAttributes_unboundCounter_emptyAttributesUsesContextAttributesDirectly() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    LongCounter counter = meter.counterBuilder("counter_empty_attrs").build();

    Context ctx =
        ContextAttributes.add(Context.current(), Attributes.of(stringKey("k_ctx"), "ctx"));
    try (Scope ignored = ctx.makeCurrent()) {
      counter.add(1, Attributes.empty());
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("counter_empty_attrs")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1)
                                        .hasAttributes(attributeEntry("k_ctx", "ctx")))));
  }

  @Test
  void contextScopedAttributes_doubleCounter_nanDropped() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");
    DoubleCounter counter = meter.counterBuilder("double_nan").ofDoubles().build();

    Context ctx =
        ContextAttributes.add(Context.current(), Attributes.of(stringKey("k_ctx"), "ctx"));
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
  void contextScopedAttributes_boundAndUnbound_isolationInSameContext() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder().registerMetricReader(reader);
    SdkMeterProviderUtil.setContextScopedAttributesEnabled(builder, true);
    SdkMeterProvider provider = builder.build();
    Meter meter = provider.get("test");

    LongCounter unboundCounter = meter.counterBuilder("unbound_requests").build();
    ExtendedLongCounter extendedCounter =
        (ExtendedLongCounter) meter.counterBuilder("bound_infra").build();
    BoundLongCounter boundCounter =
        extendedCounter.bind(Attributes.of(stringKey("pool"), "shared-infra"));

    Context ctx =
        ContextAttributes.add(Context.current(), Attributes.of(stringKey("tenant.id"), "acme"));

    try (Scope ignored = ctx.makeCurrent()) {
      unboundCounter.add(1, Attributes.of(stringKey("route"), "/api"));
      boundCounter.add(1);
    }

    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("unbound_requests")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1)
                                        .hasAttributes(
                                            attributeEntry("route", "/api"),
                                            attributeEntry("tenant.id", "acme")))),
            metric ->
                assertThat(metric)
                    .hasName("bound_infra")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1)
                                        .hasAttributes(attributeEntry("pool", "shared-infra")))));
  }
}
