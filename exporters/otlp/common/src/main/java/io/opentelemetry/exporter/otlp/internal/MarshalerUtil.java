/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
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

  static int sizeRepeatedFixed64(ProtoFieldInfo field, List<Long> values) {
    return sizeRepeatedFixed64(field, values.size());
  }

  private static int sizeRepeatedFixed64(ProtoFieldInfo field, int numValues) {
    if (numValues == 0) {
      return 0;
    }
    int dataSize = WireFormat.FIXED64_SIZE * numValues;
    int size = 0;
    size += field.getTagSize();
    size += CodedOutputStream.computeLengthDelimitedFieldSize(dataSize);
    return size;
  }

  static int sizeRepeatedDouble(ProtoFieldInfo field, List<Double> values) {
    // Same as fixed64.
    return sizeRepeatedFixed64(field, values.size());
  }

  static <T extends Marshaler> int sizeRepeatedMessage(ProtoFieldInfo field, T[] repeatedMessage) {
    int size = 0;
    int fieldTagSize = field.getTagSize();
    for (Marshaler message : repeatedMessage) {
      int fieldSize = message.getBinarySerializedSize();
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  static int sizeRepeatedMessage(ProtoFieldInfo field, List<? extends Marshaler> repeatedMessage) {
    int size = 0;
    int fieldTagSize = field.getTagSize();
    for (Marshaler message : repeatedMessage) {
      int fieldSize = message.getBinarySerializedSize();
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  static int sizeMessage(ProtoFieldInfo field, Marshaler message) {
    int fieldSize = message.getBinarySerializedSize();
    return field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
  }

  static int sizeBool(ProtoFieldInfo field, boolean value) {
    if (!value) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeBoolSizeNoTag(value);
  }

  static int sizeUInt32(ProtoFieldInfo field, int message) {
    if (message == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(message);
  }

  static int sizeDouble(ProtoFieldInfo field, double value) {
    if (value == 0D) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeDoubleSizeNoTag(value);
  }

  static int sizeFixed64(ProtoFieldInfo field, long message) {
    if (message == 0L) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeFixed64SizeNoTag(message);
  }

  static int sizeBytes(ProtoFieldInfo field, byte[] message) {
    if (message.length == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeByteArraySizeNoTag(message);
  }

  // Assumes OTLP always defines the first item in an enum with number 0, which it does and will.
  static int sizeEnum(ProtoFieldInfo field, int value) {
    if (value == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeEnumSizeNoTag(value);
  }

  static byte[] toBytes(@Nullable String value) {
    if (value == null || value.isEmpty()) {
      return EMPTY_BYTES;
    }
    return value.getBytes(StandardCharsets.UTF_8);
  }

  private MarshalerUtil() {}
}
