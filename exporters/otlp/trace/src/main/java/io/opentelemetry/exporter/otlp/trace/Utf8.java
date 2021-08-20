/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static java.lang.Character.MAX_SURROGATE;
import static java.lang.Character.MIN_SUPPLEMENTARY_CODE_POINT;
import static java.lang.Character.MIN_SURROGATE;
import static java.lang.Character.isSurrogatePair;
import static java.lang.Character.toCodePoint;

// Copied from
// https://github.com/protocolbuffers/protobuf/blob/master/java/core/src/main/java/com/google/protobuf/Utf8.java
//
// Removed support for ByteBuffer, decoding, and Unsafe.
//
// Unneeded lines of code are deleted as is, without any modifications otherwise.
final class Utf8 {

  /**
   * UTF-8 is a runtime hot spot so we attempt to provide heavily optimized implementations
   * depending on what is available on the platform. The processor is the platform-optimized
   * delegate for which all methods are delegated directly to.
   */
  private static final Processor processor = new SafeProcessor();

  /**
   * Maximum number of bytes per Java UTF-16 char in UTF-8.
   *
   * @see java.nio.charset.CharsetEncoder#maxBytesPerChar()
   */
  static final int MAX_BYTES_PER_CHAR = 3;

  // These UTF-8 handling methods are copied from Guava's Utf8 class with a modification to throw
  // a protocol buffer local exception. This exception is then caught in CodedOutputStream so it can
  // fallback to more lenient behavior.

  static class UnpairedSurrogateException extends IllegalArgumentException {

    private static final long serialVersionUID = 3903874482406618982L;

    UnpairedSurrogateException(int index, int length) {
      super("Unpaired surrogate at index " + index + " of " + length);
    }
  }

  /**
   * Returns the number of bytes in the UTF-8-encoded form of {@code sequence}. For a string, this
   * method is equivalent to {@code string.getBytes(UTF_8).length}, but is more efficient in both
   * time and space.
   *
   * @throws IllegalArgumentException if {@code sequence} contains ill-formed UTF-16 (unpaired
   *     surrogates)
   */
  static int encodedLength(CharSequence sequence) {
    // Warning to maintainers: this implementation is highly optimized.
    int utf16Length = sequence.length();
    int utf8Length = utf16Length;
    int i = 0;

    // This loop optimizes for pure ASCII.
    while (i < utf16Length && sequence.charAt(i) < 0x80) {
      i++;
    }

    // This loop optimizes for chars less than 0x800.
    for (; i < utf16Length; i++) {
      char c = sequence.charAt(i);
      if (c < 0x800) {
        utf8Length += ((0x7f - c) >>> 31); // branch free!
      } else {
        utf8Length += encodedLengthGeneral(sequence, i);
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

  private static int encodedLengthGeneral(CharSequence sequence, int start) {
    int utf16Length = sequence.length();
    int utf8Length = 0;
    for (int i = start; i < utf16Length; i++) {
      char c = sequence.charAt(i);
      if (c < 0x800) {
        utf8Length += (0x7f - c) >>> 31; // branch free!
      } else {
        utf8Length += 2;
        // jdk7+: if (Character.isSurrogate(c)) {
        if (MIN_SURROGATE <= c && c <= MAX_SURROGATE) {
          // Check that we have a well-formed surrogate pair.
          int cp = Character.codePointAt(sequence, i);
          if (cp < MIN_SUPPLEMENTARY_CODE_POINT) {
            throw new UnpairedSurrogateException(i, utf16Length);
          }
          i++;
        }
      }
    }
    return utf8Length;
  }

  static int encode(CharSequence in, byte[] out, int offset, int length) {
    return processor.encodeUtf8(in, out, offset, length);
  }
  // End Guava UTF-8 methods.

  /** A processor of UTF-8 strings, providing methods for checking validity and encoding. */
  // TODO(nathanmittler): Add support for Memory/MemoryBlock on Android.
  abstract static class Processor {

    abstract int encodeUtf8(CharSequence in, byte[] out, int offset, int length);
  }

  /** {@link Processor} implementation that does not use any {@code sun.misc.Unsafe} methods. */
  static final class SafeProcessor extends Processor {

    @Override
    int encodeUtf8(CharSequence in, byte[] out, int offset, int length) {
      int utf16Length = in.length();
      int j = offset;
      int i = 0;
      int limit = offset + length;
      // Designed to take advantage of
      // https://wiki.openjdk.java.net/display/HotSpotInternals/RangeCheckElimination
      for (char c; i < utf16Length && i + j < limit && (c = in.charAt(i)) < 0x80; i++) {
        out[j + i] = (byte) c;
      }
      if (i == utf16Length) {
        return j + utf16Length;
      }
      j += i;
      for (char c; i < utf16Length; i++) {
        c = in.charAt(i);
        if (c < 0x80 && j < limit) {
          out[j++] = (byte) c;
        } else if (c < 0x800 && j <= limit - 2) { // 11 bits, two UTF-8 bytes
          out[j++] = (byte) ((0xF << 6) | (c >>> 6));
          out[j++] = (byte) (0x80 | (0x3F & c));
        } else if ((c < MIN_SURROGATE || MAX_SURROGATE < c) && j <= limit - 3) {
          // Maximum single-char code point is 0xFFFF, 16 bits, three UTF-8 bytes
          out[j++] = (byte) ((0xF << 5) | (c >>> 12));
          out[j++] = (byte) (0x80 | (0x3F & (c >>> 6)));
          out[j++] = (byte) (0x80 | (0x3F & c));
        } else if (j <= limit - 4) {
          // Minimum code point represented by a surrogate pair is 0x10000, 17 bits,
          // four UTF-8 bytes
          final char low;
          if (i + 1 == in.length() || !isSurrogatePair(c, (low = in.charAt(++i)))) {
            throw new UnpairedSurrogateException((i - 1), utf16Length);
          }
          int codePoint = toCodePoint(c, low);
          out[j++] = (byte) ((0xF << 4) | (codePoint >>> 18));
          out[j++] = (byte) (0x80 | (0x3F & (codePoint >>> 12)));
          out[j++] = (byte) (0x80 | (0x3F & (codePoint >>> 6)));
          out[j++] = (byte) (0x80 | (0x3F & codePoint));
        } else {
          // If we are surrogates and we're not a surrogate pair, always throw an
          // UnpairedSurrogateException instead of an ArrayOutOfBoundsException.
          if ((MIN_SURROGATE <= c && c <= MAX_SURROGATE)
              && (i + 1 == in.length() || !isSurrogatePair(c, in.charAt(i + 1)))) {
            throw new UnpairedSurrogateException(i, utf16Length);
          }
          throw new ArrayIndexOutOfBoundsException("Failed writing " + c + " at index " + j);
        }
      }
      return j;
    }
  }

  private Utf8() {}
}
