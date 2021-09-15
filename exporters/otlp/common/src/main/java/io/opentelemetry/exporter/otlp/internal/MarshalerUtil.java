/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * Marshaler utilities.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class MarshalerUtil {
  private static final int TRACE_ID_VALUE_SIZE =
      CodedOutputStream.computeLengthDelimitedFieldSize(TraceId.getLength() / 2);
  private static final int SPAN_ID_VALUE_SIZE =
      CodedOutputStream.computeLengthDelimitedFieldSize(SpanId.getLength() / 2);

  private static final boolean JSON_AVAILABLE;

  static {
    boolean jsonAvailable = false;
    try {
      Class.forName("com.fasterxml.jackson.core.JsonFactory");
      jsonAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }
    JSON_AVAILABLE = jsonAvailable;
  }

  static final byte[] EMPTY_BYTES = new byte[0];

  /** Groups SDK items by resource and instrumentation library. */
  public static <T, U>
      Map<Resource, Map<InstrumentationLibraryInfo, List<U>>> groupByResourceAndLibrary(
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

  static String preserializeJsonFields(Marshaler marshaler) {
    if (!MarshalerUtil.JSON_AVAILABLE) {
      return "";
    }

    ByteArrayOutputStream jsonBos = new ByteArrayOutputStream();
    try {
      marshaler.writeJsonTo(jsonBos);
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Serialization error, this is likely a bug in OpenTelemetry.", e);
    }

    // We effectively cache `writeTo`, however Jackson would not allow us to only write out
    // fields
    // which is what writeTo does. So we need to write to an object but skip the object start
    // /
    // end.
    byte[] jsonBytes = jsonBos.toByteArray();
    return new String(jsonBytes, 1, jsonBytes.length - 2, StandardCharsets.UTF_8);
  }

  /** Returns the size of a repeated fixed64 field. */
  public static int sizeRepeatedFixed64(ProtoFieldInfo field, List<Long> values) {
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

  /** Returns the size of a repeated double field. */
  public static int sizeRepeatedDouble(ProtoFieldInfo field, List<Double> values) {
    // Same as fixed64.
    return sizeRepeatedFixed64(field, values.size());
  }

  /** Returns the size of a repeated message field. */
  public static <T extends Marshaler> int sizeRepeatedMessage(
      ProtoFieldInfo field, T[] repeatedMessage) {
    int size = 0;
    int fieldTagSize = field.getTagSize();
    for (Marshaler message : repeatedMessage) {
      int fieldSize = message.getBinarySerializedSize();
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  /** Returns the size of a repeated message field. */
  public static int sizeRepeatedMessage(
      ProtoFieldInfo field, List<? extends Marshaler> repeatedMessage) {
    int size = 0;
    int fieldTagSize = field.getTagSize();
    for (Marshaler message : repeatedMessage) {
      int fieldSize = message.getBinarySerializedSize();
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  /** Returns the size of a message field. */
  public static int sizeMessage(ProtoFieldInfo field, Marshaler message) {
    int fieldSize = message.getBinarySerializedSize();
    return field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
  }

  /** Returns the size of a bool field. */
  public static int sizeBool(ProtoFieldInfo field, boolean value) {
    if (!value) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeBoolSizeNoTag(value);
  }

  /** Returns the size of a uint32 field. */
  public static int sizeUInt32(ProtoFieldInfo field, int message) {
    if (message == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(message);
  }

  /** Returns the size of a double field. */
  public static int sizeDouble(ProtoFieldInfo field, double value) {
    if (value == 0D) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeDoubleSizeNoTag(value);
  }

  /** Returns the size of a fixed64 field. */
  public static int sizeFixed64(ProtoFieldInfo field, long message) {
    if (message == 0L) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeFixed64SizeNoTag(message);
  }

  /** Returns the size of a bytes field. */
  public static int sizeBytes(ProtoFieldInfo field, byte[] message) {
    if (message.length == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeByteArraySizeNoTag(message);
  }

  /** Returns the size of a enum field. */
  // Assumes OTLP always defines the first item in an enum with number 0, which it does and will.
  public static int sizeEnum(ProtoFieldInfo field, int value) {
    if (value == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeEnumSizeNoTag(value);
  }

  /** Returns the size of a trace_id field. */
  public static int sizeTraceId(ProtoFieldInfo field, @Nullable String traceId) {
    if (traceId == null) {
      return 0;
    }
    return field.getTagSize() + TRACE_ID_VALUE_SIZE;
  }

  /** Returns the size of a span_id field. */
  public static int sizeSpanId(ProtoFieldInfo field, @Nullable String spanId) {
    if (spanId == null) {
      return 0;
    }
    return field.getTagSize() + SPAN_ID_VALUE_SIZE;
  }

  /** Converts the string to utf8 bytes for encoding. */
  public static byte[] toBytes(@Nullable String value) {
    if (value == null || value.isEmpty()) {
      return EMPTY_BYTES;
    }
    return value.getBytes(StandardCharsets.UTF_8);
  }

  private MarshalerUtil() {}
}
