/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SummaryDataTest {

  @Test
  void create() {
    List<ValueAtQuantile> percentileValues =
        Arrays.asList(ImmutableValueAtQuantile.create(3.0, 4.0));
    List<SummaryPointData> points =
        Arrays.asList(
            ImmutableSummaryPointData.create(
                12345, 12346, Attributes.empty(), 1, 2.0, percentileValues));
    SummaryData summary = ImmutableSummaryData.create(points);
    assertThat(summary.getPoints()).isEqualTo(points);
  }
}
