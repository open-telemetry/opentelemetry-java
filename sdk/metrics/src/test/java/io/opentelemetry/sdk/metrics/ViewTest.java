/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ViewTest {

  @Test
  void stringRepresentation() {
    assertThat(View.builder().build().toString())
        .isEqualTo(
            "View{aggregation=DefaultAggregation, attributesProcessor=NoopAttributesProcessor{}}");
    assertThat(
            View.builder()
                .setName("name")
                .setDescription("description")
                .setAggregation(Aggregation.sum())
                .build()
                .toString())
        .isEqualTo(
            "View{"
                + "name=name, "
                + "description=description, "
                + "aggregation=SumAggregation, "
                + "attributesProcessor=NoopAttributesProcessor{}"
                + "}");
  }
}
