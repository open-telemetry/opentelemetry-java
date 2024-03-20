/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.DefaultMessageSize;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.MarshallingObjectsPool;
import io.opentelemetry.exporter.internal.marshal.MessageSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import java.io.IOException;

final class IntAnyValueMarshaler extends MarshalerWithSize {

  private final long value;

  private IntAnyValueMarshaler(long value) {
    super(calculateSize(value));
    this.value = value;
  }

  static MarshalerWithSize create(long value) {
    return new IntAnyValueMarshaler(value);
  }

  public static MessageSize messageSize(long value, MarshallingObjectsPool pool) {
    int encodedSize = calculateSize(value);
    DefaultMessageSize messageSize = pool.getDefaultMessageSizePool().borrowObject();
    messageSize.set(encodedSize);
    return messageSize;
  }

  public static void encode(Serializer output, long value) throws IOException {
    output.writeInt64(AnyValue.INT_VALUE, value);
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    // Do not call serialize* method because we always have to write the message tag even if the
    // value is empty since it's a oneof.
    output.writeInt64(AnyValue.INT_VALUE, value);
  }

  private static int calculateSize(long value) {
    return AnyValue.INT_VALUE.getTagSize() + CodedOutputStream.computeInt64SizeNoTag(value);
  }
}
