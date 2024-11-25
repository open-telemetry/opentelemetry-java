/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.AttributeUnit;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class AttributeUnitMarshaler extends MarshalerWithSize {

  private static final AttributeUnitMarshaler[] EMPTY_REPEATED = new AttributeUnitMarshaler[0];

  private final int attributeKeyStrindex;
  private final int unitStrindex;

  static AttributeUnitMarshaler create(AttributeUnitData attributeUnitData) {
    return new AttributeUnitMarshaler(
        attributeUnitData.getAttributeKeyStrindex(), attributeUnitData.getUnitIndexStrindex());
  }

  static AttributeUnitMarshaler[] createRepeated(List<AttributeUnitData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    AttributeUnitMarshaler[] attributeUnitMarshalers = new AttributeUnitMarshaler[items.size()];
    items.forEach(
        item ->
            new Consumer<AttributeUnitData>() {
              int index = 0;

              @Override
              public void accept(AttributeUnitData attributeUnitData) {
                attributeUnitMarshalers[index++] = AttributeUnitMarshaler.create(attributeUnitData);
              }
            });
    return attributeUnitMarshalers;
  }

  private AttributeUnitMarshaler(int attributeKeyStrindex, int unitStrindex) {
    super(calculateSize(attributeKeyStrindex, unitStrindex));
    this.attributeKeyStrindex = attributeKeyStrindex;
    this.unitStrindex = unitStrindex;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt32(AttributeUnit.ATTRIBUTE_KEY_STRINDEX, attributeKeyStrindex);
    output.serializeInt32(AttributeUnit.UNIT_STRINDEX, unitStrindex);
  }

  private static int calculateSize(int attributeKeyStrindex, int unitStrindex) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeInt32(AttributeUnit.ATTRIBUTE_KEY_STRINDEX, attributeKeyStrindex);
    size += MarshalerUtil.sizeInt32(AttributeUnit.UNIT_STRINDEX, unitStrindex);
    return size;
  }
}
