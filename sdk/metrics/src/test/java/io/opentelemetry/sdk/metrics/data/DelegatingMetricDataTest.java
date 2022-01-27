/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

class DelegatingMetricDataTest {

  @Test
  void delegates() {
    MetricData metricData = createMetricData();
    MetricData noopWrapper = new NoOpDelegatingMetricData(metricData);
    // Test should always verify delegation is working even when methods are added since it calls
    // each method individually.
    Assertions.assertThat(noopWrapper)
        .usingRecursiveComparison(
            RecursiveComparisonConfiguration.builder().withIgnoredFields("delegate").build())
        .isEqualTo(metricData);
  }

  @Test
  void overrideDelegate() {
    MetricData metricData = createMetricData();
    MetricData enrichedMetricData =
        new MetricDataWithAttributes(
            metricData, Attributes.builder().put("key2", "value2").build());
    assertThat(enrichedMetricData)
        .hasLongSum()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasAttributes(
                        Attributes.builder().put("key1", "value1").put("key2", "value2").build()));
  }

  @Test
  void equals() {
    MetricData metricData = createMetricData();
    MetricData noopWrapper = new NoOpDelegatingMetricData(metricData);
    MetricData enrichedMetricData =
        new MetricDataWithAttributes(
            metricData, Attributes.builder().put("key2", "value2").build());

    assertThat(noopWrapper).isEqualTo(metricData);
    // TODO(jack-berg): Bug - metricData.equals(noopWrapper) should be equal but AutoValue does not
    // implement equals for interfaces properly. We can't add it as a separate group either since
    // noopWrapper.equals(metricData) does work properly.
    assertThat(metricData).isNotEqualTo(noopWrapper);

    new EqualsTester()
        .addEqualityGroup(noopWrapper)
        .addEqualityGroup(enrichedMetricData)
        .testEquals();
  }

  static MetricData createMetricData() {
    return MetricData.createLongSum(
        Resource.getDefault(),
        InstrumentationLibraryInfo.create("test", null),
        "name",
        "description",
        "unit",
        LongSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singleton(
                LongPointData.create(
                    TimeUnit.MILLISECONDS.toNanos(Instant.now().toEpochMilli()),
                    TimeUnit.MILLISECONDS.toNanos(Instant.now().plusSeconds(60).toEpochMilli()),
                    Attributes.builder().put("key1", "value1").build(),
                    10))));
  }

  private static final class NoOpDelegatingMetricData extends DelegatingMetricData {
    private NoOpDelegatingMetricData(MetricData delegate) {
      super(delegate);
    }
  }

  private static class MetricDataWithAttributes extends DelegatingMetricData {

    private final Attributes attributes;

    private MetricDataWithAttributes(MetricData delegate, Attributes attributes) {
      super(delegate);
      this.attributes = attributes;
    }

    @Override
    public Data<?> getData() {
      if (!super.getType().equals(MetricDataType.LONG_SUM)) {
        return super.getData();
      }
      LongSumData longSumData = (LongSumData) super.getData();
      List<LongPointData> points =
          longSumData.getPoints().stream()
              .map(
                  pointData ->
                      LongPointData.create(
                          pointData.getStartEpochNanos(),
                          pointData.getEpochNanos(),
                          pointData.getAttributes().toBuilder().putAll(attributes).build(),
                          pointData.getValue(),
                          pointData.getExemplars()))
              .collect(Collectors.toList());
      return LongSumData.create(
          longSumData.isMonotonic(), longSumData.getAggregationTemporality(), points);
    }
  }
}
