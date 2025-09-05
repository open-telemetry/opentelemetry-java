/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.AnyValueMarshaler;
import io.opentelemetry.proto.profiles.v1development.internal.KeyValueAndUnit;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class KeyValueAndUnitMarshaler extends MarshalerWithSize {

  private static final KeyValueAndUnitMarshaler[] EMPTY_REPEATED = new KeyValueAndUnitMarshaler[0];

  private final int keyStringIndex;
  private final Marshaler valueMarshaler;
  private final int unitStringIndex;

  static KeyValueAndUnitMarshaler create(KeyValueAndUnitData keyValueAndUnitData) {
    Marshaler valueMarshaler = AnyValueMarshaler.create(keyValueAndUnitData.getValue());
    return new KeyValueAndUnitMarshaler(
        keyValueAndUnitData.getKeyStringIndex(),
        valueMarshaler,
        keyValueAndUnitData.getUnitStringIndex());
  }

  static KeyValueAndUnitMarshaler[] createRepeated(List<KeyValueAndUnitData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    KeyValueAndUnitMarshaler[] keyValueAndUnitMarshalers =
        new KeyValueAndUnitMarshaler[items.size()];
    items.forEach(
        new Consumer<KeyValueAndUnitData>() {
          int index = 0;

          @Override
          public void accept(KeyValueAndUnitData keyValueAndUnitData) {
            keyValueAndUnitMarshalers[index++] =
                KeyValueAndUnitMarshaler.create(keyValueAndUnitData);
          }
        });
    return keyValueAndUnitMarshalers;
  }

  private KeyValueAndUnitMarshaler(
      int keyStringIndex, Marshaler valueMarshaler, int unitStringIndex) {
    super(calculateSize(keyStringIndex, valueMarshaler, unitStringIndex));
    this.keyStringIndex = keyStringIndex;
    this.valueMarshaler = valueMarshaler;
    this.unitStringIndex = unitStringIndex;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt32(KeyValueAndUnit.KEY_STRINDEX, keyStringIndex);
    output.serializeMessage(KeyValueAndUnit.VALUE, valueMarshaler);
    output.serializeInt64(KeyValueAndUnit.UNIT_STRINDEX, unitStringIndex);
  }

  private static int calculateSize(
      int keyStringIndex, Marshaler valueMarshaler, int unitStringIndex) {
    int size = 0;
    size += MarshalerUtil.sizeInt32(KeyValueAndUnit.KEY_STRINDEX, keyStringIndex);
    size += MarshalerUtil.sizeMessage(KeyValueAndUnit.VALUE, valueMarshaler);
    size += MarshalerUtil.sizeInt32(KeyValueAndUnit.UNIT_STRINDEX, unitStringIndex);
    return size;
  }
}
