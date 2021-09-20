/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.traces;

import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.collector.trace.v1.internal.ExportTraceServiceRequest;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.Collection;

/**
 * {@link Marshaler} to convert SDK {@link SpanData} to OTLP ExportTraceServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class TraceRequestMarshaler extends MarshalerWithSize {

  private final ResourceSpansMarshaler[] resourceSpansMarshalers;

  /**
   * Returns a {@link TraceRequestMarshaler} that can be used to convert the provided {@link
   * SpanData} into a serialized OTLP ExportTraceServiceRequest.
   */
  public static TraceRequestMarshaler create(Collection<SpanData> spanDataList) {
    return new TraceRequestMarshaler(ResourceSpansMarshaler.create(spanDataList));
  }

  private TraceRequestMarshaler(ResourceSpansMarshaler[] resourceSpansMarshalers) {
    super(
        MarshalerUtil.sizeRepeatedMessage(
            ExportTraceServiceRequest.RESOURCE_SPANS, resourceSpansMarshalers));
    this.resourceSpansMarshalers = resourceSpansMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(
        ExportTraceServiceRequest.RESOURCE_SPANS, resourceSpansMarshalers);
  }
}
