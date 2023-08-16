/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static java.util.Arrays.asList;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.extension.incubator.metrics.ExtendedDoubleCounterBuilder;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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

  @Test
  void counterWithoutAdvice() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    DoubleCounter counter =
        meterProvider.get("meter").counterBuilder("counter").ofDoubles().build();
    counter.add(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasDoubleSumSatisfying(
                        sum -> sum.hasPointsSatisfying(point -> point.hasAttributes(ATTRIBUTES))));
  }

  @Test
  void counterWithAdvice() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();

    DoubleCounterBuilder doubleCounterBuilder =
        meterProvider.get("meter").counterBuilder("counter").ofDoubles();
    ((ExtendedDoubleCounterBuilder) doubleCounterBuilder)
        .setAdvice(advice -> advice.setAttributes(asList(stringKey("key1"), stringKey("key2"))));
    DoubleCounter counter = doubleCounterBuilder.build();
    counter.add(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasAttributesSatisfyingExactly(
                                        equalTo(stringKey("key1"), "1"),
                                        equalTo(stringKey("key2"), "2")))));
  }

  @Test
  void counterWithAdviceAndViews() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(reader)
            .registerView(
                InstrumentSelector.builder().setType(InstrumentType.COUNTER).build(),
                View.builder()
                    .setAttributeFilter(key -> "key2".equals(key) || "key3".equals(key))
                    .build())
            .build();

    DoubleCounterBuilder doubleCounterBuilder =
        meterProvider.get("meter").counterBuilder("counter").ofDoubles();
    ((ExtendedDoubleCounterBuilder) doubleCounterBuilder)
        .setAdvice(advice -> advice.setAttributes(asList(stringKey("key1"), stringKey("key2"))));
    DoubleCounter counter = doubleCounterBuilder.build();
    counter.add(1, ATTRIBUTES);

    assertThat(reader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point.hasAttributesSatisfyingExactly(
                                        equalTo(stringKey("key2"), "2"),
                                        equalTo(stringKey("key3"), "3")))));
  }
}
