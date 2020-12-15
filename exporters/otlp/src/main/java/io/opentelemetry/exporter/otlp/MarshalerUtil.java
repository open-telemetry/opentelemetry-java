/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.WireFormat;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;

final class MarshalerUtil {
  static <T extends Marshaler> void marshalRepeatedMessage(
      int fieldNumber, T[] repeatedMessage, CodedOutputStream output) throws IOException {
    for (Marshaler message : repeatedMessage) {
      marshalMessage(fieldNumber, message, output);
    }
  }

  static void marshalRepeatedMessage(
      int fieldNumber, List<? extends Marshaler> repeatedMessage, CodedOutputStream output)
      throws IOException {
    for (Marshaler message : repeatedMessage) {
      marshalMessage(fieldNumber, message, output);
    }
  }

  static void marshalMessage(int fieldNumber, Marshaler message, CodedOutputStream output)
      throws IOException {
    output.writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    output.writeUInt32NoTag(message.getSerializedSize());
    message.writeTo(output);
  }

  static void marshalUInt32(int fieldNumber, int message, CodedOutputStream output)
      throws IOException {
    if (message == 0) {
      return;
    }
    output.writeUInt32(fieldNumber, message);
  }

  static void marshalFixed64(int fieldNumber, long message, CodedOutputStream output)
      throws IOException {
    if (message == 0L) {
      return;
    }
    output.writeFixed64(fieldNumber, message);
  }

  static void marshalBytes(int fieldNumber, ByteString message, CodedOutputStream output)
      throws IOException {
    if (message.isEmpty()) {
      return;
    }
    output.writeBytes(fieldNumber, message);
  }

  static <T extends Marshaler> int sizeRepeatedMessage(int fieldNumber, T[] repeatedMessage) {
    int size = 0;
    int fieldTagSize = CodedOutputStream.computeTagSize(fieldNumber);
    for (Marshaler message : repeatedMessage) {
      int fieldSize = message.getSerializedSize();
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  static int sizeRepeatedMessage(int fieldNumber, List<? extends Marshaler> repeatedMessage) {
    int size = 0;
    int fieldTagSize = CodedOutputStream.computeTagSize(fieldNumber);
    for (Marshaler message : repeatedMessage) {
      int fieldSize = message.getSerializedSize();
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  static int sizeMessage(int fieldNumber, Marshaler message) {
    if (message == null) {
      return 0;
    }
    int fieldSize = message.getSerializedSize();
    return CodedOutputStream.computeTagSize(fieldNumber)
        + CodedOutputStream.computeUInt32SizeNoTag(fieldSize)
        + fieldSize;
  }

  static int sizeUInt32(int fieldNumber, int message) {
    if (message == 0) {
      return 0;
    }
    return CodedOutputStream.computeUInt32Size(fieldNumber, message);
  }

  static int sizeFixed64(int fieldNumber, long message) {
    if (message == 0L) {
      return 0;
    }
    return CodedOutputStream.computeFixed64Size(fieldNumber, message);
  }

  static int sizeBytes(int fieldNumber, ByteString message) {
    if (message.isEmpty()) {
      return 0;
    }
    return CodedOutputStream.computeBytesSize(fieldNumber, message);
  }

  static ByteString toByteString(@Nullable String value) {
    if (value == null || value.isEmpty()) {
      return ByteString.EMPTY;
    }
    return ByteString.copyFromUtf8(value);
  }

  private MarshalerUtil() {}
}
