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
}
