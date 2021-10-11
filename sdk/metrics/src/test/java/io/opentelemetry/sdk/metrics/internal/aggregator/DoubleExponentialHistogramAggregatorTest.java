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
          ExemplarReservoir::noSamples);

  private static int valueToIndex(int scale, double value) {
    double scaleFactor = Math.scalb(1D / Math.log(2), scale);
    return (int) Math.floor(Math.log(value) * scaleFactor);
  }


  private static ExponentialHistogramAccumulation getTestAccumulation(List<ExemplarData> exemplars, double... recordings) {
    AggregatorHandle<ExponentialHistogramAccumulation> aggregatorHandle = aggregator.createHandle();
    for (double r : recordings) {
      aggregatorHandle.recordDouble(r);
    }
    return aggregatorHandle.doAccumulateThenReset(exemplars);
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
    aggregatorHandle.recordDouble(12.0);
    aggregatorHandle.recordDouble(-13.2);
    aggregatorHandle.recordDouble(-2.01);
    aggregatorHandle.recordDouble(-1);
    aggregatorHandle.recordDouble(0.0);
    aggregatorHandle.recordLong(0);

    ExponentialHistogramAccumulation acc = aggregatorHandle.accumulateThenReset(Attributes.empty());
    List<Long> positiveCounts = Objects.requireNonNull(acc).getPositiveBuckets().getBucketCounts();
    List<Long> negativeCounts = acc.getNegativeBuckets().getBucketCounts();
    int expectedScale = 6;

    assertThat(acc.getScale()).isEqualTo(expectedScale);
    assertThat(acc.getZeroCount()).isEqualTo(2);

    // Assert positive recordings are at correct index
    int posOffset = acc.getPositiveBuckets().getOffset();
    assertThat(acc.getPositiveBuckets().getTotalCount()).isEqualTo(5);
    assertThat(
        positiveCounts.get(valueToIndex(expectedScale, 0.5) - posOffset))
        .isEqualTo(1);
    assertThat(
        positiveCounts.get(valueToIndex(expectedScale, 1.0) - posOffset))
        .isEqualTo(1);
    assertThat(
        positiveCounts.get(valueToIndex(expectedScale, 12.0) - posOffset))
        .isEqualTo(2);
    assertThat(
        positiveCounts.get(valueToIndex(expectedScale, 15.213) - posOffset))
        .isEqualTo(1);

    // Assert negative recordings are at correct index
    int negOffset = acc.getNegativeBuckets().getOffset();
    assertThat(acc.getNegativeBuckets().getTotalCount()).isEqualTo(3);
    assertThat(
        negativeCounts.get(valueToIndex(expectedScale, 13.2) - negOffset))
        .isEqualTo(1);
    assertThat(
        negativeCounts.get(valueToIndex(expectedScale, 2.01) - negOffset))
        .isEqualTo(1);
    assertThat(
        negativeCounts.get(valueToIndex(expectedScale, 1.0) - negOffset))
        .isEqualTo(1);
  }

  // todo test boundaries of 32 bit index and double value;

  @Test
  void testExemplarsInAccumulation() {
    DoubleExponentialHistogramAggregator agg =
        new DoubleExponentialHistogramAggregator(
            Resource.getDefault(),
            InstrumentationLibraryInfo.empty(),
            MetricDescriptor.create("name", "description", "unit"),
            false,
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
        getTestAccumulation(Collections.emptyList(), 1.2);
    assertThat(acc).isEqualTo(expected);
  }

//  @Test
//  void testMergeAccumulation() {
//    Attributes attributes = Attributes.builder().put("test", "value").build();
//    ExemplarData exemplar = DoubleExemplarData.create(attributes, 2L, "spanid", "traceid", 1);
//    List<ExemplarData> exemplars = Collections.singletonList(exemplar);
//    List<ExemplarData> previousExemplars =
//        Collections.singletonList(
//            DoubleExemplarData.create(attributes, 1L, "spanId", "traceId", 2));
//    ExponentialHistogramAccumulation previousAccumulation =
//        getTestAccumulation( previousExemplars, 0, 4.1);
//    ExponentialHistogramAccumulation nextAccumulation =
//        getTestAccumulation( exemplars, -8.2, 2.3);
//
//    // Merged accumulations should equal accumulation with equivalent recordings, and latest
//    // exemplars.
//    assertThat(aggregator.merge(previousAccumulation, nextAccumulation))
//        .isEqualTo(getTestAccumulation(exemplars, 0, 4.1, -8.2, 2.3));
//  }

  @Test
  void testDownScale() {
    // todo
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
