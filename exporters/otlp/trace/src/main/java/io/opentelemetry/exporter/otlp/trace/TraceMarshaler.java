/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CLIENT;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CONSUMER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_INTERNAL;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_PRODUCER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_SERVER;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_OK;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_UNKNOWN_ERROR;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.UnknownFieldSet;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TraceMarshaler {

  static final class RequestMarshaler extends MarshalerWithSize {
    private final ResourceSpansMarshaler[] resourceSpansMarshalers;

    static RequestMarshaler create(Collection<SpanData> spanDataList) {
      Map<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>> resourceAndLibraryMap =
          TraceMarshaler.groupByResourceAndLibrary(spanDataList);

      final ResourceSpansMarshaler[] resourceSpansMarshalers =
          new ResourceSpansMarshaler[resourceAndLibraryMap.size()];
      int posResource = 0;
      for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>> entry :
          resourceAndLibraryMap.entrySet()) {
        final InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers =
            new InstrumentationLibrarySpansMarshaler[entry.getValue().size()];
        int posInstrumentation = 0;
        for (Map.Entry<InstrumentationLibraryInfo, List<SpanMarshaler>> entryIs :
            entry.getValue().entrySet()) {
          instrumentationLibrarySpansMarshalers[posInstrumentation++] =
              new InstrumentationLibrarySpansMarshaler(
                  InstrumentationLibraryMarshaler.create(entryIs.getKey()), entryIs.getValue());
        }
        resourceSpansMarshalers[posResource++] =
            new ResourceSpansMarshaler(
                ResourceMarshaler.create(entry.getKey()), instrumentationLibrarySpansMarshalers);
      }

      return new RequestMarshaler(resourceSpansMarshalers);
    }

    private RequestMarshaler(ResourceSpansMarshaler[] resourceSpansMarshalers) {
      super(
          MarshalerUtil.sizeRepeatedMessage(
              ExportTraceServiceRequest.RESOURCE_SPANS_FIELD_NUMBER, resourceSpansMarshalers));
      this.resourceSpansMarshalers = resourceSpansMarshalers;
    }

    ExportTraceServiceRequest toRequest() throws IOException {
      byte[] buf = new byte[getSerializedSize()];
      writeTo(CodedOutputStream.newInstance(buf));
      return ExportTraceServiceRequest.newBuilder()
          .setUnknownFields(UnknownFieldSet.newBuilder().mergeFrom(buf).build())
          .build();
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalRepeatedMessage(
          ExportTraceServiceRequest.RESOURCE_SPANS_FIELD_NUMBER, resourceSpansMarshalers, output);
    }
  }

  private static final class ResourceSpansMarshaler extends MarshalerWithSize {
    private final ResourceMarshaler resourceMarshaler;
    private final InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers;

    private ResourceSpansMarshaler(
        ResourceMarshaler resourceMarshaler,
        InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers) {
      super(calculateSize(resourceMarshaler, instrumentationLibrarySpansMarshalers));
      this.resourceMarshaler = resourceMarshaler;
      this.instrumentationLibrarySpansMarshalers = instrumentationLibrarySpansMarshalers;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalMessage(ResourceSpans.RESOURCE_FIELD_NUMBER, resourceMarshaler, output);
      MarshalerUtil.marshalRepeatedMessage(
          ResourceSpans.INSTRUMENTATION_LIBRARY_SPANS_FIELD_NUMBER,
          instrumentationLibrarySpansMarshalers,
          output);
    }

    private static int calculateSize(
        ResourceMarshaler resourceMarshaler,
        InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers) {
      int size = 0;
      size += MarshalerUtil.sizeMessage(ResourceSpans.RESOURCE_FIELD_NUMBER, resourceMarshaler);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              ResourceSpans.INSTRUMENTATION_LIBRARY_SPANS_FIELD_NUMBER,
              instrumentationLibrarySpansMarshalers);
      return size;
    }
  }

  private static final class InstrumentationLibrarySpansMarshaler extends MarshalerWithSize {
    private final InstrumentationLibraryMarshaler instrumentationLibrary;
    private final List<SpanMarshaler> spanMarshalers;

    private InstrumentationLibrarySpansMarshaler(
        InstrumentationLibraryMarshaler instrumentationLibrary,
        List<SpanMarshaler> spanMarshalers) {
      super(calculateSize(instrumentationLibrary, spanMarshalers));
      this.instrumentationLibrary = instrumentationLibrary;
      this.spanMarshalers = spanMarshalers;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalMessage(
          InstrumentationLibrarySpans.INSTRUMENTATION_LIBRARY_FIELD_NUMBER,
          instrumentationLibrary,
          output);
      MarshalerUtil.marshalRepeatedMessage(
          InstrumentationLibrarySpans.SPANS_FIELD_NUMBER, spanMarshalers, output);
    }

    private static int calculateSize(
        InstrumentationLibraryMarshaler instrumentationLibrary,
        List<SpanMarshaler> spanMarshalers) {
      int size = 0;
      size +=
          MarshalerUtil.sizeMessage(
              InstrumentationLibrarySpans.INSTRUMENTATION_LIBRARY_FIELD_NUMBER,
              instrumentationLibrary);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              InstrumentationLibrarySpans.SPANS_FIELD_NUMBER, spanMarshalers);
      return size;
    }
  }

  private static final class SpanMarshaler extends MarshalerWithSize {
    private final byte[] traceId;
    private final byte[] spanId;
    private final byte[] parentSpanId;
    private final byte[] name;
    private final int spanKind;
    private final long startEpochNanos;
    private final long endEpochNanos;
    private final AttributeMarshaler[] attributeMarshalers;
    private final int droppedAttributesCount;
    private final SpanEventMarshaler[] spanEventMarshalers;
    private final int droppedEventsCount;
    private final SpanLinkMarshaler[] spanLinkMarshalers;
    private final int droppedLinksCount;
    private final SpanStatusMarshaler spanStatusMarshaler;

    // Because SpanMarshaler is always part of a repeated field, it cannot return "null".
    private static SpanMarshaler create(SpanData spanData) {
      AttributeMarshaler[] attributeMarshalers =
          AttributeMarshaler.createRepeated(spanData.getAttributes());
      SpanEventMarshaler[] spanEventMarshalers = SpanEventMarshaler.create(spanData.getEvents());
      SpanLinkMarshaler[] spanLinkMarshalers = SpanLinkMarshaler.create(spanData.getLinks());

      byte[] parentSpanId = MarshalerUtil.EMPTY_BYTES;
      SpanContext parentSpanContext = spanData.getParentSpanContext();
      if (parentSpanContext.isValid()) {
        parentSpanId = parentSpanContext.getSpanIdBytes();
      }

      return new SpanMarshaler(
          spanData.getSpanContext().getTraceIdBytes(),
          spanData.getSpanContext().getSpanIdBytes(),
          parentSpanId,
          MarshalerUtil.toBytes(spanData.getName()),
          toProtoSpanKind(spanData.getKind()).getNumber(),
          spanData.getStartEpochNanos(),
          spanData.getEndEpochNanos(),
          attributeMarshalers,
          spanData.getTotalAttributeCount() - spanData.getAttributes().size(),
          spanEventMarshalers,
          spanData.getTotalRecordedEvents() - spanData.getEvents().size(),
          spanLinkMarshalers,
          spanData.getTotalRecordedLinks() - spanData.getLinks().size(),
          SpanStatusMarshaler.create(spanData.getStatus()));
    }

    private SpanMarshaler(
        byte[] traceId,
        byte[] spanId,
        byte[] parentSpanId,
        byte[] name,
        int spanKind,
        long startEpochNanos,
        long endEpochNanos,
        AttributeMarshaler[] attributeMarshalers,
        int droppedAttributesCount,
        SpanEventMarshaler[] spanEventMarshalers,
        int droppedEventsCount,
        SpanLinkMarshaler[] spanLinkMarshalers,
        int droppedLinksCount,
        SpanStatusMarshaler spanStatusMarshaler) {
      super(
          calculateSize(
              traceId,
              spanId,
              parentSpanId,
              name,
              spanKind,
              startEpochNanos,
              endEpochNanos,
              attributeMarshalers,
              droppedAttributesCount,
              spanEventMarshalers,
              droppedEventsCount,
              spanLinkMarshalers,
              droppedLinksCount,
              spanStatusMarshaler));
      this.traceId = traceId;
      this.spanId = spanId;
      this.parentSpanId = parentSpanId;
      this.name = name;
      this.spanKind = spanKind;
      this.startEpochNanos = startEpochNanos;
      this.endEpochNanos = endEpochNanos;
      this.attributeMarshalers = attributeMarshalers;
      this.droppedAttributesCount = droppedAttributesCount;
      this.spanEventMarshalers = spanEventMarshalers;
      this.droppedEventsCount = droppedEventsCount;
      this.spanLinkMarshalers = spanLinkMarshalers;
      this.droppedLinksCount = droppedLinksCount;
      this.spanStatusMarshaler = spanStatusMarshaler;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalBytes(Span.TRACE_ID_FIELD_NUMBER, traceId, output);
      MarshalerUtil.marshalBytes(Span.SPAN_ID_FIELD_NUMBER, spanId, output);
      // TODO: Set TraceState;
      MarshalerUtil.marshalBytes(Span.PARENT_SPAN_ID_FIELD_NUMBER, parentSpanId, output);
      MarshalerUtil.marshalBytes(Span.NAME_FIELD_NUMBER, name, output);

      // TODO: Make this a MarshalerUtil helper.
      output.writeEnum(Span.KIND_FIELD_NUMBER, spanKind);

      MarshalerUtil.marshalFixed64(Span.START_TIME_UNIX_NANO_FIELD_NUMBER, startEpochNanos, output);
      MarshalerUtil.marshalFixed64(Span.END_TIME_UNIX_NANO_FIELD_NUMBER, endEpochNanos, output);

      MarshalerUtil.marshalRepeatedMessage(
          Span.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers, output);
      MarshalerUtil.marshalUInt32(
          Span.DROPPED_ATTRIBUTES_COUNT_FIELD_NUMBER, droppedAttributesCount, output);

      MarshalerUtil.marshalRepeatedMessage(Span.EVENTS_FIELD_NUMBER, spanEventMarshalers, output);
      MarshalerUtil.marshalUInt32(
          Span.DROPPED_EVENTS_COUNT_FIELD_NUMBER, droppedEventsCount, output);

      MarshalerUtil.marshalRepeatedMessage(Span.LINKS_FIELD_NUMBER, spanLinkMarshalers, output);
      MarshalerUtil.marshalUInt32(Span.DROPPED_LINKS_COUNT_FIELD_NUMBER, droppedLinksCount, output);

      MarshalerUtil.marshalMessage(Span.STATUS_FIELD_NUMBER, spanStatusMarshaler, output);
    }

    private static int calculateSize(
        byte[] traceId,
        byte[] spanId,
        byte[] parentSpanId,
        byte[] name,
        int spanKind,
        long startEpochNanos,
        long endEpochNanos,
        AttributeMarshaler[] attributeMarshalers,
        int droppedAttributesCount,
        SpanEventMarshaler[] spanEventMarshalers,
        int droppedEventsCount,
        SpanLinkMarshaler[] spanLinkMarshalers,
        int droppedLinksCount,
        SpanStatusMarshaler spanStatusMarshaler) {
      int size = 0;
      size += MarshalerUtil.sizeBytes(Span.TRACE_ID_FIELD_NUMBER, traceId);
      size += MarshalerUtil.sizeBytes(Span.SPAN_ID_FIELD_NUMBER, spanId);
      // TODO: Set TraceState;
      size += MarshalerUtil.sizeBytes(Span.PARENT_SPAN_ID_FIELD_NUMBER, parentSpanId);
      size += MarshalerUtil.sizeBytes(Span.NAME_FIELD_NUMBER, name);

      // TODO: Make this a MarshalerUtil helper.
      size += CodedOutputStream.computeEnumSize(Span.KIND_FIELD_NUMBER, spanKind);

      size += MarshalerUtil.sizeFixed64(Span.START_TIME_UNIX_NANO_FIELD_NUMBER, startEpochNanos);
      size += MarshalerUtil.sizeFixed64(Span.END_TIME_UNIX_NANO_FIELD_NUMBER, endEpochNanos);

      size += MarshalerUtil.sizeRepeatedMessage(Span.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers);
      size +=
          MarshalerUtil.sizeUInt32(
              Span.DROPPED_ATTRIBUTES_COUNT_FIELD_NUMBER, droppedAttributesCount);

      size += MarshalerUtil.sizeRepeatedMessage(Span.EVENTS_FIELD_NUMBER, spanEventMarshalers);
      size += MarshalerUtil.sizeUInt32(Span.DROPPED_EVENTS_COUNT_FIELD_NUMBER, droppedEventsCount);

      size += MarshalerUtil.sizeRepeatedMessage(Span.LINKS_FIELD_NUMBER, spanLinkMarshalers);
      size += MarshalerUtil.sizeUInt32(Span.DROPPED_LINKS_COUNT_FIELD_NUMBER, droppedLinksCount);

      size += MarshalerUtil.sizeMessage(Span.STATUS_FIELD_NUMBER, spanStatusMarshaler);
      return size;
    }
  }

  private static final class SpanEventMarshaler extends MarshalerWithSize {
    private static final SpanEventMarshaler[] EMPTY = new SpanEventMarshaler[0];
    private final long epochNanos;
    private final byte[] name;
    private final AttributeMarshaler[] attributeMarshalers;
    private final int droppedAttributesCount;

    private static SpanEventMarshaler[] create(List<EventData> events) {
      if (events.isEmpty()) {
        return EMPTY;
      }

      SpanEventMarshaler[] result = new SpanEventMarshaler[events.size()];
      int pos = 0;
      for (EventData event : events) {
        result[pos++] =
            new SpanEventMarshaler(
                event.getEpochNanos(),
                MarshalerUtil.toBytes(event.getName()),
                AttributeMarshaler.createRepeated(event.getAttributes()),
                event.getTotalAttributeCount() - event.getAttributes().size());
      }

      return result;
    }

    private SpanEventMarshaler(
        long epochNanos,
        byte[] name,
        AttributeMarshaler[] attributeMarshalers,
        int droppedAttributesCount) {
      super(calculateSize(epochNanos, name, attributeMarshalers, droppedAttributesCount));
      this.epochNanos = epochNanos;
      this.name = name;
      this.attributeMarshalers = attributeMarshalers;
      this.droppedAttributesCount = droppedAttributesCount;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalFixed64(Span.Event.TIME_UNIX_NANO_FIELD_NUMBER, epochNanos, output);
      MarshalerUtil.marshalBytes(Span.Event.NAME_FIELD_NUMBER, name, output);
      MarshalerUtil.marshalRepeatedMessage(
          Span.Event.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers, output);
      MarshalerUtil.marshalUInt32(
          Span.Event.DROPPED_ATTRIBUTES_COUNT_FIELD_NUMBER, droppedAttributesCount, output);
    }

    private static int calculateSize(
        long epochNanos,
        byte[] name,
        AttributeMarshaler[] attributeMarshalers,
        int droppedAttributesCount) {
      int size = 0;
      size += MarshalerUtil.sizeFixed64(Span.Event.TIME_UNIX_NANO_FIELD_NUMBER, epochNanos);
      size += MarshalerUtil.sizeBytes(Span.Event.NAME_FIELD_NUMBER, name);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              Span.Event.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers);
      size +=
          MarshalerUtil.sizeUInt32(
              Span.Event.DROPPED_ATTRIBUTES_COUNT_FIELD_NUMBER, droppedAttributesCount);
      return size;
    }
  }

  private static final class SpanLinkMarshaler extends MarshalerWithSize {
    private static final SpanLinkMarshaler[] EMPTY = new SpanLinkMarshaler[0];
    private final byte[] traceId;
    private final byte[] spanId;
    private final AttributeMarshaler[] attributeMarshalers;
    private final int droppedAttributesCount;

    private static SpanLinkMarshaler[] create(List<LinkData> links) {
      if (links.isEmpty()) {
        return EMPTY;
      }

      SpanLinkMarshaler[] result = new SpanLinkMarshaler[links.size()];
      int pos = 0;
      for (LinkData link : links) {
        result[pos++] =
            new SpanLinkMarshaler(
                link.getSpanContext().getTraceIdBytes(),
                link.getSpanContext().getSpanIdBytes(),
                AttributeMarshaler.createRepeated(link.getAttributes()),
                link.getTotalAttributeCount() - link.getAttributes().size());
      }

      return result;
    }

    private SpanLinkMarshaler(
        byte[] traceId,
        byte[] spanId,
        AttributeMarshaler[] attributeMarshalers,
        int droppedAttributesCount) {
      super(calculateSize(traceId, spanId, attributeMarshalers, droppedAttributesCount));
      this.traceId = traceId;
      this.spanId = spanId;
      this.attributeMarshalers = attributeMarshalers;
      this.droppedAttributesCount = droppedAttributesCount;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalBytes(Span.Link.TRACE_ID_FIELD_NUMBER, traceId, output);
      MarshalerUtil.marshalBytes(Span.Link.SPAN_ID_FIELD_NUMBER, spanId, output);
      // TODO: Set TraceState;
      MarshalerUtil.marshalRepeatedMessage(
          Span.Link.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers, output);
      MarshalerUtil.marshalUInt32(
          Span.Link.DROPPED_ATTRIBUTES_COUNT_FIELD_NUMBER, droppedAttributesCount, output);
    }

    private static int calculateSize(
        byte[] traceId,
        byte[] spanId,
        AttributeMarshaler[] attributeMarshalers,
        int droppedAttributesCount) {
      int size = 0;
      size += MarshalerUtil.sizeBytes(Span.Link.TRACE_ID_FIELD_NUMBER, traceId);
      size += MarshalerUtil.sizeBytes(Span.Link.SPAN_ID_FIELD_NUMBER, spanId);
      // TODO: Set TraceState;
      size +=
          MarshalerUtil.sizeRepeatedMessage(Span.Link.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers);
      size +=
          MarshalerUtil.sizeUInt32(
              Span.Link.DROPPED_ATTRIBUTES_COUNT_FIELD_NUMBER, droppedAttributesCount);
      return size;
    }
  }

  private static final class SpanStatusMarshaler extends MarshalerWithSize {
    private final Status.StatusCode protoStatusCode;
    private final Status.DeprecatedStatusCode deprecatedStatusCode;
    private final byte[] description;

    static SpanStatusMarshaler create(StatusData status) {
      Status.StatusCode protoStatusCode = Status.StatusCode.STATUS_CODE_UNSET;
      Status.DeprecatedStatusCode deprecatedStatusCode = DEPRECATED_STATUS_CODE_OK;
      if (status.getStatusCode() == StatusCode.OK) {
        protoStatusCode = Status.StatusCode.STATUS_CODE_OK;
      } else if (status.getStatusCode() == StatusCode.ERROR) {
        protoStatusCode = Status.StatusCode.STATUS_CODE_ERROR;
        deprecatedStatusCode = DEPRECATED_STATUS_CODE_UNKNOWN_ERROR;
      }
      byte[] description = MarshalerUtil.toBytes(status.getDescription());
      return new SpanStatusMarshaler(protoStatusCode, deprecatedStatusCode, description);
    }

    private SpanStatusMarshaler(
        Status.StatusCode protoStatusCode,
        Status.DeprecatedStatusCode deprecatedStatusCode,
        byte[] description) {
      super(computeSize(protoStatusCode, deprecatedStatusCode, description));
      this.protoStatusCode = protoStatusCode;
      this.deprecatedStatusCode = deprecatedStatusCode;
      this.description = description;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      // TODO: Make this a MarshalerUtil helper.
      if (deprecatedStatusCode != DEPRECATED_STATUS_CODE_OK) {
        output.writeEnum(Status.DEPRECATED_CODE_FIELD_NUMBER, deprecatedStatusCode.getNumber());
      }
      MarshalerUtil.marshalBytes(Status.MESSAGE_FIELD_NUMBER, description, output);
      // TODO: Make this a MarshalerUtil helper.
      if (protoStatusCode != Status.StatusCode.STATUS_CODE_UNSET) {
        output.writeEnum(Status.CODE_FIELD_NUMBER, protoStatusCode.getNumber());
      }
    }

    private static int computeSize(
        Status.StatusCode protoStatusCode,
        Status.DeprecatedStatusCode deprecatedStatusCode,
        byte[] description) {
      int size = 0;
      // TODO: Make this a MarshalerUtil helper.
      if (deprecatedStatusCode != DEPRECATED_STATUS_CODE_OK) {
        size +=
            CodedOutputStream.computeEnumSize(
                Status.DEPRECATED_CODE_FIELD_NUMBER, deprecatedStatusCode.getNumber());
      }
      size += MarshalerUtil.sizeBytes(Status.MESSAGE_FIELD_NUMBER, description);
      // TODO: Make this a MarshalerUtil helper.
      if (protoStatusCode != Status.StatusCode.STATUS_CODE_UNSET) {
        size +=
            CodedOutputStream.computeEnumSize(
                Status.CODE_FIELD_NUMBER, protoStatusCode.getNumber());
      }
      return size;
    }
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>>
      groupByResourceAndLibrary(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>> result = new HashMap<>();
    for (SpanData spanData : spanDataList) {
      Resource resource = spanData.getResource();
      Map<InstrumentationLibraryInfo, List<SpanMarshaler>> libraryInfoListMap =
          result.get(spanData.getResource());
      if (libraryInfoListMap == null) {
        libraryInfoListMap = new HashMap<>();
        result.put(resource, libraryInfoListMap);
      }
      List<SpanMarshaler> spanList =
          libraryInfoListMap.get(spanData.getInstrumentationLibraryInfo());
      if (spanList == null) {
        spanList = new ArrayList<>();
        libraryInfoListMap.put(spanData.getInstrumentationLibraryInfo(), spanList);
      }
      spanList.add(SpanMarshaler.create(spanData));
    }
    return result;
  }

  private static Span.SpanKind toProtoSpanKind(SpanKind kind) {
    switch (kind) {
      case INTERNAL:
        return SPAN_KIND_INTERNAL;
      case SERVER:
        return SPAN_KIND_SERVER;
      case CLIENT:
        return SPAN_KIND_CLIENT;
      case PRODUCER:
        return SPAN_KIND_PRODUCER;
      case CONSUMER:
        return SPAN_KIND_CONSUMER;
    }
    return Span.SpanKind.UNRECOGNIZED;
  }

  private TraceMarshaler() {}
}
