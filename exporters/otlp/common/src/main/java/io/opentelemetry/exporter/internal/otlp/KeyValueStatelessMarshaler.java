/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.KeyAnyValue;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.common.v1.internal.KeyValue;
import java.io.IOException;

/** A Marshaler of key value pairs. See {@link AnyValueMarshaler}. */
public final class KeyValueStatelessMarshaler implements StatelessMarshaler<KeyAnyValue> {

  public static final KeyValueStatelessMarshaler INSTANCE = new KeyValueStatelessMarshaler();
  private static final byte[] EMPTY_BYTES = new byte[0];

  private KeyValueStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, KeyAnyValue value, MarshalerContext context)
      throws IOException {
    String key = value.getKey();
    if (key.isEmpty()) {
      output.serializeString(KeyValue.KEY, EMPTY_BYTES);
    } else {
      output.serializeStringWithContext(KeyValue.KEY, key, context);
    }
    output.serializeMessageWithContext(
        KeyValue.VALUE, value.getAnyValue(), AnyValueStatelessMarshaler.INSTANCE, context);
  }

  @Override
  public int getBinarySerializedSize(KeyAnyValue value, MarshalerContext context) {
    int size = 0;
    String key = value.getKey();
    if (!key.isEmpty()) {
      size += StatelessMarshalerUtil.sizeStringWithContext(KeyValue.KEY, key, context);
    }
    size +=
        StatelessMarshalerUtil.sizeMessageWithContext(
            KeyValue.VALUE, value.getAnyValue(), AnyValueStatelessMarshaler.INSTANCE, context);

    return size;
  }
}
