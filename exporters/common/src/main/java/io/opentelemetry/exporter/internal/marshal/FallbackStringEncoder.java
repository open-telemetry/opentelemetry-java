/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;

/**
 * Fallback StringEncoder implementation using standard Java string operations.
 *
 * <p>This implementation works on all Java versions and provides correct UTF-8 handling.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class FallbackStringEncoder implements StringEncoder {

  FallbackStringEncoder() {}

  @Override
  public int getUtf8Size(String string) {
    return encodedUtf8Length(string);
  }

  @Override
  public void writeUtf8(CodedOutputStream output, String string, int utf8Length)
      throws IOException {
    encodeUtf8(output, string);
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
}
