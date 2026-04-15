/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import static io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.metrics.internal.MeterConfig.disabled;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of extended Metrics API. */
class ExtendedMetricsApiUsageTest {

  @Test
  void meterEnabled() {
    // Setup SdkMeterProvider
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProviderBuilder meterProviderBuilder =
        SdkMeterProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // In-memory reader used for demonstration purposes
            .registerMetricReader(reader);
    // Disable meterB
    SdkMeterProviderUtil.addMeterConfiguratorCondition(
        meterProviderBuilder, nameEquals("meterB"), disabled());
    SdkMeterProvider meterProvider = meterProviderBuilder.build();

    // Create meterA and meterB, and corresponding instruments
    Meter meterA = meterProvider.get("meterA");
    Meter meterB = meterProvider.get("meterB");
    ExtendedDoubleHistogram histogramA =
        (ExtendedDoubleHistogram) meterA.histogramBuilder("histogramA").build();
    ExtendedDoubleHistogram histogramB =
        (ExtendedDoubleHistogram) meterB.histogramBuilder("histogramB").build();

    // Check if instrument is enabled before recording measurement and avoid unnecessary computation
    if (histogramA.isEnabled()) {
      histogramA.record(1.0, Attributes.builder().put("result", flipCoin()).build());
    }
    if (histogramB.isEnabled()) {
      histogramA.record(1.0, Attributes.builder().put("result", flipCoin()).build());
    }

    // histogramA is enabled since meterA is enabled, histogramB is disabled since meterB is
    // disabled
    assertThat(histogramA.isEnabled()).isTrue();
    assertThat(histogramB.isEnabled()).isFalse();

    // Collected data only consists of metrics from meterA. Note, meterB's histogramB would be
    // omitted from the results even if values were recorded. The check if enabled simply avoids
    // unnecessary computation.
    assertThat(reader.collectAllMetrics())
        .allSatisfy(
            metric ->
                assertThat(metric.getInstrumentationScopeInfo().getName()).isEqualTo("meterA"));
  }

  private static final Random random = new Random();

  private static String flipCoin() {
    return random.nextBoolean() ? "heads" : "tails";
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
