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
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class MetricExportBatcherTest {

  @Test
  void batchMetrics_InvalidMaxExportBatchSize() {
    assertThatThrownBy(() -> MetricExportBatcher.batchMetrics(Collections.emptyList(), 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxExportBatchSize must be positive");
    assertThatThrownBy(() -> MetricExportBatcher.batchMetrics(Collections.emptyList(), -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxExportBatchSize must be positive");
  }

  @Test
  void batchMetrics_EmptyMetrics() {
    assertThat(MetricExportBatcher.batchMetrics(Collections.emptyList(), 10)).isEmpty();
  }

  @Test
  void batchMetrics_MetricFitsIntact() {
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
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 10);
    assertThat(batches).hasSize(1);
    assertThat(batches.iterator().next()).containsExactly(metric);
  }

  @Test
  void batchMetrics_SplitsDoubleGauge_LastBatchPartiallyFilled() {
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
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 2);
    List<Collection<MetricData>> batchesList = new ArrayList<>(batches);

    assertThat(batchesList.size()).isEqualTo(3);
    Collection<MetricData> firstBatch = batchesList.get(0);
    Collection<MetricData> secondBatch = batchesList.get(1);
    Collection<MetricData> thirdBatch = batchesList.get(2);

    assertThat(firstBatch.size()).isEqualTo(1);
    assertThat(secondBatch.size()).isEqualTo(1);
    assertThat(thirdBatch.size()).isEqualTo(1);

    MetricData b1m1 = firstBatch.iterator().next();
    assertThat(b1m1.getType()).isEqualByComparingTo(MetricDataType.DOUBLE_GAUGE);
    assertThat(b1m1.getName()).isEqualTo("name");
    assertThat(b1m1.getDescription()).isEqualTo("desc");
    assertThat(b1m1.getUnit()).isEqualTo("1");
    assertThat(b1m1.getDoubleGaugeData().getPoints()).containsExactly(p1, p2);

    MetricData b2m1 = secondBatch.iterator().next();
    assertThat(b2m1.getType()).isEqualByComparingTo(MetricDataType.DOUBLE_GAUGE);
    assertThat(b2m1.getName()).isEqualTo("name");
    assertThat(b2m1.getDescription()).isEqualTo("desc");
    assertThat(b2m1.getUnit()).isEqualTo("1");
    assertThat(b2m1.getDoubleGaugeData().getPoints()).containsExactly(p3, p4);

    // Last batch is partially filled.
    MetricData b3m1 = thirdBatch.iterator().next();
    assertThat(b3m1.getType()).isEqualByComparingTo(MetricDataType.DOUBLE_GAUGE);
    assertThat(b3m1.getName()).isEqualTo("name");
    assertThat(b3m1.getDescription()).isEqualTo("desc");
    assertThat(b3m1.getUnit()).isEqualTo("1");
    assertThat(b3m1.getDoubleGaugeData().getPoints()).containsExactly(p5);
  }

  @Test
  void batchMetrics_SplitsLongGauge_SingleBatchPartiallyFilled() {
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
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 4);

    assertThat(batches).hasSize(1);
    Collection<MetricData> firstBatch = batches.iterator().next();
    assertThat(firstBatch).hasSize(1); // There is only 1 MetricData

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.LONG_GAUGE);
    assertThat(m1.getName()).isEqualTo("name");
    assertThat(m1.getDescription()).isEqualTo("desc");
    assertThat(m1.getUnit()).isEqualTo("1");
    assertThat(m1.getLongGaugeData().getPoints()).containsExactly(p1, p2, p3);
  }

  @Test
  void batchMetrics_SplitsDoubleSum_SingleBatchCompletelyFilled() {
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
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 2);

    Collection<MetricData> firstBatch = batches.iterator().next();
    assertThat(firstBatch).hasSize(1); // There is only 1 MetricData

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.DOUBLE_SUM);
    assertThat(m1.getName()).isEqualTo("name");
    assertThat(m1.getDescription()).isEqualTo("desc");
    assertThat(m1.getUnit()).isEqualTo("1");
    assertThat(m1.getDoubleSumData().getPoints()).containsExactly(p1, p2);
    assertThat(m1.getDoubleSumData().isMonotonic()).isTrue();
    assertThat(m1.getDoubleSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void batchMetrics_SplitsLongSum_MultipleBatchesCompletelyFilled_MultipleMetrics() {
    Attributes attrs1 = Attributes.builder().put("key1", "val1").build();
    Attributes attrs2 = Attributes.builder().put("key2", "val2").build();
    LongPointData p1 = ImmutableLongPointData.create(1, 2, attrs1, 1L);
    LongPointData p2 = ImmutableLongPointData.create(1, 2, attrs2, 2L);

    MetricData metric1 =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name_1",
            "desc_1",
            "1",
            ImmutableSumData.create(
                /* isMonotonic= */ false, AggregationTemporality.DELTA, Arrays.asList(p1, p2)));

    MetricData metric2 =
        ImmutableMetricData.createLongSum(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name_2",
            "desc_2",
            "1",
            ImmutableSumData.create(
                /* isMonotonic= */ false, AggregationTemporality.DELTA, Arrays.asList(p1, p2)));

    Collection<Collection<MetricData>> batches =
        MetricExportBatcher.batchMetrics(Arrays.asList(metric1, metric2), 1);

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
    assertThat(m1.getLongSumData().getPoints().iterator().next().getAttributes()).isEqualTo(attrs1);
    assertThat(m1.getLongSumData().isMonotonic()).isFalse();
    assertThat(m1.getLongSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);

    MetricData m2 = secondBatch.iterator().next();
    assertThat(m2.getType()).isEqualTo(MetricDataType.LONG_SUM);
    assertThat(m2.getName()).isEqualTo("name_1");
    assertThat(m2.getDescription()).isEqualTo("desc_1");
    assertThat(m2.getUnit()).isEqualTo("1");
    assertThat(m2.getLongSumData().getPoints()).containsExactly(p2);
    assertThat(m2.getLongSumData().getPoints().iterator().next().getAttributes()).isEqualTo(attrs2);
    assertThat(m2.getLongSumData().isMonotonic()).isFalse();
    assertThat(m2.getLongSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);

    MetricData m3 = thirdBatch.iterator().next();
    assertThat(m3.getType()).isEqualTo(MetricDataType.LONG_SUM);
    assertThat(m3.getName()).isEqualTo("name_2");
    assertThat(m3.getDescription()).isEqualTo("desc_2");
    assertThat(m3.getUnit()).isEqualTo("1");
    assertThat(m3.getLongSumData().getPoints()).containsExactly(p1);
    assertThat(m3.getLongSumData().getPoints().iterator().next().getAttributes()).isEqualTo(attrs1);
    assertThat(m3.getLongSumData().isMonotonic()).isFalse();
    assertThat(m3.getLongSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);

    MetricData m4 = fourthBatch.iterator().next();
    assertThat(m4.getType()).isEqualTo(MetricDataType.LONG_SUM);
    assertThat(m4.getName()).isEqualTo("name_2");
    assertThat(m4.getDescription()).isEqualTo("desc_2");
    assertThat(m4.getUnit()).isEqualTo("1");
    assertThat(m4.getLongSumData().getPoints()).containsExactly(p2);
    assertThat(m4.getLongSumData().getPoints().iterator().next().getAttributes()).isEqualTo(attrs2);
    assertThat(m4.getLongSumData().isMonotonic()).isFalse();
    assertThat(m4.getLongSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void batchMetrics_SplitsHistogram_MultipleBatchesCompletelyFilled_SingleMetric() {
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
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 1);

    assertThat(batches).hasSize(2);
    Collection<MetricData> firstBatch = batches.iterator().next();
    Collection<MetricData> secondBatch = batches.stream().skip(1).findFirst().get();
    assertThat(firstBatch).hasSize(1);
    assertThat(secondBatch).hasSize(1);

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.HISTOGRAM);
    assertThat(m1.getName()).isEqualTo("name");
    assertThat(m1.getDescription()).isEqualTo("desc");
    assertThat(m1.getUnit()).isEqualTo("1");
    assertThat(m1.getHistogramData().getPoints()).containsExactly(p1);
    assertThat(m1.getHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    MetricData m2 = secondBatch.iterator().next();
    assertThat(m2.getType()).isEqualTo(MetricDataType.HISTOGRAM);
    assertThat(m2.getName()).isEqualTo("name");
    assertThat(m2.getDescription()).isEqualTo("desc");
    assertThat(m2.getUnit()).isEqualTo("1");
    assertThat(m2.getHistogramData().getPoints()).containsExactly(p2);
    assertThat(m2.getHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void batchMetrics_MultipleMetricsExactCapacityMatch() {
    Attributes attrs1 = Attributes.builder().put("k", "v1").build();
    Attributes attrs2 = Attributes.builder().put("k", "v2").build();
    Attributes attrs3 = Attributes.builder().put("k", "v3").build();
    Attributes attrs4 = Attributes.builder().put("k", "v4").build();
    LongPointData p1 = ImmutableLongPointData.create(1, 2, attrs1, 1L);
    LongPointData p2 = ImmutableLongPointData.create(1, 2, attrs2, 2L);
    LongPointData p3 = ImmutableLongPointData.create(1, 2, attrs3, 3L);
    LongPointData p4 = ImmutableLongPointData.create(1, 2, attrs4, 4L);

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

    Collection<Collection<MetricData>> batches =
        MetricExportBatcher.batchMetrics(Arrays.asList(m1, m2), 4);
    assertThat(batches).hasSize(1);
    Collection<MetricData> firstBatch = batches.iterator().next();
    assertThat(firstBatch).containsExactly(m1, m2);

    MetricData res1 = firstBatch.iterator().next();
    MetricData res2 = firstBatch.stream().skip(1).findFirst().get();

    assertThat(res1.getName()).isEqualTo("name_1");
    assertThat(res1.getLongGaugeData().getPoints()).containsExactly(p1, p2);
    assertThat(res2.getName()).isEqualTo("name_2");
    assertThat(res2.getLongGaugeData().getPoints()).containsExactly(p3, p4);
  }

  @Test
  void batchMetrics_SplitsLongGauge_MultipleMetrics_ExceedsCapacity() {
    LongPointData p1 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 1L);
    LongPointData p2 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 2L);
    LongPointData p3 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 3L);
    LongPointData p4 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 4L);
    LongPointData p5 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 5L);
    LongPointData p6 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 6L);

    MetricData m1 =
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name_1",
            "desc",
            "1",
            ImmutableGaugeData.create(Arrays.asList(p1, p2, p3)));
    MetricData m2 =
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name_2",
            "desc",
            "1",
            ImmutableGaugeData.create(Arrays.asList(p4, p5, p6)));

    Collection<Collection<MetricData>> batches =
        MetricExportBatcher.batchMetrics(Arrays.asList(m1, m2), 4);

    assertThat(batches).hasSize(2);

    Collection<MetricData> firstBatch = batches.iterator().next();
    assertThat(firstBatch).hasSize(2);
    MetricData b1m1 = firstBatch.iterator().next();
    MetricData b1m2 = firstBatch.stream().skip(1).findFirst().get();
    assertThat(b1m1.getName()).isEqualTo("name_1");
    assertThat(b1m1.getDescription()).isEqualTo("desc");
    assertThat(b1m1.getUnit()).isEqualTo("1");
    assertThat(b1m1.getLongGaugeData().getPoints()).containsExactly(p1, p2, p3);

    assertThat(b1m2.getName()).isEqualTo("name_2");
    assertThat(b1m2.getDescription()).isEqualTo("desc");
    assertThat(b1m2.getUnit()).isEqualTo("1");
    assertThat(b1m2.getLongGaugeData().getPoints()).containsExactly(p4);

    Collection<MetricData> secondBatch = batches.stream().skip(1).findFirst().get();
    assertThat(secondBatch).hasSize(1);
    MetricData b2m1 = secondBatch.iterator().next();
    assertThat(b2m1.getName()).isEqualTo("name_2");
    assertThat(b2m1.getDescription()).isEqualTo("desc");
    assertThat(b2m1.getUnit()).isEqualTo("1");
    assertThat(b2m1.getLongGaugeData().getPoints()).containsExactly(p5, p6);
  }

  @Test
  void batchMetrics_SplitsLongGauge_MultipleMetrics_PerfectFillThenSplit() {
    // m1 fills the batch completely (remaining capacity becomes 0).
    // m2 has 3 points, which forces it to split from the start of a fully-exhausted
    // previous pass.
    // This test case fails if there is an empty batch
    LongPointData p1 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 1L);
    LongPointData p2 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 2L);
    LongPointData p3 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 3L);
    LongPointData p4 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 4L);
    LongPointData p5 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 5L);

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
            ImmutableGaugeData.create(Arrays.asList(p3, p4, p5)));

    Collection<Collection<MetricData>> batches =
        MetricExportBatcher.batchMetrics(Arrays.asList(m1, m2), 2);

    assertThat(batches).hasSize(3);

    // Batch 1 should contain exactly m1 (p1, p2)
    Collection<MetricData> firstBatch = batches.iterator().next();
    assertThat(firstBatch).hasSize(1);
    MetricData b1m1 = firstBatch.iterator().next();
    assertThat(b1m1.getName()).isEqualTo("name_1");
    assertThat(b1m1.getLongGaugeData().getPoints()).containsExactly(p1, p2);

    // Batch 2 should contain the first part of m2 (p3, p4)
    Collection<MetricData> secondBatch = batches.stream().skip(1).findFirst().get();
    assertThat(secondBatch).hasSize(1);
    MetricData b2m1 = secondBatch.iterator().next();
    assertThat(b2m1.getName()).isEqualTo("name_2");
    assertThat(b2m1.getLongGaugeData().getPoints()).containsExactly(p3, p4);

    // Batch 3 should contain the rest of m2 (p5)
    Collection<MetricData> thirdBatch = batches.stream().skip(2).findFirst().get();
    assertThat(thirdBatch).hasSize(1);
    MetricData b3m1 = thirdBatch.iterator().next();
    assertThat(b3m1.getName()).isEqualTo("name_2");
    assertThat(b3m1.getLongGaugeData().getPoints()).containsExactly(p5);
  }

  @Test
  void batchMetrics_SplitsExponentialHistogram_MultipleBatchesCompletelyFilled_SingleMetric() {
    ExponentialHistogramBuckets buckets =
        ImmutableExponentialHistogramBuckets.create(
            /* scale= */ 20, /* offset= */ 0, /* bucketCounts= */ Collections.singletonList(1L));
    ExponentialHistogramPointData p1 =
        ImmutableExponentialHistogramPointData.create(
            /* scale= */ 20,
            /* sum= */ 1.0,
            /* zeroCount= */ 0,
            /* hasMin= */ false,
            /* min= */ 0.0,
            /* hasMax= */ false,
            /* max= */ 0.0,
            /* positiveBuckets= */ buckets,
            /* negativeBuckets= */ buckets,
            /* startEpochNanos= */ 1,
            /* epochNanos= */ 2,
            /* attributes= */ Attributes.empty(),
            /* exemplars= */ Collections.emptyList());
    ExponentialHistogramPointData p2 =
        ImmutableExponentialHistogramPointData.create(
            /* scale= */ 20,
            /* sum= */ 2.0,
            /* zeroCount= */ 0,
            /* hasMin= */ false,
            /* min= */ 0.0,
            /* hasMax= */ false,
            /* max= */ 0.0,
            /* positiveBuckets= */ buckets,
            /* negativeBuckets= */ buckets,
            /* startEpochNanos= */ 1,
            /* epochNanos= */ 2,
            /* attributes= */ Attributes.empty(),
            /* exemplars= */ Collections.emptyList());

    MetricData metric =
        ImmutableMetricData.createExponentialHistogram(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableExponentialHistogramData.create(
                AggregationTemporality.CUMULATIVE, Arrays.asList(p1, p2)));

    Collection<Collection<MetricData>> batches =
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 1);

    assertThat(batches).hasSize(2);
    Collection<MetricData> firstBatch = batches.iterator().next();
    Collection<MetricData> secondBatch = batches.stream().skip(1).findFirst().get();
    assertThat(firstBatch).hasSize(1);
    assertThat(secondBatch).hasSize(1);

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.EXPONENTIAL_HISTOGRAM);
    assertThat(m1.getName()).isEqualTo("name");
    assertThat(m1.getDescription()).isEqualTo("desc");
    assertThat(m1.getUnit()).isEqualTo("1");
    assertThat(m1.getExponentialHistogramData().getPoints()).containsExactly(p1);
    assertThat(m1.getExponentialHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);

    MetricData m2 = secondBatch.iterator().next();
    assertThat(m2.getType()).isEqualTo(MetricDataType.EXPONENTIAL_HISTOGRAM);
    assertThat(m2.getName()).isEqualTo("name");
    assertThat(m2.getDescription()).isEqualTo("desc");
    assertThat(m2.getUnit()).isEqualTo("1");
    assertThat(m2.getExponentialHistogramData().getPoints()).containsExactly(p2);
    assertThat(m2.getExponentialHistogramData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void batchMetrics_SplitsSummary_MultipleBatchesCompletelyFilled_SingleMetric() {
    SummaryPointData p1 =
        ImmutableSummaryPointData.create(
            /* startEpochNanos= */ 1,
            /* epochNanos= */ 2,
            /* attributes= */ Attributes.empty(),
            /* count= */ 1,
            /* sum= */ 1.0,
            /* percentileValues= */ Collections.singletonList(
                ImmutableValueAtQuantile.create(0.5, 1.0)));
    SummaryPointData p2 =
        ImmutableSummaryPointData.create(
            /* startEpochNanos= */ 1,
            /* epochNanos= */ 2,
            /* attributes= */ Attributes.empty(),
            /* count= */ 1,
            /* sum= */ 2.0,
            /* percentileValues= */ Collections.singletonList(
                ImmutableValueAtQuantile.create(0.5, 2.0)));

    MetricData metric =
        ImmutableMetricData.createDoubleSummary(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableSummaryData.create(Arrays.asList(p1, p2)));

    Collection<Collection<MetricData>> batches =
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 1);

    assertThat(batches).hasSize(2);
    Collection<MetricData> firstBatch = batches.iterator().next();
    Collection<MetricData> secondBatch = batches.stream().skip(1).findFirst().get();
    assertThat(firstBatch).hasSize(1);
    assertThat(secondBatch).hasSize(1);

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.SUMMARY);
    assertThat(m1.getName()).isEqualTo("name");
    assertThat(m1.getDescription()).isEqualTo("desc");
    assertThat(m1.getUnit()).isEqualTo("1");
    assertThat(m1.getSummaryData().getPoints()).containsExactly(p1);

    MetricData m2 = secondBatch.iterator().next();
    assertThat(m2.getType()).isEqualTo(MetricDataType.SUMMARY);
    assertThat(m2.getName()).isEqualTo("name");
    assertThat(m2.getDescription()).isEqualTo("desc");
    assertThat(m2.getUnit()).isEqualTo("1");
    assertThat(m2.getSummaryData().getPoints()).containsExactly(p2);
  }

  @Test
  void batchMetrics_SplitsLongGauge_MultipleBatches() {
    LongPointData p1 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 1L);
    LongPointData p2 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 2L);
    LongPointData p3 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 3L);
    LongPointData p4 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 4L);
    LongPointData p5 = ImmutableLongPointData.create(1, 2, Attributes.empty(), 5L);

    MetricData metric =
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableGaugeData.create(Arrays.asList(p1, p2, p3, p4, p5)));

    Collection<Collection<MetricData>> batches =
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 2);
    List<Collection<MetricData>> batchesList = new ArrayList<>(batches);

    assertThat(batchesList).hasSize(3);
    Collection<MetricData> firstBatch = batchesList.get(0);
    Collection<MetricData> secondBatch = batchesList.get(1);
    Collection<MetricData> thirdBatch = batchesList.get(2);

    assertThat(firstBatch).hasSize(1);
    assertThat(secondBatch).hasSize(1);
    assertThat(thirdBatch).hasSize(1);

    MetricData firstBatchMetricData = firstBatch.iterator().next();
    assertThat(firstBatchMetricData.getType()).isEqualTo(MetricDataType.LONG_GAUGE);
    assertThat(firstBatchMetricData.getName()).isEqualTo("name");
    assertThat(firstBatchMetricData.getDescription()).isEqualTo("desc");
    assertThat(firstBatchMetricData.getUnit()).isEqualTo("1");
    assertThat(firstBatchMetricData.getLongGaugeData().getPoints()).containsExactly(p1, p2);

    MetricData secondBatchMetricData = secondBatch.iterator().next();
    assertThat(secondBatchMetricData.getType()).isEqualTo(MetricDataType.LONG_GAUGE);
    assertThat(secondBatchMetricData.getName()).isEqualTo("name");
    assertThat(secondBatchMetricData.getDescription()).isEqualTo("desc");
    assertThat(secondBatchMetricData.getUnit()).isEqualTo("1");
    assertThat(secondBatchMetricData.getLongGaugeData().getPoints()).containsExactly(p3, p4);

    MetricData thirdBatchMetricData = thirdBatch.iterator().next();
    assertThat(thirdBatchMetricData.getType()).isEqualTo(MetricDataType.LONG_GAUGE);
    assertThat(thirdBatchMetricData.getName()).isEqualTo("name");
    assertThat(thirdBatchMetricData.getDescription()).isEqualTo("desc");
    assertThat(thirdBatchMetricData.getUnit()).isEqualTo("1");
    assertThat(thirdBatchMetricData.getLongGaugeData().getPoints()).containsExactly(p5);
  }

  @Test
  void batchMetrics_SplitsDoubleSum_MultipleBatches() {
    DoublePointData p1 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 1.0);
    DoublePointData p2 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 2.0);
    DoublePointData p3 = ImmutableDoublePointData.create(1, 2, Attributes.empty(), 3.0);

    MetricData metric =
        ImmutableMetricData.createDoubleSum(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                Arrays.asList(p1, p2, p3)));

    Collection<Collection<MetricData>> batches =
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 1);
    List<Collection<MetricData>> batchesList = new ArrayList<>(batches);

    assertThat(batchesList).hasSize(3);
    Collection<MetricData> firstBatch = batchesList.get(0);
    Collection<MetricData> secondBatch = batchesList.get(1);
    Collection<MetricData> thirdBatch = batchesList.get(2);

    assertThat(firstBatch).hasSize(1);
    assertThat(secondBatch).hasSize(1);
    assertThat(thirdBatch).hasSize(1);

    MetricData m1 = firstBatch.iterator().next();
    assertThat(m1.getType()).isEqualTo(MetricDataType.DOUBLE_SUM);
    assertThat(m1.getName()).isEqualTo("name");
    assertThat(m1.getDescription()).isEqualTo("desc");
    assertThat(m1.getUnit()).isEqualTo("1");
    assertThat(m1.getDoubleSumData().getPoints()).containsExactly(p1);
    assertThat(m1.getDoubleSumData().isMonotonic()).isTrue();
    assertThat(m1.getDoubleSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);

    MetricData m2 = secondBatch.iterator().next();
    assertThat(m2.getType()).isEqualTo(MetricDataType.DOUBLE_SUM);
    assertThat(m2.getName()).isEqualTo("name");
    assertThat(m2.getDescription()).isEqualTo("desc");
    assertThat(m2.getUnit()).isEqualTo("1");
    assertThat(m2.getDoubleSumData().getPoints()).containsExactly(p2);
    assertThat(m2.getDoubleSumData().isMonotonic()).isTrue();
    assertThat(m2.getDoubleSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);

    MetricData m3 = thirdBatch.iterator().next();
    assertThat(m3.getType()).isEqualTo(MetricDataType.DOUBLE_SUM);
    assertThat(m3.getName()).isEqualTo("name");
    assertThat(m3.getDescription()).isEqualTo("desc");
    assertThat(m3.getUnit()).isEqualTo("1");
    assertThat(m3.getDoubleSumData().getPoints()).containsExactly(p3);
    assertThat(m3.getDoubleSumData().isMonotonic()).isTrue();
    assertThat(m3.getDoubleSumData().getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void batchMetrics_EmptyPointsInMetricData() {
    MetricData metric =
        ImmutableMetricData.createLongGauge(
            Resource.empty(),
            InstrumentationScopeInfo.empty(),
            "name",
            "desc",
            "1",
            ImmutableGaugeData.create(Collections.emptyList()));

    Collection<Collection<MetricData>> batches =
        MetricExportBatcher.batchMetrics(Collections.singletonList(metric), 2);
    assertThat(batches).hasSize(1);
    assertThat(batches.iterator().next()).containsExactly(metric);
  }
}
