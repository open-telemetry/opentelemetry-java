/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import java.io.IOException;

/**
 * A Marshaler of key value pairs. See {@link AnyValueMarshaler}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class KeyValueStatelessMarshaler implements StatelessMarshaler<KeyValue> {

  public static final KeyValueStatelessMarshaler INSTANCE = new KeyValueStatelessMarshaler();
  private static final byte[] EMPTY_BYTES = new byte[0];

  private KeyValueStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, KeyValue value, MarshalerContext context)
      throws IOException {
    String key = value.getKey();
    if (key.isEmpty()) {
      output.serializeString(io.opentelemetry.proto.common.v1.internal.KeyValue.KEY, EMPTY_BYTES);
    } else {
      output.serializeStringWithContext(
          io.opentelemetry.proto.common.v1.internal.KeyValue.KEY, key, context);
    }
    output.serializeMessageWithContext(
        io.opentelemetry.proto.common.v1.internal.KeyValue.VALUE,
        value.getValue(),
        AnyValueStatelessMarshaler.INSTANCE,
        context);
  }

  @Override
  public int getBinarySerializedSize(KeyValue value, MarshalerContext context) {
    int size = 0;
    String key = value.getKey();
    if (!key.isEmpty()) {
      size +=
          StatelessMarshalerUtil.sizeStringWithContext(
              io.opentelemetry.proto.common.v1.internal.KeyValue.KEY, key, context);
    }
    size +=
        StatelessMarshalerUtil.sizeMessageWithContext(
            io.opentelemetry.proto.common.v1.internal.KeyValue.VALUE,
            value.getValue(),
            AnyValueStatelessMarshaler.INSTANCE,
            context);

    return size;
  }
}
