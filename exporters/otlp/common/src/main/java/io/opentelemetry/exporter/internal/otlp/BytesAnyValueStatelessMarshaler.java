/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.Value;
import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import java.io.IOException;
import java.nio.ByteBuffer;

/** See {@link BytesAnyValueMarshaler}. */
final class BytesAnyValueStatelessMarshaler implements StatelessMarshaler<Value<ByteBuffer>> {
  static final BytesAnyValueStatelessMarshaler INSTANCE = new BytesAnyValueStatelessMarshaler();

  private BytesAnyValueStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, Value<ByteBuffer> value, MarshalerContext context)
      throws IOException {
    byte[] bytes = context.getData(byte[].class);
    output.writeBytes(AnyValue.BYTES_VALUE, bytes);
  }

  @Override
  public int getBinarySerializedSize(Value<ByteBuffer> value, MarshalerContext context) {
    ByteBuffer buf = value.getValue();
    byte[] bytes = new byte[buf.remaining()];
    // getValue() above returns a new ByteBuffer, so mutating its position here is safe
    buf.get(bytes);
    context.addData(bytes);
    return AnyValue.BYTES_VALUE.getTagSize() + CodedOutputStream.computeByteArraySizeNoTag(bytes);
  }
}
