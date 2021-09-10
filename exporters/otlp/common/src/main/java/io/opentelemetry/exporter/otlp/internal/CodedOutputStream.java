/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.otlp.internal.WireFormat.FIXED32_SIZE;
import static io.opentelemetry.exporter.otlp.internal.WireFormat.FIXED64_SIZE;
import static io.opentelemetry.exporter.otlp.internal.WireFormat.MAX_VARINT32_SIZE;
import static io.opentelemetry.exporter.otlp.internal.WireFormat.MAX_VARINT_SIZE;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Protobuf wire encoder.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
// Copied in and trimmed from
// https://github.com/protocolbuffers/protobuf/blob/master/java/core/src/main/java/com/google/protobuf/CodedOutputStream.java
//
// Differences
// - No support for Message/Lite
// - No support for ByteString or ByteBuffer
// - No support for message set extensions
// - No support for Unsafe
// - Allow resetting and use a ThreadLocal instance
//
@SuppressWarnings({"UngroupedOverloads", "InlineMeSuggester"})
abstract class CodedOutputStream {
  private static final Logger logger = Logger.getLogger(CodedOutputStream.class.getName());

  /** The buffer size used in {@link #newInstance(OutputStream)}. */
  private static final int DEFAULT_BUFFER_SIZE;

  static {
    int bufferSize = 50 * 1024;
    try {
      bufferSize = Integer.parseInt(System.getProperty("otel.experimental.otlp.buffer-size"));
    } catch (Throwable t) {
      // Ignore.
    }
    DEFAULT_BUFFER_SIZE = bufferSize;
  }

  private static final ThreadLocal<OutputStreamEncoder> THREAD_LOCAL_CODED_OUTPUT_STREAM =
      new ThreadLocal<>();

  /**
   * Create a new {@code CodedOutputStream} wrapping the given {@code OutputStream}.
   *
   * <p>NOTE: The provided {@link OutputStream} <strong>MUST NOT</strong> retain access or modify
   * the provided byte arrays. Doing so may result in corrupted data, which would be difficult to
   * debug.
   */
  static CodedOutputStream newInstance(final OutputStream output) {
    OutputStreamEncoder cos = THREAD_LOCAL_CODED_OUTPUT_STREAM.get();
    if (cos == null) {
      cos = new OutputStreamEncoder(output);
      THREAD_LOCAL_CODED_OUTPUT_STREAM.set(cos);
    } else {
      cos.reset(output);
    }
    return cos;
  }

  // Disallow construction outside of this class.
  private CodedOutputStream() {}

  // -----------------------------------------------------------------

  /** Encode and write a tag. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeTag(int fieldNumber, int wireType) throws IOException;

  /** Write an {@code int32} field, including tag, to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeInt32(int fieldNumber, int value) throws IOException;

  /** Write a {@code uint32} field, including tag, to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeUInt32(int fieldNumber, int value) throws IOException;

  /** Write a {@code sint32} field, including tag, to the stream. */
  final void writeSInt32(final int fieldNumber, final int value) throws IOException {
    writeUInt32(fieldNumber, encodeZigZag32(value));
  }

  /** Write a {@code fixed32} field, including tag, to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeFixed32(int fieldNumber, int value) throws IOException;

  /** Write an {@code sfixed32} field, including tag, to the stream. */
  final void writeSFixed32(final int fieldNumber, final int value) throws IOException {
    writeFixed32(fieldNumber, value);
  }

  /** Write an {@code int64} field, including tag, to the stream. */
  final void writeInt64(final int fieldNumber, final long value) throws IOException {
    writeUInt64(fieldNumber, value);
  }

  /** Write a {@code uint64} field, including tag, to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeUInt64(int fieldNumber, long value) throws IOException;

  /** Write an {@code sint64} field, including tag, to the stream. */
  final void writeSInt64(final int fieldNumber, final long value) throws IOException {
    writeUInt64(fieldNumber, encodeZigZag64(value));
  }

  /** Write a {@code fixed64} field, including tag, to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeFixed64(int fieldNumber, long value) throws IOException;

  /** Write an {@code sfixed64} field, including tag, to the stream. */
  final void writeSFixed64(final int fieldNumber, final long value) throws IOException {
    writeFixed64(fieldNumber, value);
  }

