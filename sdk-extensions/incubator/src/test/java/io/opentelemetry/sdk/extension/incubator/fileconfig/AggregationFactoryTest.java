/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Base2ExponentialBucketHistogramAggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.DropAggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogramAggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LastValueAggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SumAggregationModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AggregationFactoryTest {

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(AggregationModel model, io.opentelemetry.sdk.metrics.Aggregation expectedResult) {
    io.opentelemetry.sdk.metrics.Aggregation aggregation =
        AggregationFactory.getInstance().create(model, mock(SpiHelper.class), new ArrayList<>());
    assertThat(aggregation.toString()).isEqualTo(expectedResult.toString());
  }

  private static Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.of(
            new AggregationModel(), io.opentelemetry.sdk.metrics.Aggregation.defaultAggregation()),
        Arguments.of(
            new AggregationModel().withDrop(new DropAggregationModel()),
            io.opentelemetry.sdk.metrics.Aggregation.drop()),
        Arguments.of(
            new AggregationModel().withSum(new SumAggregationModel()),
            io.opentelemetry.sdk.metrics.Aggregation.sum()),
        Arguments.of(
            new AggregationModel().withLastValue(new LastValueAggregationModel()),
            io.opentelemetry.sdk.metrics.Aggregation.lastValue()),
        Arguments.of(
            new AggregationModel()
                .withBase2ExponentialBucketHistogram(
                    new Base2ExponentialBucketHistogramAggregationModel()),
            io.opentelemetry.sdk.metrics.Aggregation.base2ExponentialBucketHistogram()),
        Arguments.of(
            new AggregationModel()
                .withBase2ExponentialBucketHistogram(
                    new Base2ExponentialBucketHistogramAggregationModel()
                        .withMaxSize(2)
                        .withMaxScale(2)),
            io.opentelemetry.sdk.metrics.Aggregation.base2ExponentialBucketHistogram(2, 2)),
        Arguments.of(
            new AggregationModel()
                .withExplicitBucketHistogram(
                    new ExplicitBucketHistogramAggregationModel().withBoundaries(null)),
            io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram()),
        Arguments.of(
            new AggregationModel()
                .withExplicitBucketHistogram(
                    new ExplicitBucketHistogramAggregationModel()
                        .withBoundaries(Arrays.asList(1.0, 2.0))),
            io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram(
                Arrays.asList(1.0, 2.0))));
  }
}
