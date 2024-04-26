/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.DynamicPrimitiveLongList;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
  private static final MarshalerContext.Key GROUPER_KEY = MarshalerContext.key();
  private static final MarshalerContext.Key ATTRIBUTES_SIZE_CALCULATOR_KEY = MarshalerContext.key();

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

  private static final byte[] EMPTY_BYTES = new byte[0];

  /** Groups SDK items by resource and instrumentation scope. */
  public static <T, U>
      Map<Resource, Map<InstrumentationScopeInfo, List<U>>> groupByResourceAndScope(
          Collection<T> dataList,
          Function<T, Resource> getResource,
          Function<T, InstrumentationScopeInfo> getInstrumentationScope,
          Function<T, U> createMarshaler) {
    // expectedMaxSize of 8 means initial map capacity of 16 to match HashMap
    IdentityHashMap<Resource, Map<InstrumentationScopeInfo, List<U>>> result =
        new IdentityHashMap<>(8);
    for (T data : dataList) {
      Map<InstrumentationScopeInfo, List<U>> scopeInfoListMap =
          result.computeIfAbsent(getResource.apply(data), unused -> new IdentityHashMap<>(8));
      List<U> marshalerList =
          scopeInfoListMap.computeIfAbsent(
              getInstrumentationScope.apply(data), unused -> new ArrayList<>());
      marshalerList.add(createMarshaler.apply(data));
    }
    return result;
  }

  /** Groups SDK items by resource and instrumentation scope. */
  public static <T> Map<Resource, Map<InstrumentationScopeInfo, List<T>>> groupByResourceAndScope(
      Collection<T> dataList,
      Function<T, Resource> getResource,
      Function<T, InstrumentationScopeInfo> getInstrumentationScope,
      MarshalerContext context) {
    Map<Resource, Map<InstrumentationScopeInfo, List<T>>> result = context.getIdentityMap();

    Grouper<T> grouper = context.getInstance(GROUPER_KEY, Grouper::new);
    grouper.initialize(result, getResource, getInstrumentationScope, context);
    dataList.forEach(grouper);

    return result;
  }

  private static class Grouper<T> implements Consumer<T> {
    @SuppressWarnings("NullAway")
    private Map<Resource, Map<InstrumentationScopeInfo, List<T>>> result;

    @SuppressWarnings("NullAway")
    private Function<T, Resource> getResource;

    @SuppressWarnings("NullAway")
    private Function<T, InstrumentationScopeInfo> getInstrumentationScope;

    @SuppressWarnings("NullAway")
    private MarshalerContext context;

    void initialize(
        Map<Resource, Map<InstrumentationScopeInfo, List<T>>> result,
        Function<T, Resource> getResource,
        Function<T, InstrumentationScopeInfo> getInstrumentationScope,
        MarshalerContext context) {
      this.result = result;
      this.getResource = getResource;
      this.getInstrumentationScope = getInstrumentationScope;
      this.context = context;
    }

    @Override
    public void accept(T data) {
      Resource resource = getResource.apply(data);
      Map<InstrumentationScopeInfo, List<T>> scopeInfoListMap = result.get(resource);
      if (scopeInfoListMap == null) {
        scopeInfoListMap = context.getIdentityMap();
        result.put(resource, scopeInfoListMap);
      }
      InstrumentationScopeInfo instrumentationScopeInfo = getInstrumentationScope.apply(data);
      List<T> elementList = scopeInfoListMap.get(instrumentationScopeInfo);
      if (elementList == null) {
        elementList = context.getList();
        scopeInfoListMap.put(instrumentationScopeInfo, elementList);
      }
      elementList.add(data);
    }
  }

  /** Preserialize into JSON format. */
  public static String preserializeJsonFields(Marshaler marshaler) {
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

  /**
   * Returns the size of a repeated uint64 field.
   *
   * <p>Packed repeated fields contain the tag, an integer representing the incoming payload size,
   * and an actual payload of repeated varints.
   */
  public static int sizeRepeatedUInt64(ProtoFieldInfo field, long[] values) {
    if (values.length == 0) {
      return 0;
    }

    int payloadSize = 0;
    for (long v : values) {
      payloadSize += CodedOutputStream.computeUInt64SizeNoTag(v);
    }

    // tag size + payload indicator size + actual payload size
    return field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(payloadSize) + payloadSize;
  }

  /**
   * Returns the size of a repeated uint64 field.
   *
   * <p>Packed repeated fields contain the tag, an integer representing the incoming payload size,
   * and an actual payload of repeated varints.
   *
   * <p>NOTE: This method has the same logic as {@link #sizeRepeatedUInt64(ProtoFieldInfo, long[])}
   * )} but instead of using a primitive array it uses {@link DynamicPrimitiveLongList} to avoid
   * boxing/unboxing
   */
  public static int sizeRepeatedUInt64(ProtoFieldInfo field, DynamicPrimitiveLongList values) {
    if (values.isEmpty()) {
      return 0;
    }

    int payloadSize = 0;
    for (int i = 0; i < values.size(); i++) {
      long v = values.getLong(i);
      payloadSize += CodedOutputStream.computeUInt64SizeNoTag(v);
    }

    // tag size + payload indicator size + actual payload size
    return field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(payloadSize) + payloadSize;
  }

  /** Returns the size of a repeated double field. */
  public static int sizeRepeatedDouble(ProtoFieldInfo field, List<Double> values) {
    // Same as fixed64.
    return sizeRepeatedFixed64(field, values.size());
  }

  /** Returns the size of a repeated message field. */
  @SuppressWarnings("AvoidObjectArrays")
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

  /** Returns the size of a repeated message field. */
  public static <T> int sizeRepeatedMessage(
      ProtoFieldInfo field,
      List<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context) {
    if (messages.isEmpty()) {
      return 0;
    }

    int size = 0;
    int fieldTagSize = field.getTagSize();
    for (int i = 0; i < messages.size(); i++) {
      T message = messages.get(i);
      int sizeIndex = context.addSize();
      int fieldSize = marshaler.getBinarySerializedSize(message, context);
      context.setSize(sizeIndex, fieldSize);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  /** Returns the size of a repeated message field. */
  @SuppressWarnings("unchecked")
  public static <T> int sizeRepeatedMessage(
      ProtoFieldInfo field,
      Collection<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context,
      MarshalerContext.Key key) {
    if (messages instanceof List) {
      return sizeRepeatedMessage(field, (List<T>) messages, marshaler, context);
    }

    if (messages.isEmpty()) {
      return 0;
    }

    RepeatedElementSizeCalculator<T> sizeCalculator =
        context.getInstance(key, RepeatedElementSizeCalculator::new);
    sizeCalculator.initialize(field, marshaler, context);
    messages.forEach(sizeCalculator);

    return sizeCalculator.size;
  }

  /** Returns the size of a repeated message field. */
  public static <K, V> int sizeRepeatedMessage(
      ProtoFieldInfo field,
      Map<K, V> messages,
      StatelessMarshaler2<K, V> marshaler,
      MarshalerContext context,
      MarshalerContext.Key key) {
    if (messages.isEmpty()) {
      return 0;
    }

    RepeatedElementPairSizeCalculator<K, V> sizeCalculator =
        context.getInstance(key, RepeatedElementPairSizeCalculator::new);
    sizeCalculator.initialize(field, marshaler, context);
    messages.forEach(sizeCalculator);

    return sizeCalculator.size;
  }

  /** Returns the size of a repeated message field. */
  public static int sizeRepeatedMessage(
      ProtoFieldInfo field,
      Attributes attributes,
      StatelessMarshaler2<AttributeKey<?>, Object> marshaler,
      MarshalerContext context) {
    if (attributes.isEmpty()) {
      return 0;
    }

    RepeatedElementPairSizeCalculator<AttributeKey<?>, Object> sizeCalculator =
        context.getInstance(ATTRIBUTES_SIZE_CALCULATOR_KEY, RepeatedElementPairSizeCalculator::new);
    sizeCalculator.initialize(field, marshaler, context);
    attributes.forEach(sizeCalculator);

    return sizeCalculator.size;
  }

  private static class RepeatedElementSizeCalculator<T> implements Consumer<T> {
    private int size;
    private int fieldTagSize;

    @SuppressWarnings("NullAway")
    private StatelessMarshaler<T> marshaler;

    @SuppressWarnings("NullAway")
    private MarshalerContext context;

    void initialize(
        ProtoFieldInfo field, StatelessMarshaler<T> marshaler, MarshalerContext context) {
      this.size = 0;
      this.fieldTagSize = field.getTagSize();
      this.marshaler = marshaler;
      this.context = context;
    }

    @Override
    public void accept(T element) {
      int sizeIndex = context.addSize();
      int fieldSize = marshaler.getBinarySerializedSize(element, context);
      context.setSize(sizeIndex, fieldSize);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
  }

  private static class RepeatedElementPairSizeCalculator<K, V> implements BiConsumer<K, V> {
    private int size;
    private int fieldTagSize;

    @SuppressWarnings("NullAway")
    private StatelessMarshaler2<K, V> marshaler;

    @SuppressWarnings("NullAway")
    private MarshalerContext context;

    void initialize(
        ProtoFieldInfo field, StatelessMarshaler2<K, V> marshaler, MarshalerContext context) {
      this.size = 0;
      this.fieldTagSize = field.getTagSize();
      this.marshaler = marshaler;
      this.context = context;
    }

    @Override
    public void accept(K key, V value) {
      int sizeIndex = context.addSize();
      int fieldSize = marshaler.getBinarySerializedSize(key, value, context);
      context.setSize(sizeIndex, fieldSize);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
  }

  /** Returns the size of a message field. */
  public static int sizeMessage(ProtoFieldInfo field, Marshaler message) {
    int fieldSize = message.getBinarySerializedSize();
    return field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
  }

  /** Returns the size of a message field. */
  public static <T> int sizeMessage(
      ProtoFieldInfo field, T element, StatelessMarshaler<T> marshaler, MarshalerContext context) {
    int sizeIndex = context.addSize();
    int fieldSize = marshaler.getBinarySerializedSize(element, context);
    int size = field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    context.setSize(sizeIndex, fieldSize);
    return size;
  }

  /** Returns the size of a message field. */
  public static <K, V> int sizeMessage(
      ProtoFieldInfo field,
      K key,
      V value,
      StatelessMarshaler2<K, V> marshaler,
      MarshalerContext context) {
    int sizeIndex = context.addSize();
    int fieldSize = marshaler.getBinarySerializedSize(key, value, context);
    int size = field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    context.setSize(sizeIndex, fieldSize);
    return size;
  }

  /** Returns the size of a bool field. */
  public static int sizeBool(ProtoFieldInfo field, boolean value) {
    if (!value) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeBoolSizeNoTag(value);
  }

  /** Returns the size of a int64 field. */
  public static int sizeInt64(ProtoFieldInfo field, long message) {
    if (message == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeInt64SizeNoTag(message);
  }

  /** Returns the size of a uint32 field. */
  public static int sizeUInt32(ProtoFieldInfo field, int message) {
    if (message == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(message);
  }

  /** Returns the size of a sint32 field. */
  public static int sizeSInt32(ProtoFieldInfo field, int message) {
    if (message == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeSInt32SizeNoTag(message);
  }

  /** Returns the size of a int32 field. */
  public static int sizeInt32(ProtoFieldInfo field, int message) {
    if (message == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeInt32SizeNoTag(message);
  }

  /** Returns the size of a double field. */
  public static int sizeDouble(ProtoFieldInfo field, double value) {
    if (value == 0D) {
      return 0;
    }
    return sizeDoubleOptional(field, value);
  }

  /** Returns the size of a double field. */
  public static int sizeDoubleOptional(ProtoFieldInfo field, double value) {
    return field.getTagSize() + CodedOutputStream.computeDoubleSizeNoTag(value);
  }

  /** Returns the size of a fixed64 field. */
  public static int sizeFixed64(ProtoFieldInfo field, long value) {
    if (value == 0L) {
      return 0;
    }
    return sizeFixed64Optional(field, value);
  }

  /** Returns the size of a fixed64 field. */
  public static int sizeFixed64Optional(ProtoFieldInfo field, long value) {
    return field.getTagSize() + CodedOutputStream.computeFixed64SizeNoTag(value);
  }

  /** Returns the size of a byte field when propagated to a fixed32. */
  public static int sizeByteAsFixed32(ProtoFieldInfo field, byte message) {
    return sizeFixed32(field, ((int) message) & 0xff);
  }

  /** Returns the size of a fixed32 field. */
  public static int sizeFixed32(ProtoFieldInfo field, int message) {
    if (message == 0L) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeFixed32SizeNoTag(message);
  }

  /** Returns the size of a bytes field. */
  public static int sizeBytes(ProtoFieldInfo field, byte[] message) {
    if (message.length == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeByteArraySizeNoTag(message);
  }

  /** Returns the size of a bytes field. */
  public static int sizeBytes(ProtoFieldInfo field, int length) {
    if (length == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeLengthDelimitedFieldSize(length);
  }

  /** Returns the size of a enum field. */
  // Assumes OTLP always defines the first item in an enum with number 0, which it does and will.
  public static int sizeEnum(ProtoFieldInfo field, ProtoEnumInfo enumValue) {
    int number = enumValue.getEnumNumber();
    if (number == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeEnumSizeNoTag(number);
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

  /** Returns the size of a string field. */
  public static int sizeString(
      ProtoFieldInfo field, @Nullable String value, MarshalerContext context) {
    if (value == null || value.isEmpty()) {
      return sizeBytes(field, 0);
    }
    if (context.marshalStringNoAllocation()) {
      int utf8Size = getUtf8Size(value, context);
      context.addSize(utf8Size);
      return sizeBytes(field, utf8Size);
    } else {
      byte[] valueUtf8 = toBytes(value);
      context.addData(valueUtf8);
      return sizeBytes(field, valueUtf8.length);
    }
  }

  /** Converts the string to utf8 bytes for encoding. */
  public static byte[] toBytes(@Nullable String value) {
    if (value == null || value.isEmpty()) {
      return EMPTY_BYTES;
    }
    return value.getBytes(StandardCharsets.UTF_8);
  }

  /** Returns the size of utf8 encoded string in bytes. */
  @SuppressWarnings("UnusedVariable")
  private static int getUtf8Size(String string, MarshalerContext context) {
    return getUtf8Size(string);
  }

  // Visible for testing
  static int getUtf8Size(String string) {
    return encodedUtf8Length(string);
  }

  // adapted from
  // https://github.com/protocolbuffers/protobuf/blob/b618f6750aed641a23d5f26fbbaf654668846d24/java/core/src/main/java/com/google/protobuf/Utf8.java#L217
  private static int encodedUtf8Length(String string) {
    // Warning to maintainers: this implementation is highly optimized.
    int utf16Length = string.length();
    int utf8Length = utf16Length;
    int i = 0;

    // This loop optimizes for pure ASCII.
    while (i < utf16Length && string.charAt(i) < 0x80) {
      i++;
    }

    // This loop optimizes for chars less than 0x800.
    for (; i < utf16Length; i++) {
      char c = string.charAt(i);
      if (c < 0x800) {
        utf8Length += ((0x7f - c) >>> 31); // branch free!
      } else {
        utf8Length += encodedUtf8LengthGeneral(string, i);
        break;
      }
    }

    if (utf8Length < utf16Length) {
      // Necessary and sufficient condition for overflow because of maximum 3x expansion
      throw new IllegalArgumentException(
          "UTF-8 length does not fit in int: " + (utf8Length + (1L << 32)));
    }

    return utf8Length;
  }

  // adapted from
  // https://github.com/protocolbuffers/protobuf/blob/b618f6750aed641a23d5f26fbbaf654668846d24/java/core/src/main/java/com/google/protobuf/Utf8.java#L247
  private static int encodedUtf8LengthGeneral(String string, int start) {
    int utf16Length = string.length();
    int utf8Length = 0;
    for (int i = start; i < utf16Length; i++) {
      char c = string.charAt(i);
      if (c < 0x800) {
        utf8Length += (0x7f - c) >>> 31; // branch free!
      } else {
        utf8Length += 2;
        if (Character.isSurrogate(c)) {
          // Check that we have a well-formed surrogate pair.
          if (Character.codePointAt(string, i) != c) {
            i++;
          } else {
            // invalid sequence
            // At this point we have accumulated 3 byes of length (2 in this method and 1 in caller)
            // for current character, reduce the length to 1 bytes as we are going to encode the
            // invalid character as ?
            utf8Length -= 2;
          }
        }
      }
    }

    return utf8Length;
  }

  /** Write utf8 encoded string to output stream. */
  @SuppressWarnings("UnusedVariable") // context argument is added for future use
  static void writeUtf8(
      CodedOutputStream output, String string, int utf8Length, MarshalerContext context)
      throws IOException {
    writeUtf8(output, string, utf8Length);
  }

  // Visible for testing
  @SuppressWarnings("UnusedVariable") // utf8Length argument is added for future use
  static void writeUtf8(CodedOutputStream output, String string, int utf8Length)
      throws IOException {
    encodeUtf8(output, string);
  }

  // encode utf8 the same way as length is computed in encodedUtf8Length
  // adapted from
  // https://github.com/protocolbuffers/protobuf/blob/b618f6750aed641a23d5f26fbbaf654668846d24/java/core/src/main/java/com/google/protobuf/Utf8.java#L1016
  private static void encodeUtf8(CodedOutputStream output, String in) throws IOException {
    int utf16Length = in.length();
    int i = 0;
    // Designed to take advantage of
    // https://wiki.openjdk.java.net/display/HotSpotInternals/RangeCheckElimination
    for (char c; i < utf16Length && (c = in.charAt(i)) < 0x80; i++) {
      output.write((byte) c);
    }
    if (i == utf16Length) {
      return;
    }

    for (char c; i < utf16Length; i++) {
      c = in.charAt(i);
      if (c < 0x80) {
        // 1 byte, 7 bits
        output.write((byte) c);
      } else if (c < 0x800) { // 11 bits, two UTF-8 bytes
        output.write((byte) ((0xF << 6) | (c >>> 6)));
        output.write((byte) (0x80 | (0x3F & c)));
      } else if (!Character.isSurrogate(c)) {
        // Maximum single-char code point is 0xFFFF, 16 bits, three UTF-8 bytes
        output.write((byte) ((0xF << 5) | (c >>> 12)));
        output.write((byte) (0x80 | (0x3F & (c >>> 6))));
        output.write((byte) (0x80 | (0x3F & c)));
      } else {
        // Minimum code point represented by a surrogate pair is 0x10000, 17 bits,
        // four UTF-8 bytes
        int codePoint = Character.codePointAt(in, i);
        if (codePoint != c) {
          output.write((byte) ((0xF << 4) | (codePoint >>> 18)));
          output.write((byte) (0x80 | (0x3F & (codePoint >>> 12))));
          output.write((byte) (0x80 | (0x3F & (codePoint >>> 6))));
          output.write((byte) (0x80 | (0x3F & codePoint)));
          i++;
        } else {
          // invalid sequence
          output.write((byte) '?');
        }
      }
    }
  }

  private MarshalerUtil() {}
}
