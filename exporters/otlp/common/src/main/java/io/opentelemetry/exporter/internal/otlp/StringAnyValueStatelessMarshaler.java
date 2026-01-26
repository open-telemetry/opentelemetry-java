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

/**
 * A Marshaler of string-valued {@link AnyValue}. See {@link StringAnyValueMarshaler}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class StringAnyValueStatelessMarshaler implements StatelessMarshaler<String> {
  static final StringAnyValueStatelessMarshaler INSTANCE = new StringAnyValueStatelessMarshaler();

  private StringAnyValueStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, String value, MarshalerContext context)
      throws IOException {
    output.writeString(AnyValue.STRING_VALUE, value, context.getSize(), context);
  }

  @Override
  public int getBinarySerializedSize(String value, MarshalerContext context) {
    int utf8Size = context.getStringEncoder().getUtf8Size(value);
    context.addSize(utf8Size);
    return AnyValue.STRING_VALUE.getTagSize()
        + CodedOutputStream.computeUInt32SizeNoTag(utf8Size)
        + utf8Size;
  }
}
