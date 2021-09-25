/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.proto.common.v1.internal.AnyValue;
import java.io.IOException;

public class AnyValueMarshaler extends MarshalerWithSize {

  private final byte[] stringUtf8;

  public static AnyValueMarshaler createString(byte[] stringUtf8) {
    return new AnyValueMarshaler(stringUtf8);
  }

  public AnyValueMarshaler(byte[] stringUtf8) {
    super(calculateSizeString(stringUtf8));
    this.stringUtf8 = stringUtf8;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.writeString(AnyValue.STRING_VALUE, stringUtf8);
  }

  private static int calculateSizeString(byte[] stringUtf8) {
    int size = 0;
    size += MarshalerUtil.sizeBytes(AnyValue.STRING_VALUE, stringUtf8);
    return size;
  }
}
