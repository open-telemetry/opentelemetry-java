/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.collector.trace.v1.internal.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * {@link Marshaler} to convert SDK {@link SpanData} to OTLP ExportTraceServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LowAllocationTraceRequestMarshaler extends Marshaler {

  private final MarshalerContext context = new MarshalerContext();

  @SuppressWarnings("NullAway")
  private Map<Resource, Map<InstrumentationScopeInfo, List<SpanData>>> resourceAndScopeMap;

  private int size;

  public void initialize(Collection<SpanData> spanDataList) {
    resourceAndScopeMap = groupByResourceAndScope(context, spanDataList);
    size = calculateSize(context, resourceAndScopeMap);
  }

  public void reset() {
    context.reset();
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  private final ResourceScopeMapWriter resourceScopeMapWriter = new ResourceScopeMapWriter();

  @Override
  public void writeTo(Serializer output) {
    // serializing can be retried, reset the indexes, so we could call writeTo multiple times
    context.resetReadIndex();
    resourceScopeMapWriter.init(output, context);
    resourceAndScopeMap.forEach(resourceScopeMapWriter);
  }

  private static int calculateSize(
      MarshalerContext context,
      Map<Resource, Map<InstrumentationScopeInfo, List<SpanData>>> resourceAndScopeMap) {
    if (resourceAndScopeMap.isEmpty()) {
      return 0;
    }

    ResourceScopeMapSizeCalculator resourceScopeMapSizeCalculator =
        context.getInstance(
            ResourceScopeMapSizeCalculator.class, ResourceScopeMapSizeCalculator::new);
    resourceScopeMapSizeCalculator.init(ExportTraceServiceRequest.RESOURCE_SPANS, context);
    resourceAndScopeMap.forEach(resourceScopeMapSizeCalculator);

    return resourceScopeMapSizeCalculator.size;
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<SpanData>>>
      groupByResourceAndScope(MarshalerContext context, Collection<SpanData> spanDataList) {

    if (spanDataList.isEmpty()) {
      return Collections.emptyMap();
    }

    return MarshalerUtil.groupByResourceAndScope(
        spanDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        SpanData::getResource,
        SpanData::getInstrumentationScopeInfo,
        Function.identity(),
        context);
  }

  private static class ResourceScopeMapWriter
      implements BiConsumer<Resource, Map<InstrumentationScopeInfo, List<SpanData>>> {
    @SuppressWarnings("NullAway")
    Serializer output;

    @SuppressWarnings("NullAway")
    MarshalerContext context;

    void init(Serializer output, MarshalerContext context) {
      this.output = output;
      this.context = context;
    }

    @Override
    public void accept(
        Resource resource,
        Map<InstrumentationScopeInfo, List<SpanData>> instrumentationScopeInfoListMap) {
      try {
        output.writeStartRepeated(ExportTraceServiceRequest.RESOURCE_SPANS);
        output.writeStartRepeatedElement(
            ExportTraceServiceRequest.RESOURCE_SPANS, context.getSize());

        ResourceSpansMarshaler.writeTo(output, instrumentationScopeInfoListMap, context);

        output.writeEndRepeatedElement();
        output.writeEndRepeated();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static class ResourceScopeMapSizeCalculator
      implements BiConsumer<Resource, Map<InstrumentationScopeInfo, List<SpanData>>> {
    int size;
    int fieldTagSize;

    @SuppressWarnings("NullAway")
    MarshalerContext context;

    void init(ProtoFieldInfo field, MarshalerContext context) {
      this.size = 0;
      this.fieldTagSize = field.getTagSize();
      this.context = context;
    }

    @Override
    public void accept(
        Resource resource,
        Map<InstrumentationScopeInfo, List<SpanData>> instrumentationScopeInfoListMap) {
      int fieldSize =
          ResourceSpansMarshaler.calculateSize(context, resource, instrumentationScopeInfoListMap);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
  }
}
