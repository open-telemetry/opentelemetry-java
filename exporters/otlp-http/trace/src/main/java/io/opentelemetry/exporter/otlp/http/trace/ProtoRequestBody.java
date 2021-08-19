/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import com.google.protobuf.Message;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;

final class ProtoRequestBody extends RequestBody {

  private static final MediaType PROTOBUF_MEDIA_TYPE = MediaType.parse("application/x-protobuf");

  private final Message proto;
  private final long contentLength;

  ProtoRequestBody(Message proto) {
    this.proto = proto;
    contentLength = proto.getSerializedSize();
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
    proto.writeTo(bufferedSink.outputStream());
  }
}
