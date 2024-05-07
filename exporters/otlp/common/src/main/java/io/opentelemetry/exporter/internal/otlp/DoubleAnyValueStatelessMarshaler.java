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

/** See {@link DoubleAnyValueMarshaler}. */
final class DoubleAnyValueStatelessMarshaler implements StatelessMarshaler<Double> {
  static final DoubleAnyValueStatelessMarshaler INSTANCE = new DoubleAnyValueStatelessMarshaler();

  private DoubleAnyValueStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, Double value, MarshalerContext context)
      throws IOException {
    output.writeDouble(AnyValue.DOUBLE_VALUE, value);
  }

  @Override
  public int getBinarySerializedSize(Double value, MarshalerContext context) {
    return AnyValue.DOUBLE_VALUE.getTagSize() + CodedOutputStream.computeDoubleSizeNoTag(value);
  }
}
