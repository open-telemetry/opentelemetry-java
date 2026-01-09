/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.metrics.TestMetricData;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

class DelegatingMetricDataTest {

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

  @Test
  void equals_sameInstance() {
    MetricData metricData = createBasicMetricBuilder().build();
    MetricData noOpWrapper = new NoOpDelegatingMetricData(metricData);

    assertThat(noOpWrapper.equals(noOpWrapper)).isTrue();
  }

  @Test
  void equals_differentResource() {
    MetricData metricData1 =
        createBasicMetricBuilder()
            .setResource(Resource.create(Attributes.builder().put("key", "value1").build()))
            .build();
    MetricData metricData2 =
        createBasicMetricBuilder()
            .setResource(Resource.create(Attributes.builder().put("key", "value2").build()))
            .build();
    MetricData noOpWrapper1 = new NoOpDelegatingMetricData(metricData1);
    MetricData noOpWrapper2 = new NoOpDelegatingMetricData(metricData2);

    assertThat(noOpWrapper1).isNotEqualTo(noOpWrapper2);
  }

  @Test
  void equals_differentInstrumentationScopeInfo() {
    MetricData metricData1 =
        createBasicMetricBuilder()
            .setInstrumentationScopeInfo(InstrumentationScopeInfo.create("scope1"))
            .build();
    MetricData metricData2 =
        createBasicMetricBuilder()
            .setInstrumentationScopeInfo(InstrumentationScopeInfo.create("scope2"))
            .build();
    MetricData noOpWrapper1 = new NoOpDelegatingMetricData(metricData1);
    MetricData noOpWrapper2 = new NoOpDelegatingMetricData(metricData2);

    assertThat(noOpWrapper1).isNotEqualTo(noOpWrapper2);
  }

  @Test
  void equals_differentName() {
    MetricData metricData1 = createBasicMetricBuilder().setName("name1").build();
    MetricData metricData2 = createBasicMetricBuilder().setName("name2").build();
    MetricData noOpWrapper1 = new NoOpDelegatingMetricData(metricData1);
    MetricData noOpWrapper2 = new NoOpDelegatingMetricData(metricData2);

    assertThat(noOpWrapper1).isNotEqualTo(noOpWrapper2);
  }

  @Test
  void equals_differentUnit() {
    MetricData metricData1 = createBasicMetricBuilder().setUnit("unit1").build();
    MetricData metricData2 = createBasicMetricBuilder().setUnit("unit2").build();
    MetricData noOpWrapper1 = new NoOpDelegatingMetricData(metricData1);
    MetricData noOpWrapper2 = new NoOpDelegatingMetricData(metricData2);

    assertThat(noOpWrapper1).isNotEqualTo(noOpWrapper2);
  }

  @Test
  void equals_differentType() {
    MetricData metricData1 =
        createBasicMetricBuilder().setHistogramData(ImmutableHistogramData.empty()).build();
    MetricData metricData2 =
        createBasicMetricBuilder().setSummaryData(ImmutableSummaryData.empty()).build();
    MetricData noOpWrapper1 = new NoOpDelegatingMetricData(metricData1);
    MetricData noOpWrapper2 = new NoOpDelegatingMetricData(metricData2);

    assertThat(noOpWrapper1).isNotEqualTo(noOpWrapper2);
  }

  @Test
  void equals_differentData() {
    MetricData metricData1 =
        createBasicMetricBuilder().setSummaryData(ImmutableSummaryData.empty()).build();
    MetricData metricData2 =
        createBasicMetricBuilder().setSummaryData(ImmutableSummaryData.empty()).build();
    MetricData noOpWrapper1 = new NoOpDelegatingMetricData(metricData1);
    MetricData noOpWrapper2 = new NoOpDelegatingMetricData(metricData2);

    assertThat(noOpWrapper1).isEqualTo(noOpWrapper2);
  }

  @Test
  void equals_nonMetricDataObject_returnsFalse() {
    MetricData metricData = createBasicMetricBuilder().build();
    MetricData noOpWrapper = new NoOpDelegatingMetricData(metricData);

    // Compare with a String object (non-MetricData)
    Object nonMetricData = "not a metric data";

    assertThat(noOpWrapper.equals(nonMetricData)).isFalse();
  }

  @Test
  void testToString() {
    MetricData metricData = createBasicMetricBuilder().build();
    MetricData noOpWrapper = new NoOpDelegatingMetricData(metricData);

    String expectedString =
        "DelegatingMetricData{"
            + "resource="
            + metricData.getResource()
            + ", instrumentationScopeInfo="
            + metricData.getInstrumentationScopeInfo()
            + ", name="
            + metricData.getName()
            + ", description="
            + metricData.getDescription()
            + ", unit="
            + metricData.getUnit()
            + ", type="
            + metricData.getType()
            + ", data="
            + metricData.getData()
            + "}";

    assertThat(noOpWrapper.toString()).isEqualTo(expectedString);
  }

  @Test
  void testHashCode() {
    MetricData metricData1 = createBasicMetricBuilder().build();
    MetricData metricData2 = createBasicMetricBuilder().build();
    MetricData noOpWrapper1 = new NoOpDelegatingMetricData(metricData1);
    MetricData noOpWrapper2 = new NoOpDelegatingMetricData(metricData2);

    assertThat(noOpWrapper1.hashCode()).isEqualTo(metricData1.hashCode());
    assertThat(noOpWrapper2.hashCode()).isEqualTo(metricData2.hashCode());
    assertThat(noOpWrapper1.hashCode()).isEqualTo(noOpWrapper2.hashCode());
  }

  private static TestMetricData.Builder createBasicMetricBuilder() {
    return TestMetricData.builder()
        .setResource(Resource.empty())
        .setInstrumentationScopeInfo(InstrumentationScopeInfo.empty())
        .setDescription("")
        .setUnit("1")
        .setName("name")
        .setSummaryData(ImmutableSummaryData.empty());
  }

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
}
