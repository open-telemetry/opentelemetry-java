/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.metrics.TestMetricData;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

class DelegatingMetricDataTest {

  private static final class NoOpDelegatingMetricData extends DelegatingMetricData {
    private NoOpDelegatingMetricData(MetricData delegate) {
      super(delegate);
    }
  }

  private static final class MetricDataWithCustomDescription extends DelegatingMetricData {
    private final String description;

    private MetricDataWithCustomDescription(MetricData delegate) {
      super(delegate);
      this.description = "test";
    }

    @Override
    public String getDescription() {
      return description;
    }

  }

  @Test
  void delegates() {
    MetricData metricData = createBasicMetricBuilder().build();
    MetricData noOpWrapper = new NoOpDelegatingMetricData(metricData);

    assertThat(noOpWrapper)
        .usingRecursiveComparison(
            RecursiveComparisonConfiguration.builder().withIgnoredFields("delegate").build())
        .isEqualTo(metricData);
  }

  @Test
  void overrideDelegate() {
    MetricData metricData = createBasicMetricBuilder().build();
    MetricData withCustomDescription = new MetricDataWithCustomDescription(metricData);

    assertThat(withCustomDescription.getDescription()).isEqualTo("test");
  }
  @Test
  void equals() {
    MetricData metricData = createBasicMetricBuilder().build();
    MetricData noOpWrapper = new NoOpDelegatingMetricData(metricData);
    MetricData withCustomDescription = new MetricDataWithCustomDescription(metricData);

    assertThat(noOpWrapper).isEqualTo(metricData);
    assertThat(metricData).isNotEqualTo(withCustomDescription);
  }

  private static TestMetricData.Builder createBasicMetricBuilder() {
    return TestMetricData.builder()
        .setResource(Resource.empty())
        .setInstrumentationScopeInfo(InstrumentationScopeInfo.empty())
        .setDescription("")
        .setUnit("1")
        .setName("name")
        .setData(ImmutableSummaryData.empty())
        .setType(MetricDataType.SUMMARY); // Not sure what type should be here
  }
}
