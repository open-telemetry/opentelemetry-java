/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DoubleExponentialHistogramAggregatorTest {

  @Mock ExemplarReservoir reservoir;

  private static final int SCALE = 0;

  private static final DoubleExponentialHistogramAggregator aggregator =
      new DoubleExponentialHistogramAggregator(
          Resource.getDefault(),
          InstrumentationLibraryInfo.empty(),
          MetricDescriptor.create("name", "description", "unit"),
          false,
          SCALE,
          ExemplarReservoir::noSamples);

  private static ExponentialHistogramAccumulation getTestAccumulation(
      double[] recordings, List<ExemplarData> exemplars) {
    DoubleExponentialHistogramBuckets pos = new DoubleExponentialHistogramBuckets(SCALE);
    DoubleExponentialHistogramBuckets neg = new DoubleExponentialHistogramBuckets(SCALE);
    long zeroCount = 0;
    double sum = 0;

    for (double r : recordings) {
      sum += r;
      int comparison = Double.compare(r, 0);
      if (comparison == 0) {
        zeroCount++;
      } else if (comparison < 0) {
        neg.record(r);
      } else {
        pos.record(r);
      }
    }
    return ExponentialHistogramAccumulation.create(SCALE, sum, pos, neg, zeroCount);
  }

  @Test
  void createHandle() {
    assertThat(aggregator.createHandle())
        .isInstanceOf(DoubleExponentialHistogramAggregator.Handle.class);
  }

  @Test
  void testRecordings() {
    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    aggregatorHandle.recordDouble(0.5);
    aggregatorHandle.recordDouble(1.0);
    aggregatorHandle.recordDouble(12.0);
    aggregatorHandle.recordDouble(15.213);
    aggregatorHandle.recordDouble(-13.2);
    aggregatorHandle.recordDouble(-2.01);
    aggregatorHandle.recordDouble(-1);
    aggregatorHandle.recordDouble(0.0);
    aggregatorHandle.recordLong(0);

    ExponentialHistogramAccumulation acc = aggregatorHandle.accumulateThenReset(Attributes.empty());
    List<Long> positiveCounts = Objects.requireNonNull(acc).getPositiveBuckets().getBucketCounts();
    List<Long> negativeCounts = acc.getNegativeBuckets().getBucketCounts();

    assertThat(acc.getZeroCount()).isEqualTo(2);

    assertThat(positiveCounts).isEqualTo(Arrays.asList(1L, 1L, 0L, 0L, 2L));
    assertThat(acc.getPositiveBuckets().getOffset()).isEqualTo(-1);

    assertThat(negativeCounts).isEqualTo(Arrays.asList(1L, 1L, 0L, 1L));
    assertThat(acc.getNegativeBuckets().getOffset()).isEqualTo(0);
  }

  @Test
  void testExemplarsInAccumulation() {
    DoubleExponentialHistogramAggregator agg =
        new DoubleExponentialHistogramAggregator(
            Resource.getDefault(),
            InstrumentationLibraryInfo.empty(),
            MetricDescriptor.create("name", "description", "unit"),
            false,
            SCALE,
            () -> reservoir);

    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar = DoubleExemplarData.create(attributes, 2L, "spanid", "traceid", 1);
    List<ExemplarData> exemplars = Collections.singletonList(exemplar);
    Mockito.when(reservoir.collectAndReset(Attributes.empty())).thenReturn(exemplars);

    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle = agg.createHandle();
    aggregatorHandle.recordDouble(0, attributes, Context.root());

    assertThat(
            Objects.requireNonNull(aggregatorHandle.accumulateThenReset(Attributes.empty()))
                .getExemplars())
        .isEqualTo(exemplars);
  }

  @Test
  void testAccumulationAndReset() {
    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();

    aggregatorHandle.recordDouble(5.0);
    assertThat(
            Objects.requireNonNull(aggregatorHandle.accumulateThenReset(Attributes.empty()))
                .getPositiveBuckets()
                .getBucketCounts())
        .isEqualTo(Collections.singletonList(1L));
    assertThat(aggregatorHandle.accumulateThenReset(Attributes.empty())).isNull();
  }

  @Test
  void testAccumulateData() {
    ExponentialHistogramAccumulation acc = aggregator.accumulateDouble(1.2);
    ExponentialHistogramAccumulation expected =
        getTestAccumulation(new double[] {1.2}, Collections.emptyList());
    assertThat(acc).isEqualTo(expected);
  }

  @Test
  void testMergeAccumulation() {
    Attributes attributes = Attributes.builder().put("test", "value").build();
    ExemplarData exemplar = DoubleExemplarData.create(attributes, 2L, "spanid", "traceid", 1);
    List<ExemplarData> exemplars = Collections.singletonList(exemplar);
    List<ExemplarData> previousExemplars =
        Collections.singletonList(
            DoubleExemplarData.create(attributes, 1L, "spanId", "traceId", 2));
    ExponentialHistogramAccumulation previousAccumulation =
        getTestAccumulation(new double[] {0, 4.1}, previousExemplars);
    ExponentialHistogramAccumulation nextAccumulation =
        getTestAccumulation(new double[] {-8.2, 2.3}, exemplars);

    // Merged accumulations should equal accumulation with equivalent recordings, and latest
    // exemplars.
    assertThat(aggregator.merge(previousAccumulation, nextAccumulation))
        .isEqualTo(getTestAccumulation(new double[] {0, 4.1, -8.2, 2.3}, exemplars));
  }

  @Test
  void testToMetricData() {
    // todo
  }

  @Test
  void toMetricDataWithExemplars() {
    // todo
  }

  @Test
  void testHistogramCounts() {
    // todo
  }

  @Test
  void testMultithreadedUpdates() throws InterruptedException {
    // todo
  }

  @Test
  void testScaleChange() {
    // todo
  }
}
