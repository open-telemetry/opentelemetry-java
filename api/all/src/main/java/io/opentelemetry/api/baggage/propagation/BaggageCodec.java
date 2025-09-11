/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Note: This class is based on code from Apache Commons Codec. It is comprised of code from these
 * classes:
 *
 * <ul>
 *   <li><a
 *       href="https://github.com/apache/commons-codec/blob/482df6cabfb288acb6ab3e4a732fdb93aecfa7c2/src/main/java/org/apache/commons/codec/net/URLCodec.java">org.apache.commons.codec.net.URLCodec</a>
 *   <li><a
 *       href="https://github.com/apache/commons-codec/blob/482df6cabfb288acb6ab3e4a732fdb93aecfa7c2/src/main/java/org/apache/commons/codec/net/Utils.java">org.apache.commons.codec.net.Utils</a>
 * </ul>
 *
 * <p>Implements baggage-octet decoding in accordance with th <a
 * href="https://w3c.github.io/baggage/#definition">Baggage header content</a> specification. All
 * US-ASCII characters excluding CTLs, whitespace, DQUOTE, comma, semicolon and backslash are
 * encoded in `www-form-urlencoded` encoding scheme.
 */
class BaggageCodec {

  private static final byte ESCAPE_CHAR = '%';
  private static final int RADIX = 16;

  private BaggageCodec() {}

  /**
   * Decodes an array of URL safe 7-bit characters into an array of original bytes. Escaped
   * characters are converted back to their original representation.
   *
   * @param bytes array of URL safe characters
   * @return array of original bytes
   */
  private static byte[] decode(byte[] bytes) {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    for (int i = 0; i < bytes.length; i++) {
      int b = bytes[i];
      if (b == ESCAPE_CHAR) {
        if (i + 1 >= bytes.length) {
          return buffer.toByteArray();
        }
        int u = digit16(bytes[++i]);

        if (i + 1 >= bytes.length) {
          return buffer.toByteArray();
        }
        int l = digit16(bytes[++i]);

        buffer.write((char) ((u << 4) + l));
      } else {
        buffer.write(b);
      }
    }
    return buffer.toByteArray();
  }

  /**
   * Decodes an array of URL safe 7-bit characters into an array of original bytes. Escaped
   * characters are converted back to their original representation.
   *
   * @param value string of URL safe characters
   * @param charset encoding of given string
   * @return decoded value
   */
  static String decode(String value, Charset charset) {
    byte[] bytes = decode(value.getBytes(StandardCharsets.US_ASCII));
    return new String(bytes, charset);
  }

  /**
   * Returns the numeric value of the character {@code b} in radix 16.
   *
   * @param b The byte to be converted.
   * @return The numeric value represented by the character in radix 16.
   */
  private static int digit16(byte b) {
    int i = Character.digit((char) b, RADIX);
    if (i == -1) {
      throw new IllegalArgumentException( // FIXME
          "Invalid URL encoding: not a valid digit (radix " + RADIX + "): " + b);
    }
    return i;
  }
}
