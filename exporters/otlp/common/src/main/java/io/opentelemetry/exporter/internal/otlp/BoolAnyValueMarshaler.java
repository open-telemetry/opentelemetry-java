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

final class BoolAnyValueMarshaler extends MarshalerWithSize {

  private final boolean value;

  private BoolAnyValueMarshaler(boolean value) {
    super(calculateSize(value));
    this.value = value;
  }

  static MarshalerWithSize create(boolean value) {
    return new BoolAnyValueMarshaler(value);
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    // Do not call serialize* method because we always have to write the message tag even if the
    // value is empty since it's a oneof.
    output.writeBool(AnyValue.BOOL_VALUE, value);
  }

  public static void writeTo(Serializer output, boolean value) throws IOException {
    output.writeBool(AnyValue.BOOL_VALUE, value);
  }

  public static int calculateSize(boolean value) {
    return AnyValue.BOOL_VALUE.getTagSize() + CodedOutputStream.computeBoolSizeNoTag(value);
  }
}
