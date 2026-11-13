/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Metric;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MetricMarshalerTest {

  @Test
  void reusesEncodedMetadata() throws IOException {
    String name = "request.duration";
    String description = "Request duration";
    String unit = "ms";
    Serializer serializer = mock(Serializer.class);

    for (int i = 0; i < 2; i++) {
      MetricData metric =
          ImmutableMetricData.createLongGauge(
              Resource.empty(),
              InstrumentationScopeInfo.empty(),
              name,
              description,
              unit,
              ImmutableGaugeData.create(Collections.emptyList()));
      ((MetricMarshaler) MetricMarshaler.create(metric)).writeTo(serializer);
    }

    assertReusedBytes(serializer, Metric.NAME, name);
    assertReusedBytes(serializer, Metric.DESCRIPTION, description);
    assertReusedBytes(serializer, Metric.UNIT, unit);
  }

  private static void assertReusedBytes(Serializer serializer, ProtoFieldInfo field, String value)
      throws IOException {
    ArgumentCaptor<byte[]> bytes = ArgumentCaptor.forClass(byte[].class);
    verify(serializer, times(2)).serializeString(eq(field), bytes.capture());
    byte[] first = bytes.getAllValues().get(0);
    byte[] second = bytes.getAllValues().get(1);
    assertThat(first).isEqualTo(value.getBytes(StandardCharsets.UTF_8));
    assertThat(second).isSameAs(first);
  }
}
