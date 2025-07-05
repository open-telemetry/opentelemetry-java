/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class MutableHistogramPointDataTest {

  @Test
  void testSanity() {
    MutableHistogramPointData pointData = new MutableHistogramPointData(10);
    assertThat(pointData.getSum()).isEqualTo(0);
    assertThat(pointData.getCount()).isEqualTo(0);
    assertThat(pointData.getBoundaries()).isEmpty();
    assertThat(pointData.getCounts().size()).isEqualTo(10);
    assertThat(pointData.getExemplars()).isEmpty();

    pointData.set(
        /* startEpochNanos= */ 10,
        /* epochNanos= */ 20,
        Attributes.of(AttributeKey.stringKey("foo"), "bar"),
        /* sum= */ 2,
        /* hasMin= */ true,
        /* min= */ 100,
        /* hasMax= */ true,
        /* max= */ 1000,
        /* boundaries= */ Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0),
        /* counts= */ new long[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
        Collections.emptyList());

    assertThat(pointData.getSum()).isEqualTo(2);
    assertThat(pointData.getCount()).isEqualTo(10 + 20 + 30 + 40 + 50 + 60 + 70 + 80 + 90 + 100);
    assertThat(pointData.getAttributes().get(AttributeKey.stringKey("foo"))).isEqualTo("bar");
    assertThat(pointData.getAttributes().size()).isEqualTo(1);
    assertThat(pointData.getBoundaries())
        .containsExactly(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
    assertThat(pointData.getCounts().toArray())
        .containsExactly(10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L, 100L);
    assertThat(pointData.getStartEpochNanos()).isEqualTo(10);
    assertThat(pointData.getEpochNanos()).isEqualTo(20);

    assertThat(pointData.hasMin()).isTrue();
    assertThat(pointData.getMin()).isEqualTo(100);
    assertThat(pointData.hasMax()).isTrue();
    assertThat(pointData.getMax()).isEqualTo(1000);
    assertThat(pointData.getExemplars()).isEmpty();
    assertThat(pointData.toString())
        .isEqualTo(
            "MutableHistogramPointData{startEpochNanos=10, "
                + "epochNanos=20, "
                + "attributes={foo=\"bar\"}, "
                + "sum=2.0, "
                + "count=550, "
                + "hasMin=true, "
                + "min=100.0, "
                + "hasMax=true, "
                + "max=1000.0, "
                + "boundaries=[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0], "
                + "counts=[10, 20, 30, 40, 50, 60, 70, 80, 90, 100], "
                + "exemplars=[]}");

    MutableHistogramPointData anotherPointData = new MutableHistogramPointData(10);
    // Same values
    anotherPointData.set(
        /* startEpochNanos= */ 10,
        /* epochNanos= */ 20,
        Attributes.of(AttributeKey.stringKey("foo"), "bar"),
        /* sum= */ 2,
        /* hasMin= */ true,
        /* min= */ 100,
        /* hasMax= */ true,
        /* max= */ 1000,
        /* boundaries= */ Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0),
        /* counts= */ new long[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
        Collections.emptyList());
    assertThat(anotherPointData).isEqualTo(pointData);
    assertThat(anotherPointData.hashCode()).isEqualTo(pointData.hashCode());

    // Same values but different sum
    anotherPointData.set(
        /* startEpochNanos= */ 10,
        /* epochNanos= */ 20,
        Attributes.of(AttributeKey.stringKey("foo"), "bar"),
        /* sum= */ 20000,
        /* hasMin= */ true,
        /* min= */ 100,
        /* hasMax= */ true,
        /* max= */ 1000,
        /* boundaries= */ Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0),
        /* counts= */ new long[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
        Collections.emptyList());
    assertThat(anotherPointData).isNotEqualTo(pointData);
    assertThat(anotherPointData.hashCode()).isNotEqualTo(pointData.hashCode());
  }

  @Test
  void testBoundaries() {
    MutableHistogramPointData pointData = new MutableHistogramPointData(10);
    assertThatThrownBy(
            () ->
                pointData.set(
                    /* startEpochNanos= */ 10,
                    /* epochNanos= */ 20,
                    Attributes.of(AttributeKey.stringKey("foo"), "bar"),
                    /* sum= */ 2,
                    /* hasMin= */ true,
                    /* min= */ 100,
                    /* hasMax= */ true,
                    /* max= */ 1000,
                    /* boundaries= */ Arrays.asList(1.0, 2.0, 3.0, 4.0),
                    /* counts= */ new long[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
                    Collections.emptyList()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid boundaries: size should be 9 but was 4");

    assertThatThrownBy(
            () ->
                pointData.set(
                    /* startEpochNanos= */ 10,
                    /* epochNanos= */ 20,
                    Attributes.of(AttributeKey.stringKey("foo"), "bar"),
                    /* sum= */ 2,
                    /* hasMin= */ true,
                    /* min= */ 100,
                    /* hasMax= */ true,
                    /* max= */ 1000,
                    /* boundaries= */ Arrays.asList(
                        1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, Double.POSITIVE_INFINITY),
                    /* counts= */ new long[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
                    Collections.emptyList()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid boundaries: contains explicit +/-Inf");
  }

  @Test
  void testCounts() {
    MutableHistogramPointData pointData = new MutableHistogramPointData(10);
    assertThatThrownBy(
            () ->
                pointData.set(
                    /* startEpochNanos= */ 10,
                    /* epochNanos= */ 20,
                    Attributes.of(AttributeKey.stringKey("foo"), "bar"),
                    /* sum= */ 2,
                    /* hasMin= */ true,
                    /* min= */ 100,
                    /* hasMax= */ true,
                    /* max= */ 1000,
                    /* boundaries= */ Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0),
                    /* counts= */ new long[] {10, 20, 30, 40, 50, 60},
                    Collections.emptyList()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid counts: size should be 10 but was 6");
  }
}
