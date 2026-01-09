/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SampleCompositionTest {

  SampleCompositionBuilder sampleCompositionBuilder;

  @BeforeEach
  void setUp() {
    sampleCompositionBuilder = new SampleCompositionBuilder();
  }

  @Test
  public void empty() {
    assertThat(sampleCompositionBuilder.build()).isEmpty();
  }

  @Test
  public void keyEquality() {
    SampleCompositionKey a;
    SampleCompositionKey b;

    a = new SampleCompositionKey(1, listOf(2, 3), 4);
    b = new SampleCompositionKey(1, listOf(2, 3), 4);
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());

    b = new SampleCompositionKey(1, listOf(3, 2), 4);
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());

    b = new SampleCompositionKey(2, listOf(2, 3), 4);
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  public void valueElidesNulls() {
    SampleCompositionValue v = new SampleCompositionValue();
    v.add(1L, 1L);
    v.add(null, 2L);
    v.add(2L, null);
    assertThat(v.getValues().size()).isEqualTo(2);
    assertThat(v.getTimestamps().size()).isEqualTo(2);
  }

  @Test
  public void isAggregatingSameKey() {
    SampleCompositionKey sampleCompositionKey =
        new SampleCompositionKey(0, Collections.emptyList(), 0);
    sampleCompositionBuilder.add(sampleCompositionKey, 1L, 1L);
    sampleCompositionBuilder.add(sampleCompositionKey, 2L, 2L);

    List<SampleData> sampleDataList = sampleCompositionBuilder.build();
    assertThat(sampleDataList).size().isEqualTo(1);
    assertThat(sampleDataList.get(0).getTimestamps().size()).isEqualTo(2);
    assertThat(sampleDataList.get(0).getValues().size()).isEqualTo(2);
  }

  @Test
  public void isNotAggregatingDifferentKey() {
    SampleCompositionKey keyA = new SampleCompositionKey(1, Collections.emptyList(), 0);
    sampleCompositionBuilder.add(keyA, 1L, 1L);
    SampleCompositionKey keyB = new SampleCompositionKey(2, Collections.emptyList(), 0);
    sampleCompositionBuilder.add(keyB, 2L, 2L);

    List<SampleData> sampleDataList = sampleCompositionBuilder.build();
    assertThat(sampleDataList).size().isEqualTo(2);
    assertThat(sampleDataList.get(0).getTimestamps().size()).isEqualTo(1);
    assertThat(sampleDataList.get(1).getTimestamps().size()).isEqualTo(1);
  }

  private static <T> List<T> listOf(T a, T b) {
    ArrayList<T> list = new ArrayList<>();
    list.add(a);
    list.add(b);
    return Collections.unmodifiableList(list);
  }
}
