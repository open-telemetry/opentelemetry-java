/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AggregationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogramModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.StreamModel;
import io.opentelemetry.sdk.metrics.View;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class ViewFactoryTest {

  @Test
  void create_Defaults() {
    View expectedView = View.builder().build();

    View view =
        ViewFactory.getInstance()
            .create(
                new StreamModel().withAttributeKeys(null),
                mock(SpiHelper.class),
                Collections.emptyList());

    assertThat(view.toString()).isEqualTo(expectedView.toString());
  }

  @Test
  void create() {
    View expectedView =
        View.builder()
            .setName("name")
            .setDescription("description")
            .setAttributeFilter(new HashSet<>(Arrays.asList("foo", "bar")))
            .setAggregation(
                io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram(
                    Arrays.asList(1.0, 2.0)))
            .build();

    View view =
        ViewFactory.getInstance()
            .create(
                new StreamModel()
                    .withName("name")
                    .withDescription("description")
                    .withAttributeKeys(
                        new IncludeExcludeModel().withIncluded(Arrays.asList("foo", "bar")))
                    .withAggregation(
                        new AggregationModel()
                            .withExplicitBucketHistogram(
                                new ExplicitBucketHistogramModel()
                                    .withBoundaries(Arrays.asList(1.0, 2.0)))),
                mock(SpiHelper.class),
                Collections.emptyList());

    assertThat(view.toString()).isEqualTo(expectedView.toString());
  }
}
