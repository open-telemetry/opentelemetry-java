/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.common.internal.IncludeExcludePredicate;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogramAggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewStreamModel;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.View;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ViewFactoryTest {

  @Test
  void create_Defaults() {
    View expectedView = View.builder().build();

    View view =
        ViewFactory.getInstance()
            .create(
                new ViewStreamModel().withAttributeKeys(null),
                mock(DeclarativeConfigContext.class));

    assertThat(view.toString()).isEqualTo(expectedView.toString());
  }

  @Test
  void create() {
    View expectedView =
        View.builder()
            .setName("name")
            .setDescription("description")
            .setAttributeFilter(
                IncludeExcludePredicate.createExactMatching(
                    Arrays.asList("foo", "bar"), Collections.singletonList("baz")))
            .setAggregation(Aggregation.explicitBucketHistogram(Arrays.asList(1.0, 2.0)))
            .build();

    View view =
        ViewFactory.getInstance()
            .create(
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
                mock(DeclarativeConfigContext.class));

    assertThat(view.toString()).isEqualTo(expectedView.toString());
  }
}
