/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class MetricExportBatcherTest {

  @Test
  void constructor_InvalidMaxExportBatchSize() {
    assertThatThrownBy(() -> new MetricExportBatcher(0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxExportBatchSize must be positive");
    assertThatThrownBy(() -> new MetricExportBatcher(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxExportBatchSize must be positive");
  }

  @Test
  void toString_Valid() {
    MetricExportBatcher batcher = new MetricExportBatcher(10);
    assertThat(batcher.toString()).isEqualTo("MetricExportBatcher{maxExportBatchSize=10}");
  }

  @Test
  void batchMetrics_EmptyMetrics() {
    MetricExportBatcher batcher = new MetricExportBatcher(10);
    assertThat(batcher.batchMetrics(Collections.emptyList())).isEmpty();
  }

  @Test
  void batchMetrics_MetricFitsIntact() {
    MetricExportBatcher batcher = new MetricExportBatcher(10);
    LongPointData p1 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 1L);
    MetricData metric =
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableGaugeData.create(Collections.singletonList(p1)));

    Collection<Collection<MetricData>> batches =
        batcher.batchMetrics(Collections.singletonList(metric));
    assertThat(batches).hasSize(1);
    assertThat(batches.iterator().next()).containsExactly(metric);
  }

  @Test
  @SuppressWarnings("all")
  void batchMetrics_SplitsDoubleGauge_LastBatchPartiallyFilled() {
    MetricExportBatcher batcher = new MetricExportBatcher(2);
    DoublePointData p1 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 1.0);
    DoublePointData p2 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 2.0);
    DoublePointData p3 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 3.0);
    DoublePointData p4 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 4.0);
    DoublePointData p5 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 5.0);

    MetricData metric =
        ImmutableMetricData.createDoubleGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableGaugeData.create(Arrays.asList(p1, p2, p3, p4, p5)));

    Collection<Collection<MetricData>> batches =
        batcher.batchMetrics(Collections.singletonList(metric));
    List<Collection<MetricData>> batchesList = new ArrayList<>(batches);

    assertThat(batchesList.size()).isEqualTo(3);
    Collection<MetricData> firstBatch = batchesList.get(0);
    Collection<MetricData> secondBatch = batchesList.get(1);
    Collection<MetricData> thirdBatch = batchesList.get(2);

    assertThat(firstBatch.size()).isEqualTo(1);
    assertThat(secondBatch.size()).isEqualTo(1);
    assertThat(thirdBatch.size()).isEqualTo(1);

    MetricData firsBatch_m1 = firstBatch.iterator().next();
    assertThat(firsBatch_m1.getType()).isEqualByComparingTo(MetricDataType.DOUBLE_GAUGE);
    assertThat(firsBatch_m1.getDoubleGaugeData().getPoints()).containsExactly(p1, p2);

    MetricData secondBatch_m1 = secondBatch.iterator().next();
    assertThat(secondBatch_m1.getType()).isEqualByComparingTo(MetricDataType.DOUBLE_GAUGE);
    assertThat(secondBatch_m1.getDoubleGaugeData().getPoints()).containsExactly(p3, p4);

    // Last batch is partially filled.
    MetricData thirdBatch_m1 = thirdBatch.iterator().next();
    assertThat(thirdBatch_m1.getType()).isEqualByComparingTo(MetricDataType.DOUBLE_GAUGE);
    assertThat(thirdBatch_m1.getDoubleGaugeData().getPoints()).containsExactly(p5);
  }

  @Test
  void batchMetrics_SplitsLongGauge_SingleBatchPartiallyFilled() {
    MetricExportBatcher batcher = new MetricExportBatcher(4);
    LongPointData p1 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 1L);
    LongPointData p2 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 2L);
    LongPointData p3 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 3L);

    MetricData metric =
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableGaugeData.create(Arrays.asList(p1, p2, p3)));

    Collection<Collection<MetricData>> batches =
        batcher.batchMetrics(Collections.singletonList(metric));

    assertThat(batches).hasSize(1);
    Collection<MetricData> firstBatch = batches.iterator().next();
    assertThat(firstBatch).hasSize(1); // There is only 1 MetricData

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.LONG_GAUGE);
    assertThat(m1.getLongGaugeData().getPoints()).containsExactly(p1, p2, p3);
  }

  @Test
  void batchMetrics_SplitsDoubleSum_SingleBatchCompletelyFilled() {
    MetricExportBatcher batcher = new MetricExportBatcher(2);
    DoublePointData p1 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 1.0);
    DoublePointData p2 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 2.0);

    MetricData metric =
        ImmutableMetricData.createDoubleSum(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableSumData.create(
                /* isMonotonic= */ true, AggregationTemporality.CUMULATIVE, Arrays.asList(p1, p2)));

    Collection<Collection<MetricData>> batches =
        batcher.batchMetrics(Collections.singletonList(metric));

    Collection<MetricData> firstBatch = batches.iterator().next();
    assertThat(firstBatch).hasSize(1); // There is only 1 MetricData

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.DOUBLE_SUM);
    assertThat(m1.getDoubleSumData().getPoints()).containsExactly(p1, p2);
    assertThat(m1.getDoubleSumData().isMonotonic()).isTrue();
    assertThat(m1.getDoubleSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void batchMetrics_SplitsLongSum_MultipleBatchesCompletelyFilled_MultipleMetrics() {
    MetricExportBatcher batcher = new MetricExportBatcher(1);
    LongPointData p1 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 1L);
    LongPointData p2 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 2L);

    MetricData metric_1 =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name_1",
            "desc_1",
            "1",
            ImmutableSumData.create(
                /* isMonotonic= */ false, AggregationTemporality.DELTA, Arrays.asList(p1, p2)));

    MetricData metric_2 =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name_2",
            "desc_2",
            "1",
            ImmutableSumData.create(
                /* isMonotonic= */ false, AggregationTemporality.DELTA, Arrays.asList(p1, p2)));

    Collection<Collection<MetricData>> batches =
        batcher.batchMetrics(Arrays.asList(metric_1, metric_2));

    assertThat(batches).hasSize(4);
    Collection<MetricData> firstBatch = batches.iterator().next();
    Collection<MetricData> secondBatch = batches.stream().skip(1).findFirst().get();
    Collection<MetricData> thirdBatch = batches.stream().skip(2).findFirst().get();
    Collection<MetricData> fourthBatch = batches.stream().skip(3).findFirst().get();

    assertThat(firstBatch).hasSize(1);
    assertThat(secondBatch).hasSize(1);
    assertThat(thirdBatch).hasSize(1);
    assertThat(fourthBatch).hasSize(1);

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.LONG_SUM);
    assertThat(m1.getName()).isEqualTo("name_1");
    assertThat(m1.getDescription()).isEqualTo("desc_1");
    assertThat(m1.getUnit()).isEqualTo("1");
    assertThat(m1.getLongSumData().getPoints()).containsExactly(p1);
    assertThat(m1.getLongSumData().isMonotonic()).isFalse();
    assertThat(m1.getLongSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);

    MetricData m2 = secondBatch.iterator().next();
    assertThat(m2.getType()).isEqualTo(MetricDataType.LONG_SUM);
    assertThat(m2.getName()).isEqualTo("name_1");
    assertThat(m2.getDescription()).isEqualTo("desc_1");
    assertThat(m2.getUnit()).isEqualTo("1");
    assertThat(m2.getLongSumData().getPoints()).containsExactly(p2);
    assertThat(m2.getLongSumData().isMonotonic()).isFalse();
    assertThat(m2.getLongSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);

    MetricData m3 = thirdBatch.iterator().next();
    assertThat(m3.getType()).isEqualTo(MetricDataType.LONG_SUM);
    assertThat(m3.getName()).isEqualTo("name_2");
    assertThat(m3.getDescription()).isEqualTo("desc_2");
    assertThat(m3.getUnit()).isEqualTo("1");
    assertThat(m3.getLongSumData().getPoints()).containsExactly(p1);
    assertThat(m3.getLongSumData().isMonotonic()).isFalse();
    assertThat(m3.getLongSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);

    MetricData m4 = fourthBatch.iterator().next();
    assertThat(m4.getType()).isEqualTo(MetricDataType.LONG_SUM);
    assertThat(m4.getName()).isEqualTo("name_2");
    assertThat(m4.getDescription()).isEqualTo("desc_2");
    assertThat(m4.getUnit()).isEqualTo("1");
    assertThat(m4.getLongSumData().getPoints()).containsExactly(p2);
    assertThat(m4.getLongSumData().isMonotonic()).isFalse();
    assertThat(m4.getLongSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void batchMetrics_SplitsHistogram_MultipleBatchesCompletelyFilled_SingleMetric() {
    MetricExportBatcher batcher = new MetricExportBatcher(1);
    ImmutableHistogramPointData p1 =
        ImmutableHistogramPointData.create(
            1,
            2,
            Attributes.empty(),
            1.0,
            /* hasMin= */ false,
            0.0,
            /* hasMax= */ false,
            0.0,
            Collections.emptyList(),
            Collections.singletonList(1L));
    ImmutableHistogramPointData p2 =
        ImmutableHistogramPointData.create(
            1,
            2,
            Attributes.empty(),
            2.0,
            /* hasMin= */ false,
            0.0,
            /* hasMax= */ false,
            0.0,
            Collections.emptyList(),
            Collections.singletonList(2L));

    MetricData metric =
        ImmutableMetricData.createDoubleHistogram(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableHistogramData.create(
                AggregationTemporality.CUMULATIVE, Arrays.asList(p1, p2)));

    Collection<Collection<MetricData>> batches =
        batcher.batchMetrics(Collections.singletonList(metric));

    assertThat(batches).hasSize(2);
    Collection<MetricData> firstBatch = batches.iterator().next();
    Collection<MetricData> secondBatch = batches.stream().skip(1).findFirst().get();
    assertThat(firstBatch).hasSize(1);
    assertThat(secondBatch).hasSize(1);

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.HISTOGRAM);
    assertThat(m1.getHistogramData().getPoints()).containsExactly(p1);
    assertThat(m1.getHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    MetricData m2 = secondBatch.iterator().next();
    assertThat(m2.getType()).isEqualTo(MetricDataType.HISTOGRAM);
    assertThat(m2.getHistogramData().getPoints()).containsExactly(p2);
    assertThat(m2.getHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void batchMetrics_EmptyPointsInMetricData() {
    MetricExportBatcher batcher = new MetricExportBatcher(2);
    MetricData metric =
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableGaugeData.create(Collections.emptyList()));

    Collection<Collection<MetricData>> batches =
        batcher.batchMetrics(Collections.singletonList(metric));
    assertThat(batches).hasSize(1);
    assertThat(batches.iterator().next()).containsExactly(metric);
  }

  @Test
  void batchMetrics_MultipleMetricsExactCapacityMatch() {
    MetricExportBatcher batcher = new MetricExportBatcher(4);
    LongPointData p1 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 1L);
    LongPointData p2 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 2L);
    LongPointData p3 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 3L);
    LongPointData p4 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 4L);

    MetricData m1 =
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name_1",
            "desc",
            "1",
            ImmutableGaugeData.create(Arrays.asList(p1, p2)));
    MetricData m2 =
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name_2",
            "desc",
            "1",
            ImmutableGaugeData.create(Arrays.asList(p3, p4)));

    Collection<Collection<MetricData>> batches = batcher.batchMetrics(Arrays.asList(m1, m2));
    assertThat(batches).hasSize(1);
    assertThat(batches.iterator().next()).containsExactly(m1, m2);
  }
}
