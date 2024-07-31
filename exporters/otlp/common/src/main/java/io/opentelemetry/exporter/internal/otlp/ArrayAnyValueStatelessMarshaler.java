/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.Value;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.common.v1.internal.ArrayValue;
import java.io.IOException;
import java.util.List;

/** A Marshaler of key value pairs. See {@link ArrayAnyValueMarshaler}. */
final class ArrayAnyValueStatelessMarshaler implements StatelessMarshaler<List<Value<?>>> {

  static final ArrayAnyValueStatelessMarshaler INSTANCE = new ArrayAnyValueStatelessMarshaler();

  private ArrayAnyValueStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, List<Value<?>> value, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessageWithContext(
        ArrayValue.VALUES, value, AnyValueStatelessMarshaler.INSTANCE, context);
  }

  @Override
  public int getBinarySerializedSize(List<Value<?>> value, MarshalerContext context) {
    return StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
        ArrayValue.VALUES, value, AnyValueStatelessMarshaler.INSTANCE, context);
  }
}
