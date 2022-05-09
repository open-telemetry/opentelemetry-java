/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram.ExponentialHistogramData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class MetricAssertionsTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("instrumentation_library");

  private static final MetricData EXPONENTIAL_HISTOGRAM_METRIC =
      ImmutableMetricData.createExponentialHistogram(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "exponential_histogram",
          /* description= */ "description",
          /* unit= */ "unit",
          ExponentialHistogramData.create(
              AggregationTemporality.CUMULATIVE,
              // Points
              Collections.emptyList()));

  private static final MetricData EXPONENTIAL_HISTOGRAM_DELTA_METRIC =
      ImmutableMetricData.createExponentialHistogram(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "exponential_histogram_delta",
          /* description= */ "description",
          /* unit= */ "unit",
          ExponentialHistogramData.create(
              AggregationTemporality.DELTA,
              // Points
              Collections.emptyList()));

  private static final MetricData DOUBLE_GAUGE_METRIC =
      ImmutableMetricData.createDoubleGauge(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "gauge",
          /* description= */ "description",
          /* unit= */ "unit",
          ImmutableGaugeData.create(
              // Points
              Collections.emptyList()));

  @Test
  void metric_passing() {
    assertThat(EXPONENTIAL_HISTOGRAM_METRIC)
        .hasResource(RESOURCE)
        .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
        .hasName("exponential_histogram")
        .hasDescription("description")
        .hasUnit("unit");
  }

  @Test
  void metric_fails() {
    assertThatThrownBy(
            () ->
                assertThat(EXPONENTIAL_HISTOGRAM_METRIC)
                    .hasResource(
                        Resource.create(Attributes.of(stringKey("monkey_key"), "resource_value"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(EXPONENTIAL_HISTOGRAM_METRIC)
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create("instrumentation_library_for_monkeys")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(EXPONENTIAL_HISTOGRAM_METRIC).hasName("Monkeys"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(EXPONENTIAL_HISTOGRAM_METRIC).hasDescription("Monkeys"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(EXPONENTIAL_HISTOGRAM_METRIC).hasUnit("Monkeys"))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void exponential_histogram_passing() {
    assertThat(EXPONENTIAL_HISTOGRAM_METRIC).hasExponentialHistogram().isCumulative();
    assertThat(EXPONENTIAL_HISTOGRAM_DELTA_METRIC).hasExponentialHistogram().isDelta();
  }

  @Test
  void exponential_histogram_fails() {
    assertThatThrownBy(() -> assertThat(DOUBLE_GAUGE_METRIC).hasExponentialHistogram())
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(EXPONENTIAL_HISTOGRAM_METRIC).hasExponentialHistogram().isDelta())
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(EXPONENTIAL_HISTOGRAM_DELTA_METRIC)
                    .hasExponentialHistogram()
                    .isCumulative())
        .isInstanceOf(AssertionError.class);
  }
}
