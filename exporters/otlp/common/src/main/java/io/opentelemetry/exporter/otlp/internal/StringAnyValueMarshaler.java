/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.proto.common.v1.internal.AnyValue;
import java.io.IOException;

/**
 * A Marshaler of string-valued {@link AnyValue}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class StringAnyValueMarshaler extends MarshalerWithSize {

  private final byte[] valueUtf8;

  public StringAnyValueMarshaler(byte[] valueUtf8) {
    super(calculateSize(valueUtf8));
    this.valueUtf8 = valueUtf8;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    // Do not call serialize* method because we always have to write the message tag even if the
    // value is empty since it's a oneof.
    output.writeString(AnyValue.STRING_VALUE, valueUtf8);
  }

  private static int calculateSize(byte[] valueUtf8) {
    return AnyValue.STRING_VALUE.getTagSize()
        + CodedOutputStream.computeByteArraySizeNoTag(valueUtf8);
  }
}
