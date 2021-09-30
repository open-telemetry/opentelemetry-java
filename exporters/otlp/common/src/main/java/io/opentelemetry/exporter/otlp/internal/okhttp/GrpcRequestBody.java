/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.okhttp;

import io.opentelemetry.exporter.otlp.internal.Marshaler;
import java.io.IOException;
import javax.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * A {@link RequestBody} for reading from a {@link Marshaler} and writing in gRPC wire format.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class GrpcRequestBody extends RequestBody {

  private static final int HEADER_LENGTH = 5;

  private static final byte UNCOMPRESSED_FLAG = 0;
  private static final byte COMPRESSED_FLAG = 1;

  private static final MediaType GRPC_MEDIA_TYPE = MediaType.parse("application/grpc");

  private final Marshaler marshaler;
  private final int messageSize;
  private final int contentLength;
  private final boolean compressed;

  /** Creates a new {@link GrpcRequestBody}. */
  public GrpcRequestBody(Marshaler marshaler, boolean compressed) {
    this.marshaler = marshaler;
    this.compressed = compressed;

    messageSize = marshaler.getBinarySerializedSize();
    if (compressed) {
      // Content length not known since we want to compress on the I/O thread.
      contentLength = -1;
    } else {
      contentLength = HEADER_LENGTH + messageSize;
    }
  }

  @Nullable
  @Override
  public MediaType contentType() {
    return GRPC_MEDIA_TYPE;
  }

  @Override
  public long contentLength() {
    return contentLength;
  }

  @Override
  public void writeTo(BufferedSink sink) throws IOException {
    if (!compressed) {
      sink.writeByte(UNCOMPRESSED_FLAG);
      sink.writeInt(messageSize);
      marshaler.writeBinaryTo(sink.outputStream());
    } else {
      try (Buffer compressedBody = new Buffer()) {
        try (BufferedSink gzipSink = Okio.buffer(new GzipSink(compressedBody))) {
          marshaler.writeBinaryTo(gzipSink.outputStream());
        }
        sink.writeByte(COMPRESSED_FLAG);
        int compressedBytes = (int) compressedBody.size();
        sink.writeInt(compressedBytes);
        sink.write(compressedBody, compressedBytes);
      }
    }
  }
}
