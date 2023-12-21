/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Aggregation;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Base2ExponentialBucketHistogram;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Drop;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogram;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LastValue;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AggregationFactoryTest {

  @Test
  void create_Null() {
    assertThat(
            AggregationFactory.getInstance()
                .create(null, mock(SpiHelper.class), Collections.emptyList())
                .toString())
        .isEqualTo(io.opentelemetry.sdk.metrics.Aggregation.defaultAggregation().toString());
  }

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(Aggregation model, io.opentelemetry.sdk.metrics.Aggregation expectedResult) {
    io.opentelemetry.sdk.metrics.Aggregation aggregation =
        AggregationFactory.getInstance().create(model, mock(SpiHelper.class), new ArrayList<>());
    assertThat(aggregation.toString()).isEqualTo(expectedResult.toString());
  }

  private static Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.of(
            new Aggregation(), io.opentelemetry.sdk.metrics.Aggregation.defaultAggregation()),
        Arguments.of(
            new Aggregation().withDrop(new Drop()),
            io.opentelemetry.sdk.metrics.Aggregation.drop()),
        Arguments.of(
            new Aggregation().withSum(new Sum()), io.opentelemetry.sdk.metrics.Aggregation.sum()),
        Arguments.of(
            new Aggregation().withLastValue(new LastValue()),
            io.opentelemetry.sdk.metrics.Aggregation.lastValue()),
        Arguments.of(
            new Aggregation()
                .withBase2ExponentialBucketHistogram(new Base2ExponentialBucketHistogram()),
            io.opentelemetry.sdk.metrics.Aggregation.base2ExponentialBucketHistogram()),
        Arguments.of(
            new Aggregation()
                .withBase2ExponentialBucketHistogram(
                    new Base2ExponentialBucketHistogram().withMaxSize(2).withMaxScale(2)),
            io.opentelemetry.sdk.metrics.Aggregation.base2ExponentialBucketHistogram(2, 2)),
        Arguments.of(
            new Aggregation()
                .withExplicitBucketHistogram(new ExplicitBucketHistogram().withBoundaries(null)),
            io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram()),
        Arguments.of(
            new Aggregation()
                .withExplicitBucketHistogram(
                    new ExplicitBucketHistogram().withBoundaries(Arrays.asList(1.0, 2.0))),
            io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram(
                Arrays.asList(1.0, 2.0))));
  }
}
