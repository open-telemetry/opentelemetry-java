/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.otlp;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

final class CommonAdapter {
  static KeyValue toProtoAttribute(String key, AttributeValue attributeValue) {
    KeyValue.Builder builder = KeyValue.newBuilder().setKey(key);
    switch (attributeValue.getType()) {
      case STRING:
        return builder
            .setValue(AnyValue.newBuilder().setStringValue(attributeValue.getStringValue()).build())
            .build();
      case BOOLEAN:
        return builder
            .setValue(AnyValue.newBuilder().setBoolValue(attributeValue.getBooleanValue()).build())
            .build();
      case LONG:
        return builder
            .setValue(AnyValue.newBuilder().setIntValue(attributeValue.getLongValue()).build())
            .build();
      case DOUBLE:
        return builder
            .setValue(AnyValue.newBuilder().setDoubleValue(attributeValue.getDoubleValue()).build())
            .build();
      case BOOLEAN_ARRAY:
        return builder
            .setValue(
                AnyValue.newBuilder()
                    .setArrayValue(makeBooleanArrayAnyValue(attributeValue))
                    .build())
            .build();
      case LONG_ARRAY:
        return builder
            .setValue(
                AnyValue.newBuilder().setArrayValue(makeLongArrayAnyValue(attributeValue)).build())
            .build();
      case DOUBLE_ARRAY:
        return builder
            .setValue(
                AnyValue.newBuilder()
                    .setArrayValue(makeDoubleArrayAnyValue(attributeValue))
                    .build())
            .build();
      case STRING_ARRAY:
        return builder
            .setValue(
                AnyValue.newBuilder()
                    .setArrayValue(makeStringArrayAnyValue(attributeValue))
                    .build())
            .build();
    }
    return builder.setValue(AnyValue.getDefaultInstance()).build();
  }

  private static ArrayValue makeDoubleArrayAnyValue(AttributeValue attributeValue) {
    ArrayValue.Builder builder = ArrayValue.newBuilder();
    for (Double doubleValue : attributeValue.getDoubleArrayValue()) {
      builder.addValues(AnyValue.newBuilder().setDoubleValue(doubleValue).build());
    }
    return builder.build();
  }

  private static ArrayValue makeLongArrayAnyValue(AttributeValue attributeValue) {
    ArrayValue.Builder builder = ArrayValue.newBuilder();
    for (Long intValue : attributeValue.getLongArrayValue()) {
      builder.addValues(AnyValue.newBuilder().setIntValue(intValue).build());
    }
    return builder.build();
  }

  private static ArrayValue makeStringArrayAnyValue(AttributeValue attributeValue) {
    ArrayValue.Builder builder = ArrayValue.newBuilder();
    for (String string : attributeValue.getStringArrayValue()) {
      builder.addValues(AnyValue.newBuilder().setStringValue(string).build());
    }
    return builder.build();
  }

  private static ArrayValue makeBooleanArrayAnyValue(AttributeValue attributeValue) {
    ArrayValue.Builder builder = ArrayValue.newBuilder();
    for (Boolean bool : attributeValue.getBooleanArrayValue()) {
      builder.addValues(AnyValue.newBuilder().setBoolValue(bool).build());
    }
    return builder.build();
  }

  static InstrumentationLibrary toProtoInstrumentationLibrary(
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return InstrumentationLibrary.newBuilder()
        .setName(instrumentationLibraryInfo.getName())
        .setVersion(
            instrumentationLibraryInfo.getVersion() == null
                ? ""
                : instrumentationLibraryInfo.getVersion())
        .build();
  }

  private CommonAdapter() {}
}
