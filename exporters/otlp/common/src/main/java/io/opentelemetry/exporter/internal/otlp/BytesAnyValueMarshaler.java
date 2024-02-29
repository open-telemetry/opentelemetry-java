/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import java.io.IOException;
import java.nio.ByteBuffer;

final class BytesAnyValueMarshaler extends MarshalerWithSize {

  private final byte[] value;

  private BytesAnyValueMarshaler(byte[] value) {
    super(calculateSize(value));
    this.value = value;
  }

  static MarshalerWithSize create(ByteBuffer value) {
    byte[] bytes = new byte[value.remaining()];
    value.get(bytes);
    return new BytesAnyValueMarshaler(bytes);
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    // Do not call serialize* method because we always have to write the message tag even if the
    // value is empty since it's a oneof.
    output.writeBytes(AnyValue.BYTES_VALUE, value);
  }

  private static int calculateSize(byte[] value) {
    return AnyValue.BYTES_VALUE.getTagSize() + CodedOutputStream.computeByteArraySizeNoTag(value);
  }
}