  /** Write a {@code float} field, including tag, to the stream. */
  final void writeFloat(final int fieldNumber, final float value) throws IOException {
    writeFixed32(fieldNumber, Float.floatToRawIntBits(value));
  }

  /** Write a {@code double} field, including tag, to the stream. */
  final void writeDouble(final int fieldNumber, final double value) throws IOException {
    writeFixed64(fieldNumber, Double.doubleToRawLongBits(value));
  }

  /** Write a {@code bool} field, including tag, to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeBool(int fieldNumber, boolean value) throws IOException;

  /**
   * Write an enum field, including tag, to the stream. The provided value is the numeric value used
   * to represent the enum value on the wire (not the enum ordinal value).
   */
  final void writeEnum(final int fieldNumber, final int value) throws IOException {
    writeInt32(fieldNumber, value);
  }

  /** Write a {@code string} field, including tag, to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeString(int fieldNumber, String value) throws IOException;

  /** Write a {@code bytes} field, including tag, to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeByteArray(int fieldNumber, byte[] value) throws IOException;

  /** Write a {@code bytes} field, including tag, to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeByteArray(int fieldNumber, byte[] value, int offset, int length)
      throws IOException;

  /** Write a single byte. */
  final void writeRawByte(final byte value) throws IOException {
    write(value);
  }

  /** Write a single byte, represented by an integer value. */
  final void writeRawByte(final int value) throws IOException {
    write((byte) value);
  }

  /** Write an array of bytes. */
  final void writeRawBytes(final byte[] value) throws IOException {
    write(value, 0, value.length);
  }

  /** Write part of an array of bytes. */
  final void writeRawBytes(final byte[] value, int offset, int length) throws IOException {
    write(value, offset, length);
  }

  // -----------------------------------------------------------------

  /** Write an {@code int32} field to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeInt32NoTag(final int value) throws IOException;

  /** Write a {@code uint32} field to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeUInt32NoTag(int value) throws IOException;

  /** Write a {@code sint32} field to the stream. */
  final void writeSInt32NoTag(final int value) throws IOException {
    writeUInt32NoTag(encodeZigZag32(value));
  }

  /** Write a {@code fixed32} field to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeFixed32NoTag(int value) throws IOException;

  /** Write a {@code sfixed32} field to the stream. */
  final void writeSFixed32NoTag(final int value) throws IOException {
    writeFixed32NoTag(value);
  }

  /** Write an {@code int64} field to the stream. */
  final void writeInt64NoTag(final long value) throws IOException {
    writeUInt64NoTag(value);
  }

  /** Write a {@code uint64} field to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeUInt64NoTag(long value) throws IOException;

  /** Write a {@code sint64} field to the stream. */
  final void writeSInt64NoTag(final long value) throws IOException {
    writeUInt64NoTag(encodeZigZag64(value));
  }

  /** Write a {@code fixed64} field to the stream. */
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeFixed64NoTag(long value) throws IOException;

  /** Write a {@code sfixed64} field to the stream. */
  final void writeSFixed64NoTag(final long value) throws IOException {
    writeFixed64NoTag(value);
  }

  /** Write a {@code float} field to the stream. */
  final void writeFloatNoTag(final float value) throws IOException {
    writeFixed32NoTag(Float.floatToRawIntBits(value));
  }

  /** Write a {@code double} field to the stream. */
  final void writeDoubleNoTag(final double value) throws IOException {
    writeFixed64NoTag(Double.doubleToRawLongBits(value));
  }

  /** Write a {@code bool} field to the stream. */
  final void writeBoolNoTag(final boolean value) throws IOException {
    write((byte) (value ? 1 : 0));
  }

  /**
   * Write an enum field to the stream. The provided value is the numeric value used to represent
   * the enum value on the wire (not the enum ordinal value).
   */
  final void writeEnumNoTag(final int value) throws IOException {
    writeInt32NoTag(value);
  }

  /** Write a {@code string} field to the stream. */
  // TODO(dweis): Document behavior on ill-formed UTF-16 input.
  // Abstract to avoid overhead of additional virtual method calls.
  abstract void writeStringNoTag(String value) throws IOException;

