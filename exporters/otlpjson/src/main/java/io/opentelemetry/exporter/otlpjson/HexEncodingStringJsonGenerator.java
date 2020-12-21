/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlpjson;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import java.io.IOException;

final class HexEncodingStringJsonGenerator extends JsonGeneratorDelegate {

  static final JsonFactory JSON_FACTORY = new JsonFactory();

  static JsonGenerator create(SegmentedStringWriter stringWriter) {
    final JsonGenerator delegate;
    try {
      delegate = JSON_FACTORY.createGenerator(stringWriter);
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
    char[] hexChars = new char[len * 2];
    for (int i = 0; i < len; i++) {
      int v = bytes[offset + i] & 0xFF;
      hexChars[i * 2] = HEX_ARRAY[v >>> 4];
      hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }
}
