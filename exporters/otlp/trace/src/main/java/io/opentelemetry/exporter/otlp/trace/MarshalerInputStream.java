/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import com.google.common.io.ByteStreams;
import com.google.protobuf.CodedOutputStream;
import io.grpc.Drainable;
import io.grpc.KnownLength;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nullable;

// Adapted from gRPC ProtoInputStream but using our Marshaller
// https://github.com/grpc/grpc-java/blob/2c2ebaebd5a93acec92fbd2708faac582db99371/protobuf-lite/src/main/java/io/grpc/protobuf/lite/ProtoInputStream.java
final class MarshalerInputStream extends InputStream implements Drainable, KnownLength {
  private static final int DEFAULT_BUFFER_SIZE = 4096;

  @Nullable private Marshaler message;
  @Nullable private ByteArrayInputStream partial;

  MarshalerInputStream(Marshaler message) {
    this.message = message;
  }

  @Override
  public int drainTo(OutputStream target) throws IOException {
    int written;
    if (message != null) {
      written = message.getSerializedSize();
      int bufferSize = computePreferredBufferSize(message.getSerializedSize());
      CodedOutputStream cos = CodedOutputStream.newInstance(target, bufferSize);
      message.writeTo(cos);
      cos.flush();
      message = null;
    } else if (partial != null) {
      written = (int) ByteStreams.copy(partial, target);
      partial = null;
    } else {
      written = 0;
    }
    return written;
  }

  @Override
  public int read() throws IOException {
    if (message != null) {
      partial = new ByteArrayInputStream(toByteArray(message));
      message = null;
    }
    if (partial != null) {
      return partial.read();
    }
    return -1;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (message != null) {
      int size = message.getSerializedSize();
      if (size == 0) {
        message = null;
        partial = null;
        return -1;
      }
      if (len >= size) {
        // This is the only case that is zero-copy.
        CodedOutputStream stream = CodedOutputStream.newInstance(b, off, size);
        message.writeTo(stream);
        stream.flush();
        stream.checkNoSpaceLeft();

        message = null;
        partial = null;
        return size;
      }

      partial = new ByteArrayInputStream(toByteArray(message));
      message = null;
    }
    if (partial != null) {
      return partial.read(b, off, len);
    }
    return -1;
  }

  private static byte[] toByteArray(Marshaler message) throws IOException {
    byte[] output = new byte[message.getSerializedSize()];
    message.writeTo(CodedOutputStream.newInstance(output));
    return output;
  }

  @Override
  public int available() {
    if (message != null) {
      return message.getSerializedSize();
    } else if (partial != null) {
      return partial.available();
    }
    return 0;
  }

  private static int computePreferredBufferSize(int dataLength) {
    if (dataLength > DEFAULT_BUFFER_SIZE) {
      return DEFAULT_BUFFER_SIZE;
    }
    return dataLength;
  }
}
