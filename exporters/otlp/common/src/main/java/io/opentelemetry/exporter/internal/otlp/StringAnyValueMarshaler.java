/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import java.io.IOException;

/**
 * A Marshaler of string-valued {@link AnyValue}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class StringAnyValueMarshaler extends MarshalerWithSize {

  private final byte[] valueUtf8;

  private StringAnyValueMarshaler(byte[] valueUtf8) {
    super(calculateSize(valueUtf8));
    this.valueUtf8 = valueUtf8;
  }

  static MarshalerWithSize create(String value) {
    return new StringAnyValueMarshaler(MarshalerUtil.toBytes(value));
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.writeString(AnyValue.STRING_VALUE, valueUtf8);
  }

  private static int calculateSize(byte[] valueUtf8) {
    return AnyValue.STRING_VALUE.getTagSize()
        + CodedOutputStream.computeByteArraySizeNoTag(valueUtf8);
  }
}
