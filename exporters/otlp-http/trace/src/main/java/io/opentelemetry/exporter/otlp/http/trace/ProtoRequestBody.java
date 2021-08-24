/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import io.opentelemetry.exporter.otlp.internal.CodedOutputStream;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;

final class ProtoRequestBody extends RequestBody {

  private static final MediaType PROTOBUF_MEDIA_TYPE = MediaType.parse("application/x-protobuf");

  private final Marshaler marshaler;
  private final int contentLength;

  ProtoRequestBody(Marshaler marshaler) {
    this.marshaler = marshaler;
    contentLength = marshaler.getSerializedSize();
  }

  @Override
  public long contentLength() {
    return contentLength;
  }

  @Override
  public MediaType contentType() {
    return PROTOBUF_MEDIA_TYPE;
  }

  @Override
  public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
    CodedOutputStream cos =
        CodedOutputStream.newInstance(bufferedSink.outputStream(), contentLength);
    marshaler.writeTo(cos);
    cos.flush();
  }
}
