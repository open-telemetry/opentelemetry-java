/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import java.io.IOException;
import java.nio.ByteBuffer;

/** See {@link BytesAnyValueMarshaler}. */
final class BytesAnyValueStatelessMarshaler implements StatelessMarshaler<ByteBuffer> {
  static final BytesAnyValueStatelessMarshaler INSTANCE = new BytesAnyValueStatelessMarshaler();

  private BytesAnyValueStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, ByteBuffer value, MarshalerContext context)
      throws IOException {
    byte[] bytes = context.getData(byte[].class);
    output.writeBytes(AnyValue.BYTES_VALUE, bytes);
  }

  @Override
  public int getBinarySerializedSize(ByteBuffer value, MarshalerContext context) {
    byte[] bytes = new byte[value.remaining()];
    value.get(bytes);
    context.addData(bytes);
    return AnyValue.BYTES_VALUE.getTagSize() + CodedOutputStream.computeByteArraySizeNoTag(bytes);
  }
}
