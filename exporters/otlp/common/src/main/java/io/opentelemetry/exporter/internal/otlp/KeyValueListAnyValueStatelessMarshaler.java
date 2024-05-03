/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.incubator.logs.KeyAnyValue;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.common.v1.internal.KeyValueList;
import java.io.IOException;
import java.util.List;

/** A Marshaler of key value pairs. See {@link KeyValueListAnyValueMarshaler}. */
final class KeyValueListAnyValueStatelessMarshaler
    implements StatelessMarshaler<List<KeyAnyValue>> {

  static final KeyValueListAnyValueStatelessMarshaler INSTANCE =
      new KeyValueListAnyValueStatelessMarshaler();

  private KeyValueListAnyValueStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, List<KeyAnyValue> value, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessageWithContext(
        KeyValueList.VALUES, value, KeyValueStatelessMarshaler.INSTANCE, context);
  }

  @Override
  public int getBinarySerializedSize(List<KeyAnyValue> value, MarshalerContext context) {
    return StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
        KeyValueList.VALUES, value, KeyValueStatelessMarshaler.INSTANCE, context);
  }
}
