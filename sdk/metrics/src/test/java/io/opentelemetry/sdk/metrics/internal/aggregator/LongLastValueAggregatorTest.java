/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongLastValueAggregator}. */
class LongLastValueAggregatorTest {
  private static final Resource RESOURCE = Resource.getDefault();
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.empty();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private static final LongLastValueAggregator aggregator =
      new LongLastValueAggregator(ExemplarReservoir::noSamples);

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle()).isInstanceOf(LongLastValueAggregator.Handle.class);
  }

  @Test
  void multipleRecords() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue()).isEqualTo(12L);
    aggregatorHandle.recordLong(13);
    aggregatorHandle.recordLong(14);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue()).isEqualTo(14L);
  }

  @Test
  void toAccumulationAndReset() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(13);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue()).isEqualTo(13L);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordLong(12);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty()).getValue()).isEqualTo(12L);
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void mergeAccumulation() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar = LongExemplarData.create(attributes, 2L, "spanid", "traceid", 1);
    List<ExemplarData> exemplars = Collections.singletonList(exemplar);
    List<ExemplarData> previousExemplars =
        Collections.singletonList(LongExemplarData.create(attributes, 1L, "spanId", "traceId", 2));
    LongAccumulation result =
        aggregator.merge(
            LongAccumulation.create(1, previousExemplars), LongAccumulation.create(2, exemplars));
    // Assert that latest measurement is kept.
    assertThat(result).isEqualTo(LongAccumulation.create(2, exemplars));
  }

  @Test
  void toMetricData() {
    AggregatorHandle<LongAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordLong(10);

    MetricData metricData =
        aggregator.toMetricData(
            RESOURCE,
            INSTRUMENTATION_LIBRARY_INFO,
            METRIC_DESCRIPTOR,
            Collections.singletonMap(
                Attributes.empty(), aggregatorHandle.accumulateThenReset(Attributes.empty())),
            AggregationTemporality.CUMULATIVE,
            2,
            10,
            100);
    assertThat(metricData)
        .isEqualTo(
            MetricData.createLongGauge(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                "name",
                "description",
                "unit",
                LongGaugeData.create(
                    Collections.singletonList(
                        LongPointData.create(2, 100, Attributes.empty(), 10)))));
  }
}
