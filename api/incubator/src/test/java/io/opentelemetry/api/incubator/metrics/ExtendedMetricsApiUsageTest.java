/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of extended Metrics API. */
class ExtendedMetricsApiUsageTest {

  @Test
  void synchronousGaugeUsage() {
    // Setup SdkMeterProvider
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // In-memory reader used for demonstration purposes
            .registerMetricReader(reader)
            .build();

    // Get a Meter for a scope
    Meter meter = meterProvider.get("org.foo.my-scope");

    // Cast GaugeBuilder to ExtendedDoubleGaugeBuilder
    DoubleGauge gauge = ((ExtendedDoubleGaugeBuilder) meter.gaugeBuilder("my-gauge")).build();

    // Call set synchronously to set the value
    gauge.set(1.0, Attributes.builder().put("key", "value1").build());
    gauge.set(2.0, Attributes.builder().put("key", "value2").build());

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasName("my-gauge")
                    .hasDoubleGaugeSatisfying(
                        gaugeAssert ->
                            gaugeAssert.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1.0)
                                        .hasAttributes(
                                            Attributes.builder().put("key", "value1").build()),
                                point ->
                                    point
                                        .hasValue(2.0)
                                        .hasAttributes(
                                            Attributes.builder().put("key", "value2").build()))));
  }

  @Test
  void attributesAdvice() {
    // Setup SdkMeterProvider
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // In-memory reader used for demonstration purposes
            .registerMetricReader(reader)
            // Register a view which indicates that for counter1, attributes key1, key2 should be
            // retained
            .registerView(
                InstrumentSelector.builder().setName("counter1").build(),
                View.builder().setAttributeFilter(ImmutableSet.of("key1", "key2")).build())
            .build();

    // Get a Meter for a scope
    Meter meter = meterProvider.get("org.foo.my-scope");

    // To apply attribute advice, cast the instrument builder to appropriate
    // Extended{Instrument}Builder, and call setAttributeAdvice
    // Here we create counter1 and counter2, both configured to only retain attribute key1. counter1
    // has a view configured which overrides this and retains key1, key2.
    LongCounter counter1 =
        ((ExtendedLongCounterBuilder) meter.counterBuilder("counter1"))
            .setAttributesAdvice(ImmutableList.of(AttributeKey.stringKey("key1")))
            .build();
    LongCounter counter2 =
        ((ExtendedLongCounterBuilder) meter.counterBuilder("counter2"))
            .setAttributesAdvice(ImmutableList.of(AttributeKey.stringKey("key1")))
            .build();

    // Record data with attribute key1, key2
    counter1.add(1, Attributes.builder().put("key1", "value1").put("key2", "value2").build());
    counter2.add(1, Attributes.builder().put("key1", "value1").put("key2", "value2").build());

    // Verify that counter1 has both key1, key2 since view overrides the attribute advice
    // Verify that counter2 only has key1, since attribute advice causes key2 to be dropped by
    // default
    assertThat(reader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("counter1")
                    .hasLongSumSatisfying(
                        sumAssert ->
                            sumAssert.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1L)
                                        .hasAttributes(
                                            Attributes.builder()
                                                .put("key1", "value1")
                                                .put("key2", "value2")
                                                .build()))),
            metricData ->
                assertThat(metricData)
                    .hasName("counter2")
                    .hasLongSumSatisfying(
                        sumAssert ->
                            sumAssert.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1L)
                                        .hasAttributes(
                                            Attributes.builder().put("key1", "value1").build()))));
  }
}
