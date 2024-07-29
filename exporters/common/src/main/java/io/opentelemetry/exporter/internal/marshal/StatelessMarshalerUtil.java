/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
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
public final class StatelessMarshalerUtil {
  private static final MarshalerContext.Key GROUPER_KEY = MarshalerContext.key();
  private static final MarshalerContext.Key ATTRIBUTES_SIZE_CALCULATOR_KEY = MarshalerContext.key();

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

  /**
   * Returns the size of a string field. This method adds elements to context, use together with
   * {@link Serializer#serializeStringWithContext(ProtoFieldInfo, String, MarshalerContext)}.
   */
  public static int sizeStringWithContext(
      ProtoFieldInfo field, @Nullable String value, MarshalerContext context) {
    if (value == null || value.isEmpty()) {
      return sizeBytes(field, 0);
    }
    if (context.marshalStringNoAllocation()) {
      int utf8Size = getUtf8Size(value, context);
      context.addSize(utf8Size);
      return sizeBytes(field, utf8Size);
    } else {
      byte[] valueUtf8 = MarshalerUtil.toBytes(value);
      context.addData(valueUtf8);
      return sizeBytes(field, valueUtf8.length);
    }
  }

  /** Returns the size of a bytes field. */
  private static int sizeBytes(ProtoFieldInfo field, int length) {
    if (length == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeLengthDelimitedFieldSize(length);
  }

  /**
   * Returns the size of a repeated message field. This method adds elements to context, use
   * together with {@link Serializer#serializeRepeatedMessageWithContext(ProtoFieldInfo, List,
   * StatelessMarshaler, MarshalerContext)}.
   */
  public static <T> int sizeRepeatedMessageWithContext(
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

  /**
   * Returns the size of a repeated message field. This method adds elements to context, use
   * together with {@link Serializer#serializeRepeatedMessageWithContext(ProtoFieldInfo, Collection,
   * StatelessMarshaler, MarshalerContext, MarshalerContext.Key)}.
   */
  @SuppressWarnings("unchecked")
  public static <T> int sizeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      Collection<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context,
      MarshalerContext.Key key) {
    if (messages instanceof List) {
      return sizeRepeatedMessageWithContext(field, (List<T>) messages, marshaler, context);
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

  /**
   * Returns the size of a repeated message field. This method adds elements to context, use
   * together with {@link Serializer#serializeRepeatedMessageWithContext(ProtoFieldInfo, Map,
   * StatelessMarshaler2, MarshalerContext, MarshalerContext.Key)}.
   */
  public static <K, V> int sizeRepeatedMessageWithContext(
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

  /**
   * Returns the size of a repeated message field. This method adds elements to context, use
   * together with {@link Serializer#serializeRepeatedMessageWithContext(ProtoFieldInfo, Attributes,
   * StatelessMarshaler2, MarshalerContext)}.
   */
  public static int sizeRepeatedMessageWithContext(
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

  /**
   * Returns the size of a message field. This method adds elements to context, use together with
   * {@link Serializer#serializeMessageWithContext(ProtoFieldInfo, Object, StatelessMarshaler,
   * MarshalerContext)}.
   */
  public static <T> int sizeMessageWithContext(
      ProtoFieldInfo field, T element, StatelessMarshaler<T> marshaler, MarshalerContext context) {
    int sizeIndex = context.addSize();
    int fieldSize = marshaler.getBinarySerializedSize(element, context);
    int size = field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    context.setSize(sizeIndex, fieldSize);
    return size;
  }

  /**
   * Returns the size of a message field. This method adds elements to context, use together with
   * {@link Serializer#serializeMessageWithContext(ProtoFieldInfo, Object, Object,
   * StatelessMarshaler2, MarshalerContext)}.
   */
  public static <K, V> int sizeMessageWithContext(
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

  /** Returns the size of utf8 encoded string in bytes. */
  private static int getUtf8Size(String string, MarshalerContext context) {
    return getUtf8Size(string, context.marshalStringUnsafe());
  }

  // Visible for testing
  static int getUtf8Size(String string, boolean useUnsafe) {
    if (useUnsafe && UnsafeString.isAvailable() && UnsafeString.isLatin1(string)) {
      byte[] bytes = UnsafeString.getBytes(string);
      // latin1 bytes with negative value (most significant bit set) are encoded as 2 bytes in utf8
      return string.length() + countNegative(bytes);
    }

    return encodedUtf8Length(string);
  }

  // Inner loop can process at most 8 * 255 bytes without overflowing counter. To process more bytes
  // inner loop has to be run multiple times.
  private static final int MAX_INNER_LOOP_SIZE = 8 * 255;
  // mask that selects only the most significant bit in every byte of the long
  private static final long MOST_SIGNIFICANT_BIT_MASK = 0x8080808080808080L;

  /** Returns the count of bytes with negative value. */
  private static int countNegative(byte[] bytes) {
    int count = 0;
    int offset = 0;
    // We are processing one long (8 bytes) at a time. In the inner loop we are keeping counts in a
    // long where each byte in the long is a separate counter. Due to this the inner loop can
    // process a maximum of 8*255 bytes at a time without overflow.
    for (int i = 1; i <= bytes.length / MAX_INNER_LOOP_SIZE + 1; i++) {
      long tmp = 0; // each byte in this long is a separate counter
      int limit = Math.min(i * MAX_INNER_LOOP_SIZE, bytes.length & ~7);
      for (; offset < limit; offset += 8) {
        long value = UnsafeString.getLong(bytes, offset);
        // Mask the value keeping only the most significant bit in each byte and then shift this bit
        // to the position of the least significant bit in each byte. If the input byte was not
        // negative then after this transformation it will be zero, if it was negative then it will
        // be one.
        tmp += (value & MOST_SIGNIFICANT_BIT_MASK) >>> 7;
      }
      // sum up counts
      if (tmp != 0) {
        for (int j = 0; j < 8; j++) {
          count += (int) (tmp & 0xff);
          tmp = tmp >>> 8;
        }
      }
    }

    // Handle remaining bytes. Previous loop processes 8 bytes a time, if the input size is not
    // divisible with 8 the remaining bytes are handled here.
    for (int i = offset; i < bytes.length; i++) {
      // same as if (bytes[i] < 0) count++;
      count += bytes[i] >>> 31;
    }
    return count;
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
    writeUtf8(output, string, utf8Length, context.marshalStringUnsafe());
  }

  // Visible for testing
  @SuppressWarnings("UnusedVariable") // utf8Length argument is added for future use
  static void writeUtf8(CodedOutputStream output, String string, int utf8Length, boolean useUnsafe)
      throws IOException {
    // if the length of the latin1 string and the utf8 output are the same then the string must be
    // composed of only 7bit characters and can be directly copied to the output
    if (useUnsafe
        && UnsafeString.isAvailable()
        && string.length() == utf8Length
        && UnsafeString.isLatin1(string)) {
      byte[] bytes = UnsafeString.getBytes(string);
      output.write(bytes, 0, bytes.length);
    } else {
      encodeUtf8(output, string);
    }
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

  private StatelessMarshalerUtil() {}
}
