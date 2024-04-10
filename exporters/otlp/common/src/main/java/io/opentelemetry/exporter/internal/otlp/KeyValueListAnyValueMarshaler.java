/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.incubator.logs.KeyAnyValue;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import io.opentelemetry.proto.common.v1.internal.KeyValueList;
import java.io.IOException;
import java.util.List;

final class KeyValueListAnyValueMarshaler extends MarshalerWithSize {

  private final Marshaler value;

  private KeyValueListAnyValueMarshaler(KeyValueListMarshaler value) {
    super(calculateSize(value));
    this.value = value;
  }

  static MarshalerWithSize create(List<KeyAnyValue> values) {
    int len = values.size();
    KeyValueMarshaler[] marshalers = new KeyValueMarshaler[values.size()];
    for (int i = 0; i < len; i++) {
      marshalers[i] = KeyValueMarshaler.createForKeyAnyValue(values.get(i));
    }
    return new KeyValueListAnyValueMarshaler(new KeyValueListMarshaler(marshalers));
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(AnyValue.KVLIST_VALUE, value);
  }

  private static int calculateSize(Marshaler value) {
    return MarshalerUtil.sizeMessage(AnyValue.KVLIST_VALUE, value);
  }

  private static class KeyValueListMarshaler extends MarshalerWithSize {

    private final Marshaler[] values;

    private KeyValueListMarshaler(KeyValueMarshaler[] values) {
      super(calculateSize(values));
      this.values = values;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeRepeatedMessage(KeyValueList.VALUES, values);
    }

    private static int calculateSize(Marshaler[] values) {
      return MarshalerUtil.sizeRepeatedMessage(KeyValueList.VALUES, values);
    }
  }
}
