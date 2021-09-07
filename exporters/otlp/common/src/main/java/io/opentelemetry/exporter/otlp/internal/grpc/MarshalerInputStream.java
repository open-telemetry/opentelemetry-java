/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2014 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import com.google.common.io.ByteStreams;
import io.grpc.Drainable;
import io.grpc.KnownLength;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nullable;

/**
 * Adapter from {@link Marshaler} to gRPC types.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
// Adapted from gRPC ProtoInputStream but using our Marshaller
// https://github.com/grpc/grpc-java/blob/2c2ebaebd5a93acec92fbd2708faac582db99371/protobuf-lite/src/main/java/io/grpc/protobuf/lite/ProtoInputStream.java
public final class MarshalerInputStream extends InputStream implements Drainable, KnownLength {

  @Nullable private Marshaler message;
  @Nullable private ByteArrayInputStream partial;

  /** Creates a new {@link MarshalerInputStream}. */
  public MarshalerInputStream(Marshaler message) {
    this.message = message;
  }

  @Override
  public int drainTo(OutputStream target) throws IOException {
    int written;
    if (message != null) {
      written = message.getBinarySerializedSize();
      message.writeBinaryTo(target);
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
      int size = message.getBinarySerializedSize();
      if (size == 0) {
        message = null;
        partial = null;
        return -1;
      }

      // NB: Because this class is Drainable and KnownLength, we do not expect the read methods to
      // be called in practice. Therefore, we have not copied the branch from upstream that would
      // serialize straight into the provided array without toByteArray (which requires an entire
      // CodedOutputStream implementation). If this method were to be called, it is two extra copies
      // because we also wrap in a ByteArrayOutputStream
      partial = new ByteArrayInputStream(toByteArray(message));
      message = null;
    }
    if (partial != null) {
      return partial.read(b, off, len);
    }
    return -1;
  }

  private static byte[] toByteArray(Marshaler message) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(message.getBinarySerializedSize());
    message.writeBinaryTo(bos);
    return bos.toByteArray();
  }

  @Override
  public int available() {
    if (message != null) {
      return message.getBinarySerializedSize();
    } else if (partial != null) {
      return partial.available();
    }
    return 0;
  }
}
