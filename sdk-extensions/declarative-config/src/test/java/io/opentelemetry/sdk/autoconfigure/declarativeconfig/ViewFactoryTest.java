/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.common.internal.IncludeExcludePredicate;
import io.opentelemetry.sdk.declarativeconfig.internal.model.AggregationModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ExplicitBucketHistogramAggregationModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.declarativeconfig.internal.model.ViewStreamModel;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.ExplicitBucketHistogramOptions;
import io.opentelemetry.sdk.metrics.View;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ViewFactoryTest {

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(ViewStreamModel model, View expectedView) {
    View view = ViewFactory.getInstance().create(model, mock(DeclarativeConfigContext.class));
    assertThat(view.toString()).isEqualTo(expectedView.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        // defaults
        Arguments.of(new ViewStreamModel().withAttributeKeys(null), View.builder().build()),
        // attribute_keys with only included (no excluded) - reproduces
        // https://github.com/open-telemetry/opentelemetry-java/issues/8337
        Arguments.of(
            new ViewStreamModel()
                .withAttributeKeys(
                    new IncludeExcludeModel()
                        .withIncluded(
                            Arrays.asList(
                                "url.full", "http.request.method", "http.response.status_code"))),
            View.builder()
                .setAttributeFilter(
                    IncludeExcludePredicate.createPatternMatching(
                        Arrays.asList(
                            "url.full", "http.request.method", "http.response.status_code"),
                        null))
                .build()),
        // full configuration
        Arguments.of(
            new ViewStreamModel()
                .withName("name")
                .withDescription("description")
                .withAttributeKeys(
                    new IncludeExcludeModel()
                        .withIncluded(Arrays.asList("foo", "bar"))
                        .withExcluded(Collections.singletonList("baz")))
                .withAggregation(
                    new AggregationModel()
                        .withExplicitBucketHistogram(
                            new ExplicitBucketHistogramAggregationModel()
                                .withBoundaries(Arrays.asList(1.0, 2.0)))),
            View.builder()
                .setName("name")
                .setDescription("description")
                .setAttributeFilter(
                    IncludeExcludePredicate.createPatternMatching(
                        Arrays.asList("foo", "bar"), Collections.singletonList("baz")))
                .setAggregation(
                    Aggregation.explicitBucketHistogram(
                        ExplicitBucketHistogramOptions.builder()
                            .setBucketBoundaries(Arrays.asList(1.0, 2.0))
                            .build()))
                .build()));
  }
}