  /** Write a {@code bytes} field to the stream. */
  final void writeByteArrayNoTag(final byte[] value) throws IOException {
    writeByteArrayNoTag(value, 0, value.length);
  }

  // =================================================================

  abstract void write(byte value) throws IOException;

  abstract void write(byte[] value, int offset, int length) throws IOException;

  abstract void writeLazy(byte[] value, int offset, int length) throws IOException;

  // =================================================================
  // =================================================================

  /**
   * Compute the number of bytes that would be needed to encode an {@code int32} field, including
   * tag.
   */
  static int computeInt32Size(final int fieldNumber, final int value) {
    return computeTagSize(fieldNumber) + computeInt32SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code uint32} field, including
   * tag.
   */
  static int computeUInt32Size(final int fieldNumber, final int value) {
    return computeTagSize(fieldNumber) + computeUInt32SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode an {@code sint32} field, including
   * tag.
   */
  static int computeSInt32Size(final int fieldNumber, final int value) {
    return computeTagSize(fieldNumber) + computeSInt32SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code fixed32} field, including
   * tag.
   */
  static int computeFixed32Size(final int fieldNumber, final int value) {
    return computeTagSize(fieldNumber) + computeFixed32SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode an {@code sfixed32} field, including
   * tag.
   */
  static int computeSFixed32Size(final int fieldNumber, final int value) {
    return computeTagSize(fieldNumber) + computeSFixed32SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode an {@code int64} field, including
   * tag.
   */
  static int computeInt64Size(final int fieldNumber, final long value) {
    return computeTagSize(fieldNumber) + computeInt64SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code uint64} field, including
   * tag.
   */
  static int computeUInt64Size(final int fieldNumber, final long value) {
    return computeTagSize(fieldNumber) + computeUInt64SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode an {@code sint64} field, including
   * tag.
   */
  static int computeSInt64Size(final int fieldNumber, final long value) {
    return computeTagSize(fieldNumber) + computeSInt64SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code fixed64} field, including
   * tag.
   */
  static int computeFixed64Size(final int fieldNumber, final long value) {
    return computeTagSize(fieldNumber) + computeFixed64SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode an {@code sfixed64} field, including
   * tag.
   */
  static int computeSFixed64Size(final int fieldNumber, final long value) {
    return computeTagSize(fieldNumber) + computeSFixed64SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code float} field, including
   * tag.
   */
  static int computeFloatSize(final int fieldNumber, final float value) {
    return computeTagSize(fieldNumber) + computeFloatSizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code double} field, including
   * tag.
   */
  static int computeDoubleSize(final int fieldNumber, final double value) {
    return computeTagSize(fieldNumber) + computeDoubleSizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code bool} field, including tag.
   */
  static int computeBoolSize(final int fieldNumber, final boolean value) {
    return computeTagSize(fieldNumber) + computeBoolSizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode an enum field, including tag. The
   * provided value is the numeric value used to represent the enum value on the wire (not the enum
   * ordinal value).
   */
  static int computeEnumSize(final int fieldNumber, final int value) {
    return computeTagSize(fieldNumber) + computeEnumSizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code string} field, including
   * tag.
   */
  static int computeStringSize(final int fieldNumber, final String value) {
    return computeTagSize(fieldNumber) + computeStringSizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code bytes} field, including
   * tag.
   */
  static int computeByteArraySize(final int fieldNumber, final byte[] value) {
    return computeTagSize(fieldNumber) + computeByteArraySizeNoTag(value);
  }

  // -----------------------------------------------------------------

  /** Compute the number of bytes that would be needed to encode a tag. */
  static int computeTagSize(final int fieldNumber) {
    return computeUInt32SizeNoTag(WireFormat.makeTag(fieldNumber, 0));
  }

  /**
   * Compute the number of bytes that would be needed to encode an {@code int32} field, including
   * tag.
   */
  static int computeInt32SizeNoTag(final int value) {
    if (value >= 0) {
      return computeUInt32SizeNoTag(value);
    } else {
      // Must sign-extend.
      return MAX_VARINT_SIZE;
    }
  }

  /** Compute the number of bytes that would be needed to encode a {@code uint32} field. */
  static int computeUInt32SizeNoTag(final int value) {
    if ((value & (~0 << 7)) == 0) {
      return 1;
    }
    if ((value & (~0 << 14)) == 0) {
      return 2;
    }
    if ((value & (~0 << 21)) == 0) {
      return 3;
    }
    if ((value & (~0 << 28)) == 0) {
      return 4;
    }
    return 5;
  }

  /** Compute the number of bytes that would be needed to encode an {@code sint32} field. */
  static int computeSInt32SizeNoTag(final int value) {
    return computeUInt32SizeNoTag(encodeZigZag32(value));
  }

  /** Compute the number of bytes that would be needed to encode a {@code fixed32} field. */
  static int computeFixed32SizeNoTag(@SuppressWarnings("unused") final int unused) {
    return FIXED32_SIZE;
  }

  /** Compute the number of bytes that would be needed to encode an {@code sfixed32} field. */
  static int computeSFixed32SizeNoTag(@SuppressWarnings("unused") final int unused) {
    return FIXED32_SIZE;
  }

  /**
   * Compute the number of bytes that would be needed to encode an {@code int64} field, including
   * tag.
   */
  static int computeInt64SizeNoTag(final long value) {
    return computeUInt64SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code uint64} field, including
   * tag.
   */
  static int computeUInt64SizeNoTag(long value) {
    // handle two popular special cases up front ...
    if ((value & (~0L << 7)) == 0L) {
      return 1;
    }
    if (value < 0L) {
      return 10;
    }
    // ... leaving us with 8 remaining, which we can divide and conquer
    int n = 2;
    if ((value & (~0L << 35)) != 0L) {
      n += 4;
      value >>>= 28;
    }
    if ((value & (~0L << 21)) != 0L) {
      n += 2;
      value >>>= 14;
    }
    if ((value & (~0L << 14)) != 0L) {
      n += 1;
    }
    return n;
  }

  /** Compute the number of bytes that would be needed to encode an {@code sint64} field. */
  static int computeSInt64SizeNoTag(final long value) {
    return computeUInt64SizeNoTag(encodeZigZag64(value));
  }

  /** Compute the number of bytes that would be needed to encode a {@code fixed64} field. */
  static int computeFixed64SizeNoTag(@SuppressWarnings("unused") final long unused) {
    return FIXED64_SIZE;
  }

  /** Compute the number of bytes that would be needed to encode an {@code sfixed64} field. */
  static int computeSFixed64SizeNoTag(@SuppressWarnings("unused") final long unused) {
    return FIXED64_SIZE;
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code float} field, including
   * tag.
   */
  static int computeFloatSizeNoTag(@SuppressWarnings("unused") final float unused) {
    return FIXED32_SIZE;
  }

  /**
   * Compute the number of bytes that would be needed to encode a {@code double} field, including
   * tag.
   */
  static int computeDoubleSizeNoTag(@SuppressWarnings("unused") final double unused) {
    return FIXED64_SIZE;
  }

  /** Compute the number of bytes that would be needed to encode a {@code bool} field. */
  static int computeBoolSizeNoTag(@SuppressWarnings("unused") final boolean unused) {
    return 1;
  }

  /**
   * Compute the number of bytes that would be needed to encode an enum field. The provided value is
   * the numeric value used to represent the enum value on the wire (not the enum ordinal value).
   */
  static int computeEnumSizeNoTag(final int value) {
    return computeInt32SizeNoTag(value);
  }

  /** Compute the number of bytes that would be needed to encode a {@code string} field. */
  static int computeStringSizeNoTag(final String value) {
    int length;
    try {
      length = Utf8.encodedLength(value);
    } catch (Utf8.UnpairedSurrogateException e) {
      final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      length = bytes.length;
    }

    return computeLengthDelimitedFieldSize(length);
  }

  /** Compute the number of bytes that would be needed to encode a {@code bytes} field. */
  static int computeByteArraySizeNoTag(final byte[] value) {
    return computeLengthDelimitedFieldSize(value.length);
  }

  static int computeLengthDelimitedFieldSize(int fieldLength) {
    return computeUInt32SizeNoTag(fieldLength) + fieldLength;
  }

  /**
   * Encode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into values that can be
   * efficiently encoded with varint. (Otherwise, negative values must be sign-extended to 64 bits
   * to be varint encoded, thus always taking 10 bytes on the wire.)
   *
   * @param n A signed 32-bit integer.
   * @return An unsigned 32-bit integer, stored in a signed int because Java has no explicit
   *     unsigned support.
   */
  static int encodeZigZag32(final int n) {
    // Note:  the right-shift must be arithmetic
    return (n << 1) ^ (n >> 31);
  }

  /**
   * Encode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into values that can be
   * efficiently encoded with varint. (Otherwise, negative values must be sign-extended to 64 bits
   * to be varint encoded, thus always taking 10 bytes on the wire.)
   *
   * @param n A signed 64-bit integer.
   * @return An unsigned 64-bit integer, stored in a signed int because Java has no explicit
   *     unsigned support.
   */
  static long encodeZigZag64(final long n) {
    // Note:  the right-shift must be arithmetic
    return (n << 1) ^ (n >> 63);
  }

  // =================================================================

  /**
   * Flushes the stream and forces any buffered bytes to be written. This does not flush the
   * underlying OutputStream.
   */
  abstract void flush() throws IOException;

  /**
   * If writing to a flat array, return the space left in the array. Otherwise, throws {@code
   * UnsupportedOperationException}.
   */
  abstract int spaceLeft();

  /**
   * Verifies that {@link #spaceLeft()} returns zero. It's common to create a byte array that is
   * exactly big enough to hold a message, then write to it with a {@code CodedOutputStream}.
   * Calling {@code checkNoSpaceLeft()} after writing verifies that the message was actually as big
   * as expected, which can help catch bugs.
   */
  final void checkNoSpaceLeft() {
    if (spaceLeft() != 0) {
      throw new IllegalStateException("Did not write as much data as expected.");
    }
  }

  /**
   * If you create a CodedOutputStream around a simple flat array, you must not attempt to write
   * more bytes than the array has space. Otherwise, this exception will be thrown.
   */
  static class OutOfSpaceException extends IOException {
    private static final long serialVersionUID = -6947486886997889499L;

    private static final String MESSAGE =
        "CodedOutputStream was writing to a flat byte array and ran out of space.";

    OutOfSpaceException() {
      super(MESSAGE);
    }

    OutOfSpaceException(String explanationMessage) {
      super(MESSAGE + ": " + explanationMessage);
    }

    OutOfSpaceException(Throwable cause) {
      super(MESSAGE, cause);
    }

    OutOfSpaceException(String explanationMessage, Throwable cause) {
      super(MESSAGE + ": " + explanationMessage, cause);
    }
  }

  /**
   * Get the total number of bytes successfully written to this stream. The returned value is not
   * guaranteed to be accurate if exceptions have been found in the middle of writing.
   */
  abstract int getTotalBytesWritten();

  // =================================================================

  /** Write a {@code bytes} field to the stream. */
  abstract void writeByteArrayNoTag(final byte[] value, final int offset, final int length)
      throws IOException;

  final void inefficientWriteStringNoTag(String value, Utf8.UnpairedSurrogateException cause)
      throws IOException {
    logger.log(
        Level.WARNING,
        "Converting ill-formed UTF-16. Your Protocol Buffer will not round trip correctly!",
        cause);

    // Unfortunately there does not appear to be any way to tell Java to encode
    // UTF-8 directly into our buffer, so we have to let it create its own byte
    // array and then copy.
    final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    try {
      writeUInt32NoTag(bytes.length);
      writeLazy(bytes, 0, bytes.length);
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfSpaceException(e);
    } catch (OutOfSpaceException e) {
      throw e;
    }
  }

  // =================================================================

  /**
   * Encode and write a varint. {@code value} is treated as unsigned, so it won't be sign-extended
   * if negative.
   *
   * @deprecated use {@link #writeUInt32NoTag} instead.
   */
  @Deprecated
  final void writeRawVarint32(int value) throws IOException {
    writeUInt32NoTag(value);
  }

  /**
   * Encode and write a varint.
   *
   * @deprecated use {@link #writeUInt64NoTag} instead.
   */
  @Deprecated
  final void writeRawVarint64(long value) throws IOException {
    writeUInt64NoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a varint. {@code value} is treated
   * as unsigned, so it won't be sign-extended if negative.
   *
   * @deprecated use {@link #computeUInt32SizeNoTag(int)} instead.
   */
  @Deprecated
  static int computeRawVarint32Size(final int value) {
    return computeUInt32SizeNoTag(value);
  }

  /**
   * Compute the number of bytes that would be needed to encode a varint.
   *
   * @deprecated use {@link #computeUInt64SizeNoTag(long)} instead.
   */
  @Deprecated
  static int computeRawVarint64Size(long value) {
    return computeUInt64SizeNoTag(value);
  }

  /**
   * Write a little-endian 32-bit integer.
   *
   * @deprecated Use {@link #writeFixed32NoTag} instead.
   */
  @Deprecated
  final void writeRawLittleEndian32(final int value) throws IOException {
    writeFixed32NoTag(value);
  }

  /**
   * Write a little-endian 64-bit integer.
   *
   * @deprecated Use {@link #writeFixed64NoTag} instead.
   */
  @Deprecated
  final void writeRawLittleEndian64(final long value) throws IOException {
    writeFixed64NoTag(value);
  }

  // =================================================================

  /** Abstract base class for buffered encoders. */
  private abstract static class AbstractBufferedEncoder extends CodedOutputStream {
    final byte[] buffer;
    final int limit;
    int position;
    int totalBytesWritten;

    AbstractBufferedEncoder(int bufferSize) {
      this.buffer = new byte[bufferSize];
      this.limit = buffer.length;
    }

    @Override
    final int spaceLeft() {
      throw new UnsupportedOperationException(
          "spaceLeft() can only be called on CodedOutputStreams that are "
              + "writing to a flat array or ByteBuffer.");
    }

    @Override
    final int getTotalBytesWritten() {
      return totalBytesWritten;
    }

    /**
     * This method does not perform bounds checking on the array. Checking array bounds is the
     * responsibility of the caller.
     */
    final void buffer(byte value) {
      buffer[position++] = value;
      totalBytesWritten++;
    }

    /**
     * This method does not perform bounds checking on the array. Checking array bounds is the
     * responsibility of the caller.
     */
    final void bufferTag(final int fieldNumber, final int wireType) {
      bufferUInt32NoTag(WireFormat.makeTag(fieldNumber, wireType));
    }

    /**
     * This method does not perform bounds checking on the array. Checking array bounds is the
     * responsibility of the caller.
     */
    final void bufferInt32NoTag(final int value) {
      if (value >= 0) {
        bufferUInt32NoTag(value);
      } else {
        // Must sign-extend.
        bufferUInt64NoTag(value);
      }
    }

    /**
     * This method does not perform bounds checking on the array. Checking array bounds is the
     * responsibility of the caller.
     */
    final void bufferUInt32NoTag(int value) {
      while (true) {
        if ((value & ~0x7F) == 0) {
          buffer[position++] = (byte) value;
          totalBytesWritten++;
          return;
        } else {
          buffer[position++] = (byte) ((value & 0x7F) | 0x80);
          totalBytesWritten++;
          value >>>= 7;
        }
      }
    }

    /**
     * This method does not perform bounds checking on the array. Checking array bounds is the
     * responsibility of the caller.
     */
    final void bufferUInt64NoTag(long value) {
      while (true) {
        if ((value & ~0x7FL) == 0) {
          buffer[position++] = (byte) value;
          totalBytesWritten++;
          return;
        } else {
          buffer[position++] = (byte) (((int) value & 0x7F) | 0x80);
          totalBytesWritten++;
          value >>>= 7;
        }
      }
    }

    /**
     * This method does not perform bounds checking on the array. Checking array bounds is the
     * responsibility of the caller.
     */
    final void bufferFixed32NoTag(int value) {
      buffer[position++] = (byte) (value & 0xFF);
      buffer[position++] = (byte) ((value >> 8) & 0xFF);
      buffer[position++] = (byte) ((value >> 16) & 0xFF);
      buffer[position++] = (byte) ((value >> 24) & 0xFF);
      totalBytesWritten += FIXED32_SIZE;
    }

    /**
     * This method does not perform bounds checking on the array. Checking array bounds is the
     * responsibility of the caller.
     */
    final void bufferFixed64NoTag(long value) {
      buffer[position++] = (byte) (value & 0xFF);
      buffer[position++] = (byte) ((value >> 8) & 0xFF);
      buffer[position++] = (byte) ((value >> 16) & 0xFF);
      buffer[position++] = (byte) ((value >> 24) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 32) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 40) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 48) & 0xFF);
      buffer[position++] = (byte) ((int) (value >> 56) & 0xFF);
      totalBytesWritten += FIXED64_SIZE;
    }
  }

  /**
   * An {@link CodedOutputStream} that decorates an {@link OutputStream}. It performs internal
   * buffering to optimize writes to the {@link OutputStream}.
   */
  private static final class OutputStreamEncoder extends AbstractBufferedEncoder {
    private OutputStream out;

    OutputStreamEncoder(OutputStream out) {
      super(DEFAULT_BUFFER_SIZE);
      this.out = out;
    }

    void reset(OutputStream out) {
      this.out = out;
      position = 0;
      totalBytesWritten = 0;
    }

    @Override
    void writeTag(final int fieldNumber, final int wireType) throws IOException {
      writeUInt32NoTag(WireFormat.makeTag(fieldNumber, wireType));
    }

    @Override
    void writeInt32(final int fieldNumber, final int value) throws IOException {
      flushIfNotAvailable(MAX_VARINT_SIZE * 2);
      bufferTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
      bufferInt32NoTag(value);
    }

    @Override
    void writeUInt32(final int fieldNumber, final int value) throws IOException {
      flushIfNotAvailable(MAX_VARINT_SIZE * 2);
      bufferTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
      bufferUInt32NoTag(value);
    }

    @Override
    void writeFixed32(final int fieldNumber, final int value) throws IOException {
      flushIfNotAvailable(MAX_VARINT_SIZE + FIXED32_SIZE);
      bufferTag(fieldNumber, WireFormat.WIRETYPE_FIXED32);
      bufferFixed32NoTag(value);
    }

    @Override
    void writeUInt64(final int fieldNumber, final long value) throws IOException {
      flushIfNotAvailable(MAX_VARINT_SIZE * 2);
      bufferTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
      bufferUInt64NoTag(value);
    }

    @Override
    void writeFixed64(final int fieldNumber, final long value) throws IOException {
      flushIfNotAvailable(MAX_VARINT_SIZE + FIXED64_SIZE);
      bufferTag(fieldNumber, WireFormat.WIRETYPE_FIXED64);
      bufferFixed64NoTag(value);
    }

    @Override
    void writeBool(final int fieldNumber, final boolean value) throws IOException {
      flushIfNotAvailable(MAX_VARINT_SIZE + 1);
      bufferTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
      buffer((byte) (value ? 1 : 0));
    }

    @Override
    void writeString(final int fieldNumber, final String value) throws IOException {
      writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      writeStringNoTag(value);
    }

    @Override
    void writeByteArray(final int fieldNumber, final byte[] value) throws IOException {
      writeByteArray(fieldNumber, value, 0, value.length);
    }

    @Override
    void writeByteArray(
        final int fieldNumber, final byte[] value, final int offset, final int length)
        throws IOException {
      writeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      writeByteArrayNoTag(value, offset, length);
    }

    @Override
    void writeByteArrayNoTag(final byte[] value, int offset, int length) throws IOException {
      writeUInt32NoTag(length);
      write(value, offset, length);
    }

    @Override
    void write(byte value) throws IOException {
      if (position == limit) {
        doFlush();
      }

      buffer(value);
    }

    @Override
    void writeInt32NoTag(int value) throws IOException {
      if (value >= 0) {
        writeUInt32NoTag(value);
      } else {
        // Must sign-extend.
        writeUInt64NoTag(value);
      }
    }

    @Override
    void writeUInt32NoTag(int value) throws IOException {
      flushIfNotAvailable(MAX_VARINT32_SIZE);
      bufferUInt32NoTag(value);
    }

    @Override
    void writeFixed32NoTag(final int value) throws IOException {
      flushIfNotAvailable(FIXED32_SIZE);
      bufferFixed32NoTag(value);
    }

    @Override
    void writeUInt64NoTag(long value) throws IOException {
      flushIfNotAvailable(MAX_VARINT_SIZE);
      bufferUInt64NoTag(value);
    }

    @Override
    void writeFixed64NoTag(final long value) throws IOException {
      flushIfNotAvailable(FIXED64_SIZE);
      bufferFixed64NoTag(value);
    }

    @Override
    void writeStringNoTag(String value) throws IOException {
      try {
        // UTF-8 byte length of the string is at least its UTF-16 code unit length (value.length()),
        // and at most 3 times of it. We take advantage of this in both branches below.
        final int maxLength = value.length() * Utf8.MAX_BYTES_PER_CHAR;
        final int maxLengthVarIntSize = computeUInt32SizeNoTag(maxLength);

        // If we are streaming and the potential length is too big to fit in our buffer, we take the
        // slower path.
        if (maxLengthVarIntSize + maxLength > limit) {
          // Allocate a byte[] that we know can fit the string and encode into it. String.getBytes()
          // does the same internally and then does *another copy* to return a byte[] of exactly the
          // right size. We can skip that copy and just writeRawBytes up to the actualLength of the
          // UTF-8 encoded bytes.
          final byte[] encodedBytes = new byte[maxLength];
          int actualLength = Utf8.encode(value, encodedBytes, 0, maxLength);
          writeUInt32NoTag(actualLength);
          writeLazy(encodedBytes, 0, actualLength);
          return;
        }

        // Fast path: we have enough space available in our buffer for the string...
        if (maxLengthVarIntSize + maxLength > limit - position) {
          // Flush to free up space.
          doFlush();
        }

        // Optimize for the case where we know this length results in a constant varint length as
        // this saves a pass for measuring the length of the string.
        final int minLengthVarIntSize = computeUInt32SizeNoTag(value.length());
        int oldPosition = position;
        final int length;
        try {
          if (minLengthVarIntSize == maxLengthVarIntSize) {
            position = oldPosition + minLengthVarIntSize;
            int newPosition = Utf8.encode(value, buffer, position, limit - position);
            // Since this class is stateful and tracks the position, we rewind and store the
            // state, prepend the length, then reset it back to the end of the string.
            position = oldPosition;
            length = newPosition - oldPosition - minLengthVarIntSize;
            bufferUInt32NoTag(length);
            position = newPosition;
          } else {
            length = Utf8.encodedLength(value);
            bufferUInt32NoTag(length);
            position = Utf8.encode(value, buffer, position, length);
          }
          totalBytesWritten += length;
        } catch (Utf8.UnpairedSurrogateException e) {
          // Be extra careful and restore the original position for retrying the write with the
          // less efficient path.
          totalBytesWritten -= position - oldPosition;
          position = oldPosition;
          throw e;
        } catch (ArrayIndexOutOfBoundsException e) {
          throw new OutOfSpaceException(e);
        }
      } catch (Utf8.UnpairedSurrogateException e) {
        inefficientWriteStringNoTag(value, e);
      }
    }

    @Override
    void flush() throws IOException {
      if (position > 0) {
        // Flush the buffer.
        doFlush();
      }
    }

    @Override
    void write(byte[] value, int offset, int length) throws IOException {
      if (limit - position >= length) {
        // We have room in the current buffer.
        System.arraycopy(value, offset, buffer, position, length);
        position += length;
        totalBytesWritten += length;
      } else {
        // Write extends past current buffer.  Fill the rest of this buffer and
        // flush.
        final int bytesWritten = limit - position;
        System.arraycopy(value, offset, buffer, position, bytesWritten);
        offset += bytesWritten;
        length -= bytesWritten;
        position = limit;
        totalBytesWritten += bytesWritten;
        doFlush();

        // Now deal with the rest.
        // Since we have an output stream, this is our buffer
        // and buffer offset == 0
        if (length <= limit) {
          // Fits in new buffer.
          System.arraycopy(value, offset, buffer, 0, length);
          position = length;
        } else {
          // Write is very big.  Let's do it all at once.
          out.write(value, offset, length);
        }
        totalBytesWritten += length;
      }
    }

    @Override
    void writeLazy(byte[] value, int offset, int length) throws IOException {
      write(value, offset, length);
    }

    private void flushIfNotAvailable(int requiredSize) throws IOException {
      if (limit - position < requiredSize) {
        doFlush();
      }
    }

    private void doFlush() throws IOException {
      out.write(buffer, 0, position);
      position = 0;
    }
  }
}
