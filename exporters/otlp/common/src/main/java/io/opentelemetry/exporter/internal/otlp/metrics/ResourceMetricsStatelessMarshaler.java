/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ResourceMetrics;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A Marshaler of ResourceMetrics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceMetricsStatelessMarshaler
    implements StatelessMarshaler2<Resource, Map<InstrumentationScopeInfo, List<MetricData>>> {
  static final ResourceMetricsStatelessMarshaler INSTANCE = new ResourceMetricsStatelessMarshaler();
  private static final Object SCOPE_SPAN_WRITER_KEY = new Object();
  private static final Object SCOPE_SPAN_SIZE_CALCULATOR_KEY = new Object();

  @Override
  public void writeTo(
      Serializer output,
      Resource resource,
      Map<InstrumentationScopeInfo, List<MetricData>> scopeMap,
      MarshalerContext context)
      throws IOException {
    ResourceMarshaler resourceMarshaler = context.getObject(ResourceMarshaler.class);
    output.serializeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);

    output.serializeRepeatedMessage(
        ResourceMetrics.SCOPE_METRICS,
        scopeMap,
        InstrumentationScopeMetricsStatelessMarshaler.INSTANCE,
        context,
        SCOPE_SPAN_WRITER_KEY);

    byte[] schemaUrlUtf8 = context.getByteArray();
    output.serializeString(ResourceMetrics.SCHEMA_URL, schemaUrlUtf8);
  }

  @Override
  public int getBinarySerializedSize(
      Resource resource,
      Map<InstrumentationScopeInfo, List<MetricData>> scopeMap,
      MarshalerContext context) {

    int size = 0;

    ResourceMarshaler resourceMarshaler = ResourceMarshaler.create(resource);
    context.addData(resourceMarshaler);
    size += MarshalerUtil.sizeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);

    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceMetrics.SCOPE_METRICS,
            scopeMap,
            InstrumentationScopeMetricsStatelessMarshaler.INSTANCE,
            context,
            SCOPE_SPAN_SIZE_CALCULATOR_KEY);

    byte[] schemaUrlUtf8 = MarshalerUtil.toBytes(resource.getSchemaUrl());
    context.addData(schemaUrlUtf8);
    size += MarshalerUtil.sizeBytes(ResourceMetrics.SCHEMA_URL, schemaUrlUtf8);

    return size;
  }
}
