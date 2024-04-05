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

final class DoubleAnyValueMarshaler extends MarshalerWithSize {

  private final double value;

  private DoubleAnyValueMarshaler(double value) {
    super(calculateSize(value));
    this.value = value;
  }

  static MarshalerWithSize create(double value) {
    return new DoubleAnyValueMarshaler(value);
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    // Do not call serialize* method because we always have to write the message tag even if the
    // value is empty since it's a oneof.
    output.writeDouble(AnyValue.DOUBLE_VALUE, value);
  }

  private static int calculateSize(double value) {
    return AnyValue.DOUBLE_VALUE.getTagSize() + CodedOutputStream.computeDoubleSizeNoTag(value);
  }
}
