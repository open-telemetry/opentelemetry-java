/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongLastValueAggregator}. */
class LongLastValueAggregatorTest {
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.empty();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private static final LongLastValueAggregator aggregator =
      new LongLastValueAggregator(ExemplarReservoir::longNoSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(LongLastValueAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<LongPointData, LongExemplarData> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(12L);
    aggregatorHandle.recordLong(13);
    aggregatorHandle.recordLong(14);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(14L);
  }

  @Test
  void aggregateThenMaybeReset() {
    AggregatorHandle<LongPointData, LongExemplarData> aggregatorHandle = aggregator.createHandle();

    aggregatorHandle.recordLong(13);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(13L);

    aggregatorHandle.recordLong(12);
    assertThat(
            aggregatorHandle
                .aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true)
                .getValue())
        .isEqualTo(12L);
  }

  @Test
  void toMetricData() {
    AggregatorHandle<LongPointData, LongExemplarData> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonList(
                aggregatorHandle.aggregateThenMaybeReset(
                    2, 100, Attributes.empty(), /* reset= */ true)),
            AggregationTemporality.CUMULATIVE);
    assertThat(metricData)
        .isEqualTo(
            ImmutableMetricData.createLongGauge(
                Resource.getDefault(),
                InstrumentationScopeInfo.empty(),
                "name",
                "description",
                "unit",
                ImmutableGaugeData.create(
                    Collections.singletonList(
                        ImmutableLongPointData.create(2, 100, Attributes.empty(), 10)))));
  }
}
