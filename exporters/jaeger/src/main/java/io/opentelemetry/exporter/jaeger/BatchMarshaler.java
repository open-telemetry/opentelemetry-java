/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import io.opentelemetry.exporter.jaeger.proto.api_v2.internal.Batch;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.List;

final class BatchMarshaler extends MarshalerWithSize {

  static BatchMarshaler create(List<SpanData> spans, Resource resource) {
    SpanMarshaler[] spanMarshalers = SpanMarshaler.createRepeated(spans);
    ProcessMarshaler processMarshaler = ProcessMarshaler.create(resource);
    return new BatchMarshaler(spanMarshalers, processMarshaler);
  }

  private final SpanMarshaler[] spans;
  private final ProcessMarshaler process;

  BatchMarshaler(SpanMarshaler[] spans, ProcessMarshaler process) {
    super(calculateSize(spans, process));
    this.spans = spans;
    this.process = process;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Batch.SPANS, spans);
    output.serializeMessage(Batch.PROCESS, process);
  }

  private static int calculateSize(SpanMarshaler[] spans, ProcessMarshaler process) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Batch.SPANS, spans);
    size += MarshalerUtil.sizeMessage(Batch.PROCESS, process);
    return size;
  }
}
