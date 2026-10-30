/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class ViewTest {

  @Test
  void stringRepresentation() {
    assertThat(View.builder().build().toString())
        .startsWith("View{aggregation=DefaultAggregation, attributeFilter=")
        .endsWith(", cardinalityLimit=2000}");
    ViewBuilder viewBuilder =
        View.builder()
            .setName("name")
            .setDescription("description")
            .setAggregation(Aggregation.sum())
            .setCardinalityLimit(10)
            .setAttributeFilter(new HashSet<>(Arrays.asList("key1", "key2")));
    assertThat(viewBuilder.build().toString())
        .isEqualTo(
            "View{"
                + "name=name, "
                + "description=description, "
                + "aggregation=SumAggregation, "
                + "attributeFilter=IncludeExcludePredicate{globMatchingEnabled=false, included=[key1, key2]}, "
                + "cardinalityLimit=10"
                + "}");
    SdkMeterProviderUtil.setEnabled(viewBuilder, /* enabled= */ true);
    assertThat(viewBuilder.build().toString())
        .isEqualTo(
            "View{"
                + "name=name, "
                + "description=description, "
                + "aggregation=SumAggregation, "
                + "attributeFilter=IncludeExcludePredicate{globMatchingEnabled=false, included=[key1, key2]}, "
                + "cardinalityLimit=10, "
                + "enabled=true"
                + "}");
  }
}
