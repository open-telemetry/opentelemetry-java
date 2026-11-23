/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MetricReusableDataMarshalerTest {

  @ParameterizedTest
  @EnumSource(MemoryMode.class)
  void countsDataPoints(MemoryMode memoryMode) {
    List<Integer> pointCounts = new ArrayList<>();
    MetricReusableDataMarshaler marshaler =
        new MetricReusableDataMarshaler(
            memoryMode,
            (request, count) -> {
              pointCounts.add(count);
              return CompletableResultCode.ofSuccess();
            });

    marshaler.export(Arrays.asList(metric("first", 3), metric("second", 2), metric("empty", 0)));
    marshaler.export(Collections.singletonList(metric("empty", 0)));
    marshaler.export(Collections.emptyList());
    marshaler.export(Collections.singletonList(metric("next", 1)));

    assertThat(pointCounts).containsExactly(5, 0, 0, 1);
  }

  private static MetricData metric(String name, int count) {
    List<LongPointData> points = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      points.add(ImmutableLongPointData.create(0, 1, Attributes.builder().put("id", i).build(), i));
    }
    return ImmutableMetricData.createLongGauge(
        Resource.empty(),
        InstrumentationScopeInfo.create("test"),
        name,
        "",
        "1",
        ImmutableGaugeData.create(points));
  }
}
