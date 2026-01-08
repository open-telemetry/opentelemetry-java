/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.compressor.Compressor;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.marshal.MessageWriter;
import java.io.IOException;
import javax.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
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

  private final MessageWriter messageWriter;
  private final int messageSize;
  private final int contentLength;
  @Nullable private final Compressor compressor;

  /** Creates a new {@link GrpcRequestBody}. */
  public GrpcRequestBody(MessageWriter messageWriter, @Nullable Compressor compressor) {
    this.messageWriter = messageWriter;
    this.compressor = compressor;

    messageSize = messageWriter.getContentLength();
    if (compressor != null) {
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
    if (compressor == null) {
      sink.writeByte(UNCOMPRESSED_FLAG);
      sink.writeInt(messageSize);
      messageWriter.writeMessage(sink.outputStream());
    } else {
      try (Buffer compressedBody = new Buffer()) {
        try (BufferedSink compressedSink =
            Okio.buffer(Okio.sink(compressor.compress(compressedBody.outputStream())))) {
          messageWriter.writeMessage(compressedSink.outputStream());
        }
        sink.writeByte(COMPRESSED_FLAG);
        int compressedBytes = (int) compressedBody.size();
        sink.writeInt(compressedBytes);
        sink.write(compressedBody, compressedBytes);
      }
    }
  }
}
