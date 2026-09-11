/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExplicitBucketHistogramAggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IncludeExcludeModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel;
import io.opentelemetry.sdk.common.internal.IncludeExcludePredicate;
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
        Arguments.argumentSet(
            "defaults", new ViewStreamModel().setAttributeKeys(null), View.builder().build()),
        // https://github.com/open-telemetry/opentelemetry-java/issues/8337
        Arguments.argumentSet(
            "included only no excluded",
            new ViewStreamModel()
                .setAttributeKeys(
                    new IncludeExcludeModel()
                        .setIncluded(
                            Arrays.asList(
                                "url.full", "http.request.method", "http.response.status_code"))),
            View.builder()
                .setAttributeFilter(
                    IncludeExcludePredicate.createPatternMatching(
                        Arrays.asList(
                            "url.full", "http.request.method", "http.response.status_code"),
                        null))
                .build()),
        Arguments.argumentSet(
            "full configuration",
            new ViewStreamModel()
                .setName("name")
                .setDescription("description")
                .setAttributeKeys(
                    new IncludeExcludeModel()
                        .setIncluded(Arrays.asList("foo", "bar"))
                        .setExcluded(Collections.singletonList("baz")))
                .setAggregation(
                    new AggregationModel()
                        .setExplicitBucketHistogram(
                            new ExplicitBucketHistogramAggregationModel()
                                .setBoundaries(Arrays.asList(1.0, 2.0)))),
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
