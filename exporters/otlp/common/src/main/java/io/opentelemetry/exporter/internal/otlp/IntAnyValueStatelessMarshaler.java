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

/** See {@link IntAnyValueMarshaler}. */
final class IntAnyValueStatelessMarshaler implements StatelessMarshaler<Long> {
  static final IntAnyValueStatelessMarshaler INSTANCE = new IntAnyValueStatelessMarshaler();

  @Override
  public void writeTo(Serializer output, Long value, MarshalerContext context) throws IOException {
    output.writeInt64(AnyValue.INT_VALUE, value);
  }

  @Override
  public int getBinarySerializedSize(Long value, MarshalerContext context) {
    return AnyValue.INT_VALUE.getTagSize() + CodedOutputStream.computeInt64SizeNoTag(value);
  }
}
