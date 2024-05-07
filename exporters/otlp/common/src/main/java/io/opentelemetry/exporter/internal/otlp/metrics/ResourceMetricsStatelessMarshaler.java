/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ResourceMetrics;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A Marshaler of ResourceMetrics. See {@link ResourceMetricsMarshaler}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceMetricsStatelessMarshaler
    implements StatelessMarshaler2<Resource, Map<InstrumentationScopeInfo, List<MetricData>>> {
  static final ResourceMetricsStatelessMarshaler INSTANCE = new ResourceMetricsStatelessMarshaler();
  private static final MarshalerContext.Key SCOPE_METRIC_WRITER_KEY = MarshalerContext.key();
  private static final MarshalerContext.Key SCOPE_METRIC_SIZE_CALCULATOR_KEY =
      MarshalerContext.key();

  private ResourceMetricsStatelessMarshaler() {}

  @Override
  public void writeTo(
      Serializer output,
      Resource resource,
      Map<InstrumentationScopeInfo, List<MetricData>> scopeMap,
      MarshalerContext context)
      throws IOException {
    ResourceMarshaler resourceMarshaler = context.getData(ResourceMarshaler.class);
    output.serializeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);

    output.serializeRepeatedMessageWithContext(
        ResourceMetrics.SCOPE_METRICS,
        scopeMap,
        InstrumentationScopeMetricsStatelessMarshaler.INSTANCE,
        context,
        SCOPE_METRIC_WRITER_KEY);

    output.serializeStringWithContext(ResourceMetrics.SCHEMA_URL, resource.getSchemaUrl(), context);
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
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            ResourceMetrics.SCOPE_METRICS,
            scopeMap,
            InstrumentationScopeMetricsStatelessMarshaler.INSTANCE,
            context,
            SCOPE_METRIC_SIZE_CALCULATOR_KEY);

    size +=
        StatelessMarshalerUtil.sizeStringWithContext(
            ResourceMetrics.SCHEMA_URL, resource.getSchemaUrl(), context);

    return size;
  }
}
