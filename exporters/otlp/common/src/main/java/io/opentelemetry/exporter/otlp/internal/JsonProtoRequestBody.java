/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.IOException;

/**
 * A {@link RequestBody} for reading from a {@link Marshaler}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class JsonProtoRequestBody extends RequestBody {

  private static final MediaType PROTOBUF_MEDIA_TYPE = MediaType.parse("application/x-protobuf");

  private final Marshaler marshaler;
  private final int contentLength;

  /** Creates a new {@link JsonProtoRequestBody}. */
  public JsonProtoRequestBody(Marshaler marshaler) {
    this.marshaler = marshaler;
    contentLength = marshaler.getBinarySerializedSize();
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
  public void writeTo(BufferedSink bufferedSink) throws IOException {
    marshaler.writeJsonTo(bufferedSink.outputStream());
  }
}
