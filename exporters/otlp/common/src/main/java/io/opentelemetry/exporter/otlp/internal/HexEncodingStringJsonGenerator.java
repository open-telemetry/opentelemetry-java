/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import io.opentelemetry.api.internal.TemporaryBuffers;
import java.io.IOException;

public final class HexEncodingStringJsonGenerator extends JsonGeneratorDelegate {

  /**
   * Create a JSON generator which encodes byte arrays as case-insensitive hex-encoded strings.
   *
   * @param stringWriter the string write
   * @param jsonFactory the json factory
   * @return the generator
   */
  public static JsonGenerator create(SegmentedStringWriter stringWriter, JsonFactory jsonFactory) {
    final JsonGenerator delegate;
    try {
      delegate = jsonFactory.createGenerator(stringWriter);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create in-memory JsonGenerator, can't happen.", e);
    }
    return new HexEncodingStringJsonGenerator(delegate);
  }

  private HexEncodingStringJsonGenerator(JsonGenerator delegate) {
    super(delegate);
  }

  @Override
  public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len)
      throws IOException {
    writeString(bytesToHex(data, offset, len));
  }

  private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

  private static String bytesToHex(byte[] bytes, int offset, int len) {
    int hexLength = len * 2;
    char[] hexChars = TemporaryBuffers.chars(hexLength);
    for (int i = 0; i < len; i++) {
      int v = bytes[offset + i] & 0xFF;
      hexChars[i * 2] = HEX_ARRAY[v >>> 4];
      hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars, 0, hexLength);
  }
}
