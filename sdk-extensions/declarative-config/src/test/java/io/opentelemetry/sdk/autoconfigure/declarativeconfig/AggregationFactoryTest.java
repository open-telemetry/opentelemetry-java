/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.Base2ExponentialBucketHistogramAggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.DropAggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExplicitBucketHistogramAggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LastValueAggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SumAggregationModel;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.Base2ExponentialHistogramOptions;
import io.opentelemetry.sdk.metrics.ExplicitBucketHistogramOptions;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AggregationFactoryTest {

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(AggregationModel model, Aggregation expectedResult) {
    Aggregation aggregation =
        AggregationFactory.getInstance().create(model, mock(DeclarativeConfigContext.class));
    assertThat(aggregation.toString()).isEqualTo(expectedResult.toString());
  }

  private static Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.argumentSet("default", new AggregationModel(), Aggregation.defaultAggregation()),
        Arguments.argumentSet(
            "drop",
            new AggregationModel().withDrop(new DropAggregationModel()),
            Aggregation.drop()),
        Arguments.argumentSet(
            "sum", new AggregationModel().withSum(new SumAggregationModel()), Aggregation.sum()),
        Arguments.argumentSet(
            "last_Value",
            new AggregationModel().withLastValue(new LastValueAggregationModel()),
            Aggregation.lastValue()),
        Arguments.argumentSet(
            "base2_exponential_bucket_histogram defaults",
            new AggregationModel()
                .withBase2ExponentialBucketHistogram(
                    new Base2ExponentialBucketHistogramAggregationModel()),
            Aggregation.base2ExponentialBucketHistogram()),
        Arguments.argumentSet(
            "base2_exponential_bucket_histogram with options",
            new AggregationModel()
                .withBase2ExponentialBucketHistogram(
                    new Base2ExponentialBucketHistogramAggregationModel()
                        .withMaxSize(2)
                        .withMaxScale(2)),
            Aggregation.base2ExponentialBucketHistogram(
                Base2ExponentialHistogramOptions.builder()
                    .setMaxBuckets(2)
                    .setMaxScale(2)
                    .build())),
        Arguments.argumentSet(
            "explicit_bucket_histogram null boundaries",
            new AggregationModel()
                .withExplicitBucketHistogram(
                    new ExplicitBucketHistogramAggregationModel().withBoundaries(null)),
            Aggregation.explicitBucketHistogram()),
        Arguments.argumentSet(
            "explicit_bucket_histogram with boundaries",
            new AggregationModel()
                .withExplicitBucketHistogram(
                    new ExplicitBucketHistogramAggregationModel()
                        .withBoundaries(Arrays.asList(1.0, 2.0))),
            Aggregation.explicitBucketHistogram(
                ExplicitBucketHistogramOptions.builder()
                    .setBucketBoundaries(Arrays.asList(1.0, 2.0))
                    .build())),
        Arguments.argumentSet(
            "explicit_bucket_histogram record_min_max true",
            new AggregationModel()
                .withExplicitBucketHistogram(
                    new ExplicitBucketHistogramAggregationModel()
                        .withBoundaries(Arrays.asList(1.0, 2.0))
                        .withRecordMinMax(true)),
            Aggregation.explicitBucketHistogram(
                ExplicitBucketHistogramOptions.builder()
                    .setBucketBoundaries(Arrays.asList(1.0, 2.0))
                    .setRecordMinMax(true)
                    .build())),
        Arguments.argumentSet(
            "explicit_bucket_histogram record_min_max false",
            new AggregationModel()
                .withExplicitBucketHistogram(
                    new ExplicitBucketHistogramAggregationModel()
                        .withBoundaries(Arrays.asList(1.0, 2.0))
                        .withRecordMinMax(false)),
            Aggregation.explicitBucketHistogram(
                ExplicitBucketHistogramOptions.builder()
                    .setBucketBoundaries(Arrays.asList(1.0, 2.0))
                    .setRecordMinMax(false)
                    .build())),
        Arguments.argumentSet(
            "explicit_bucket_histogram null boundaries record_min_max false",
            new AggregationModel()
                .withExplicitBucketHistogram(
                    new ExplicitBucketHistogramAggregationModel()
                        .withBoundaries(null)
                        .withRecordMinMax(false)),
            Aggregation.explicitBucketHistogram(
                ExplicitBucketHistogramOptions.builder().setRecordMinMax(false).build())),
        Arguments.argumentSet(
            "base2_exponential_bucket_histogram record_min_max true",
            new AggregationModel()
                .withBase2ExponentialBucketHistogram(
                    new Base2ExponentialBucketHistogramAggregationModel()
                        .withMaxSize(2)
                        .withMaxScale(2)
                        .withRecordMinMax(true)),
            Aggregation.base2ExponentialBucketHistogram(
                Base2ExponentialHistogramOptions.builder()
                    .setMaxBuckets(2)
                    .setMaxScale(2)
                    .setRecordMinMax(true)
                    .build())),
        Arguments.argumentSet(
            "base2_exponential_bucket_histogram record_min_max false",
            new AggregationModel()
                .withBase2ExponentialBucketHistogram(
                    new Base2ExponentialBucketHistogramAggregationModel()
                        .withMaxSize(2)
                        .withMaxScale(2)
                        .withRecordMinMax(false)),
            Aggregation.base2ExponentialBucketHistogram(
                Base2ExponentialHistogramOptions.builder()
                    .setMaxBuckets(2)
                    .setMaxScale(2)
                    .setRecordMinMax(false)
                    .build())));
  }
}
