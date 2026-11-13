/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class ViewTest {

  @Test
  void stringRepresentation() {
    assertThat(View.builder().build().toString())
        .startsWith("View{aggregation=DefaultAggregation, attributeFilter=")
        .endsWith(", cardinalityLimit=2000}");
    assertThat(
            View.builder()
                .setName("name")
                .setDescription("description")
                .setAggregation(Aggregation.sum())
                .setCardinalityLimit(10)
                .setAttributeFilter(new HashSet<>(Arrays.asList("key1", "key2")))
                .build()
                .toString())
        .isEqualTo(
            "View{"
                + "name=name, "
                + "description=description, "
                + "aggregation=SumAggregation, "
                + "attributeFilter=IncludeExcludePredicate{globMatchingEnabled=false, included=[key1, key2]}, "
                + "cardinalityLimit=10"
                + "}");
  }

  @Test
  void equalsAndHashCode() {
    View defaultView = View.builder().build();
    View defaultViewCopy = View.builder().build();
    View explicitDefaultAggregation =
        View.builder().setAggregation(Aggregation.defaultAggregation()).build();
    View explicitDefaultCardinality = View.builder().setCardinalityLimit(2000).build();
    View explicitDefaultFilter = View.builder().setAttributeFilter(StringPredicates.ALL).build();

    assertThat(defaultView)
        .isEqualTo(defaultViewCopy)
        .hasSameHashCodeAs(defaultViewCopy)
        .isEqualTo(explicitDefaultAggregation)
        .hasSameHashCodeAs(explicitDefaultAggregation)
        .isEqualTo(explicitDefaultCardinality)
        .hasSameHashCodeAs(explicitDefaultCardinality)
        .isEqualTo(explicitDefaultFilter)
        .hasSameHashCodeAs(explicitDefaultFilter);

    View sum1 = View.builder().setAggregation(Aggregation.sum()).build();
    View sum2 = View.builder().setAggregation(Aggregation.sum()).build();
    assertThat(sum1).isEqualTo(sum2).hasSameHashCodeAs(sum2);
    assertThat(sum1).isNotEqualTo(defaultView);
  }
}
