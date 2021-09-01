/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

final class MarshalerUtil {
  static final byte[] EMPTY_BYTES = new byte[0];

  static <T, U> Map<Resource, Map<InstrumentationLibraryInfo, List<U>>> groupByResourceAndLibrary(
      Collection<T> dataList,
      Function<T, Resource> getResource,
      Function<T, InstrumentationLibraryInfo> getInstrumentationLibrary,
      Function<T, U> createMarshaler) {
    // expectedMaxSize of 8 means initial map capacity of 16 to match HashMap
    IdentityHashMap<Resource, Map<InstrumentationLibraryInfo, List<U>>> result =
        new IdentityHashMap<>(8);
    for (T data : dataList) {
      Map<InstrumentationLibraryInfo, List<U>> libraryInfoListMap =
          result.computeIfAbsent(getResource.apply(data), unused -> new IdentityHashMap<>(8));
      List<U> marshalerList =
          libraryInfoListMap.computeIfAbsent(
              getInstrumentationLibrary.apply(data), unused -> new ArrayList<>());
      marshalerList.add(createMarshaler.apply(data));
    }
    return result;
  }

  static void marshalRepeatedFixed64(int fieldNumber, List<Long> values, CodedOutputStream output)
      throws IOException {
    if (values.isEmpty()) {
      return;
    }
    output.writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    // TODO(anuraaga): Consider passing in from calculateSize to avoid recomputing.
    output.writeUInt32NoTag(WireFormat.FIXED64_SIZE * values.size());
    for (long value : values) {
      output.writeFixed64NoTag(value);
    }
  }

  static void marshalRepeatedDouble(int fieldNumber, List<Double> values, CodedOutputStream output)
      throws IOException {
    if (values.isEmpty()) {
      return;
    }
    output.writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
    // TODO(anuraaga): Consider passing in from calculateSize to avoid recomputing.
    output.writeUInt32NoTag(WireFormat.FIXED64_SIZE * values.size());
    for (double value : values) {
      output.writeDoubleNoTag(value);
    }
  }

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
    output.writeUInt32NoTag(message.getProtoSerializedSize());
    message.writeTo(output);
  }

  static void marshalBool(int fieldNumber, boolean value, CodedOutputStream output)
      throws IOException {
    if (!value) {
      return;
    }
    output.writeBool(fieldNumber, value);
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

  static void marshalDouble(int fieldNumber, double message, CodedOutputStream output)
      throws IOException {
    if (message == 0D) {
      return;
    }
    output.writeDouble(fieldNumber, message);
  }

  static void marshalBytes(int fieldNumber, byte[] message, CodedOutputStream output)
      throws IOException {
    if (message.length == 0) {
      return;
    }
    output.writeByteArray(fieldNumber, message);
  }

  static int sizeRepeatedFixed64(int fieldNumber, List<Long> values) {
    return sizeRepeatedFixed64(fieldNumber, values.size());
  }

  private static int sizeRepeatedFixed64(int fieldNumber, int numValues) {
    if (numValues == 0) {
      return 0;
    }
    int dataSize = WireFormat.FIXED64_SIZE * numValues;
    int size = 0;
    size += CodedOutputStream.computeTagSize(fieldNumber);
    size += CodedOutputStream.computeLengthDelimitedFieldSize(dataSize);
    return size;
  }

  static int sizeRepeatedDouble(int fieldNumber, List<Double> values) {
    // Same as fixed64.
    return sizeRepeatedFixed64(fieldNumber, values.size());
  }

  static <T extends Marshaler> int sizeRepeatedMessage(int fieldNumber, T[] repeatedMessage) {
    int size = 0;
    int fieldTagSize = CodedOutputStream.computeTagSize(fieldNumber);
    for (Marshaler message : repeatedMessage) {
      int fieldSize = message.getProtoSerializedSize();
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  static int sizeRepeatedMessage(int fieldNumber, List<? extends Marshaler> repeatedMessage) {
    int size = 0;
    int fieldTagSize = CodedOutputStream.computeTagSize(fieldNumber);
    for (Marshaler message : repeatedMessage) {
      int fieldSize = message.getProtoSerializedSize();
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  static int sizeMessage(int fieldNumber, Marshaler message) {
    int fieldSize = message.getProtoSerializedSize();
    return CodedOutputStream.computeTagSize(fieldNumber)
        + CodedOutputStream.computeUInt32SizeNoTag(fieldSize)
        + fieldSize;
  }

  static int sizeBool(int fieldNumber, boolean value) {
    if (!value) {
      return 0;
    }
    return CodedOutputStream.computeBoolSize(fieldNumber, value);
  }

  static int sizeUInt32(int fieldNumber, int message) {
    if (message == 0) {
      return 0;
    }
    return CodedOutputStream.computeUInt32Size(fieldNumber, message);
  }

  static int sizeDouble(int fieldNumber, double value) {
    if (value == 0D) {
      return 0;
    }
    return CodedOutputStream.computeDoubleSize(fieldNumber, value);
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
