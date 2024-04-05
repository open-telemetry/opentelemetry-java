/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ScopeMetrics;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.io.IOException;
import java.util.List;

final class InstrumentationScopeMetricsStatelessMarshaler
    implements StatelessMarshaler2<InstrumentationScopeInfo, List<MetricData>> {
  static final InstrumentationScopeMetricsStatelessMarshaler INSTANCE =
      new InstrumentationScopeMetricsStatelessMarshaler();

  @Override
  public void writeTo(
      Serializer output,
      InstrumentationScopeInfo instrumentationScope,
      List<MetricData> metrics,
      MarshalerContext context)
      throws IOException {
    InstrumentationScopeMarshaler instrumentationScopeMarshaler =
        context.getObject(InstrumentationScopeMarshaler.class);
    byte[] schemaUrlUtf8 = context.getByteArray();
    output.serializeMessage(ScopeMetrics.SCOPE, instrumentationScopeMarshaler);
    output.serializeRepeatedMessage(
        ScopeMetrics.METRICS, metrics, MetricStatelessMarshaler.INSTANCE, context);
    output.serializeString(ScopeMetrics.SCHEMA_URL, schemaUrlUtf8);
  }

  @Override
  public int getBinarySerializedSize(
      InstrumentationScopeInfo instrumentationScope,
      List<MetricData> metrics,
      MarshalerContext context) {
    InstrumentationScopeMarshaler instrumentationScopeMarshaler =
        InstrumentationScopeMarshaler.create(instrumentationScope);
    context.addData(instrumentationScopeMarshaler);
    // XXX
    byte[] schemaUrlUtf8 = MarshalerUtil.toBytes(instrumentationScope.getSchemaUrl());
    context.addData(schemaUrlUtf8);

    // int sizeIndex = context.addSize();
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeMetrics.SCOPE, instrumentationScopeMarshaler);
    size += MarshalerUtil.sizeBytes(ScopeMetrics.SCHEMA_URL, schemaUrlUtf8);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ScopeMetrics.METRICS, metrics, MetricStatelessMarshaler.INSTANCE, context);
    // context.setSize(sizeIndex, size);

    return size;
  }
}
