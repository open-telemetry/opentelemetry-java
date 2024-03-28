/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.trace.v1.internal.ResourceSpans;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A Marshaler of ResourceSpans.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceSpansMarshaler extends MarshalerWithSize {
  private final ResourceMarshaler resourceMarshaler;
  private final byte[] schemaUrlUtf8;
  private final InstrumentationScopeSpansMarshaler[] instrumentationScopeSpansMarshalers;

  /** Returns Marshalers of ResourceSpans created by grouping the provided SpanData. */
  @SuppressWarnings("AvoidObjectArrays")
  public static ResourceSpansMarshaler[] create(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationScopeInfo, List<SpanMarshaler>>> resourceAndScopeMap =
        groupByResourceAndScope(spanDataList);

    ResourceSpansMarshaler[] resourceSpansMarshalers =
        new ResourceSpansMarshaler[resourceAndScopeMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationScopeInfo, List<SpanMarshaler>>> entry :
        resourceAndScopeMap.entrySet()) {
      InstrumentationScopeSpansMarshaler[] instrumentationScopeSpansMarshalers =
          new InstrumentationScopeSpansMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationScopeInfo, List<SpanMarshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationScopeSpansMarshalers[posInstrumentation++] =
            new InstrumentationScopeSpansMarshaler(
                InstrumentationScopeMarshaler.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }
      resourceSpansMarshalers[posResource++] =
          new ResourceSpansMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationScopeSpansMarshalers);
    }
    return resourceSpansMarshalers;
  }

  ResourceSpansMarshaler(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrlUtf8,
      InstrumentationScopeSpansMarshaler[] instrumentationScopeSpansMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrlUtf8, instrumentationScopeSpansMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.instrumentationScopeSpansMarshalers = instrumentationScopeSpansMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceSpans.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(ResourceSpans.SCOPE_SPANS, instrumentationScopeSpansMarshalers);
    output.serializeString(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);
  }

  public static void writeTo(
      Serializer output,
      Map<InstrumentationScopeInfo, List<SpanData>> scopeMap,
      MarshalerContext context)
      throws IOException {
    ResourceMarshaler resourceMarshaler = context.getObject(ResourceMarshaler.class);
    output.serializeMessage(ResourceSpans.RESOURCE, resourceMarshaler);

    ScopeSpanListWriter scopeSpanListWriter =
        context.getInstance(ScopeSpanListWriter.class, ScopeSpanListWriter::new);
    scopeSpanListWriter.init(output, context);
    scopeMap.forEach(scopeSpanListWriter);

    byte[] schemaUrlUtf8 = context.getByteArray();
    output.serializeString(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrlUtf8,
      InstrumentationScopeSpansMarshaler[] instrumentationScopeSpansMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceSpans.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeBytes(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceSpans.SCOPE_SPANS, instrumentationScopeSpansMarshalers);

    return size;
  }

  public static int calculateSize(
      MarshalerContext context,
      Resource resource,
      Map<InstrumentationScopeInfo, List<SpanData>> scopeMap) {

    int size = 0;
    int sizeIndex = context.addSize();

    ResourceMarshaler resourceMarshaler = ResourceMarshaler.create(resource);
    context.addData(resourceMarshaler);
    size += MarshalerUtil.sizeMessage(ResourceSpans.RESOURCE, resourceMarshaler);

    ScopeSpanListSizeCalculator scopeSpanListSizeCalculator =
        context.getInstance(ScopeSpanListSizeCalculator.class, ScopeSpanListSizeCalculator::new);
    scopeSpanListSizeCalculator.initialize(ResourceSpans.SCOPE_SPANS, context);
    scopeMap.forEach(scopeSpanListSizeCalculator);
    size += scopeSpanListSizeCalculator.size;

    byte[] schemaUrlUtf8 = MarshalerUtil.toBytes(resource.getSchemaUrl());
    context.addData(schemaUrlUtf8);
    size += MarshalerUtil.sizeBytes(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);

    context.setSize(sizeIndex, size);

    return size;
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<SpanMarshaler>>>
      groupByResourceAndScope(Collection<SpanData> spanDataList) {
    return MarshalerUtil.groupByResourceAndScope(
        spanDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        SpanData::getResource,
        SpanData::getInstrumentationScopeInfo,
        SpanMarshaler::create);
  }

  private static class ScopeSpanListWriter
      implements BiConsumer<InstrumentationScopeInfo, List<SpanData>> {
    @SuppressWarnings("NullAway")
    Serializer output;

    @SuppressWarnings("NullAway")
    MarshalerContext context;

    void init(Serializer output, MarshalerContext context) {
      this.output = output;
      this.context = context;
    }

    @Override
    public void accept(InstrumentationScopeInfo instrumentationScopeInfo, List<SpanData> spanData) {
      try {
        output.writeStartRepeated(ResourceSpans.SCOPE_SPANS);
        output.writeStartRepeatedElement(ResourceSpans.SCOPE_SPANS, context.getSize());

        InstrumentationScopeMarshaler instrumentationScopeMarshaler =
            context.getObject(InstrumentationScopeMarshaler.class);
        byte[] schemaUrlUtf8 = context.getByteArray();
        InstrumentationScopeSpansMarshaler.writeTo(
            output, context, instrumentationScopeMarshaler, spanData, schemaUrlUtf8);

        output.writeEndRepeatedElement();
        output.writeEndRepeated();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static class ScopeSpanListSizeCalculator
      implements BiConsumer<InstrumentationScopeInfo, List<SpanData>> {
    int size;
    int fieldTagSize;

    @SuppressWarnings("NullAway")
    MarshalerContext context;

    void initialize(ProtoFieldInfo field, MarshalerContext context) {
      this.size = 0;
      this.fieldTagSize = field.getTagSize();
      this.context = context;
    }

    @Override
    public void accept(InstrumentationScopeInfo instrumentationScopeInfo, List<SpanData> spanData) {
      InstrumentationScopeMarshaler instrumentationScopeMarshaler =
          InstrumentationScopeMarshaler.create(instrumentationScopeInfo);
      context.addData(instrumentationScopeMarshaler);
      byte[] schemaUrlUtf8 = MarshalerUtil.toBytes(instrumentationScopeInfo.getSchemaUrl());
      context.addData(schemaUrlUtf8);
      int fieldSize =
          InstrumentationScopeSpansMarshaler.calculateSize(
              instrumentationScopeMarshaler, schemaUrlUtf8, context, spanData);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
  }
}
