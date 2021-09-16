/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import io.opentelemetry.exporter.jaeger.proto.api_v2.internal.PostSpansRequest;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.List;

final class PostSpansRequestMarshaler extends MarshalerWithSize {

  static PostSpansRequestMarshaler create(List<SpanData> spans, Resource resource) {
    return new PostSpansRequestMarshaler(BatchMarshaler.create(spans, resource));
  }

  private final BatchMarshaler batch;

  PostSpansRequestMarshaler(BatchMarshaler batch) {
    super(calculateSize(batch));
    this.batch = batch;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeMessage(PostSpansRequest.BATCH, batch);
  }

  private static int calculateSize(BatchMarshaler batch) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(PostSpansRequest.BATCH, batch);
    return size;
  }
}
