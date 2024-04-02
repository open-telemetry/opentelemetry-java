/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ScopeMetrics;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.io.IOException;
import java.util.List;

final class InstrumentationScopeMetricsMarshaler extends MarshalerWithSize {
  private final InstrumentationScopeMarshaler instrumentationScope;
  private final List<Marshaler> metricMarshalers;
  private final byte[] schemaUrlUtf8;

  InstrumentationScopeMetricsMarshaler(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<Marshaler> metricMarshalers) {
    super(calculateSize(instrumentationScope, schemaUrlUtf8, metricMarshalers));
    this.instrumentationScope = instrumentationScope;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.metricMarshalers = metricMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ScopeMetrics.SCOPE, instrumentationScope);
    output.serializeRepeatedMessage(ScopeMetrics.METRICS, metricMarshalers);
    output.serializeString(ScopeMetrics.SCHEMA_URL, schemaUrlUtf8);
  }

  public static void writeTo(
      Serializer output,
      MarshalerContext context,
      InstrumentationScopeMarshaler instrumentationScopeMarshaler,
      List<MetricData> metricData,
      byte[] schemaUrlUtf8)
      throws IOException {
    output.serializeMessage(ScopeMetrics.SCOPE, instrumentationScopeMarshaler);
    output.serializeRepeatedMessage(
        ScopeMetrics.METRICS, metricData, MetricMarshaler::writeTo, context);
    output.serializeString(ScopeMetrics.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<Marshaler> metricMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeMetrics.SCOPE, instrumentationScope);
    size += MarshalerUtil.sizeBytes(ScopeMetrics.SCHEMA_URL, schemaUrlUtf8);
    size += MarshalerUtil.sizeRepeatedMessage(ScopeMetrics.METRICS, metricMarshalers);
    return size;
  }

  public static int calculateSize(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      MarshalerContext context,
      List<MetricData> metricData) {
    int sizeIndex = context.addSize();
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeMetrics.SCOPE, instrumentationScope);
    size += MarshalerUtil.sizeBytes(ScopeMetrics.SCHEMA_URL, schemaUrlUtf8);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ScopeMetrics.METRICS, metricData, MetricMarshaler::calculateSize, context);
    context.setSize(sizeIndex, size);

    return size;
  }
}
