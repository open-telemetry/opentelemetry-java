/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.Test;

class TestExtendedOpenTelemetrySdk {
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();

  @Test
  void endToEnd() {
    ExtendedOpenTelemetrySdk otel =
        ExtendedOpenTelemetrySdk.builder()
            .withMeterProvider(builder -> builder.registerMetricReader(sdkMeterReader))
            .build();
    // Generate our first entity.
    otel.getResourceProvider()
        .getResource()
        .attachEntity("test")
        .withId(Attributes.builder().put("test.id", 1).build())
        .emit();
    // Write a metric.
    Meter meter = otel.getMeterProvider().get("test.scope");
    LongCounter counter = meter.counterBuilder("testCounter").build();
    counter.add(1, Attributes.empty());

    // Verify we see the entity and the metric.
    assertThat(sdkMeterReader.collectAllMetrics())
        .anySatisfy(
            metric ->
                assertThat(metric)
                    .hasName("testCounter")
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                attributes -> assertThat(attributes).containsEntry("test.id", 1))));

    // Now update the resource and check the point.
    otel.getResourceProvider()
        .getResource()
        .attachEntity("test2")
        .withId(Attributes.builder().put("test2.id", 1).build())
        .emit();
    // Verify we see the new entity and the metric.
    assertThat(sdkMeterReader.collectAllMetrics())
        .anySatisfy(
            metric ->
                assertThat(metric)
                    .hasName("testCounter")
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                attributes ->
                                    assertThat(attributes)
                                        .containsEntry("test.id", 1)
                                        .containsEntry("test2.id", 1))));
  }
}
