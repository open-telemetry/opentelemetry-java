/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Aggregation;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogram;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Stream;
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
                new Stream().withAttributeKeys(null),
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
                new Stream()
                    .withName("name")
                    .withDescription("description")
                    .withAttributeKeys(Arrays.asList("foo", "bar"))
                    .withAggregation(
                        new Aggregation()
                            .withExplicitBucketHistogram(
                                new ExplicitBucketHistogram()
                                    .withBoundaries(Arrays.asList(1.0, 2.0)))),
                mock(SpiHelper.class),
                Collections.emptyList());

    assertThat(view.toString()).isEqualTo(expectedView.toString());
  }
}
