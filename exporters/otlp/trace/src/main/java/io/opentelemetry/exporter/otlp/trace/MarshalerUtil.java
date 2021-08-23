/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Nullable;

final class MarshalerUtil {
  static final byte[] EMPTY_BYTES = new byte[0];

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

  static void marshalBytes(int fieldNumber, byte[] message, CodedOutputStream output)
      throws IOException {
    if (message.length == 0) {
      return;
    }
    output.writeByteArray(fieldNumber, message);
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

  static int sizeBytes(int fieldNumber, byte[] message) {
    if (message.length == 0) {
      return 0;
    }
    return CodedOutputStream.computeByteArraySize(fieldNumber, message);
  }

  static byte[] toBytes(@Nullable String value) {
    if (value == null || value.isEmpty()) {
      return EMPTY_BYTES;
    }
    return value.getBytes(StandardCharsets.UTF_8);
  }

  private MarshalerUtil() {}
}
