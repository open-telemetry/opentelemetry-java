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

/** See {@link BoolAnyValueMarshaler}. */
final class BoolAnyValueStatelessMarshaler implements StatelessMarshaler<Boolean> {
  static final BoolAnyValueStatelessMarshaler INSTANCE = new BoolAnyValueStatelessMarshaler();

  private BoolAnyValueStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, Boolean value, MarshalerContext context)
      throws IOException {
    output.writeBool(AnyValue.BOOL_VALUE, value);
  }

  @Override
  public int getBinarySerializedSize(Boolean value, MarshalerContext context) {
    return AnyValue.BOOL_VALUE.getTagSize() + CodedOutputStream.computeBoolSizeNoTag(value);
  }
}
