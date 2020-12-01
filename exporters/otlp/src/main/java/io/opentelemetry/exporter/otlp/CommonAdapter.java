/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.List;

final class CommonAdapter {
  @SuppressWarnings("unchecked")
  public static <T> KeyValue toProtoAttribute(AttributeKey<T> key, T value) {
    KeyValue.Builder builder = KeyValue.newBuilder().setKey(key.getKey());
    switch (key.getType()) {
      case STRING:
        return makeStringKeyValue(key, (String) value);
      case BOOLEAN:
        return makeBooleanKeyValue(key, (boolean) value);
      case LONG:
        return makeLongKeyValue(key, (Long) value);
      case DOUBLE:
        return makeDoubleKeyValue(key, (Double) value);
      case BOOLEAN_ARRAY:
        return makeBooleanArrayKeyValue(key, (List<Boolean>) value);
      case LONG_ARRAY:
        return makeLongArrayKeyValue(key, (List<Long>) value);
      case DOUBLE_ARRAY:
        return makeDoubleArrayKeyValue(key, (List<Double>) value);
      case STRING_ARRAY:
        return makeStringArrayKeyValue(key, (List<String>) value);
    }
    return builder.setValue(AnyValue.getDefaultInstance()).build();
  }

  private static KeyValue makeLongArrayKeyValue(AttributeKey<?> key, List<Long> value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.getKey())
            .setValue(AnyValue.newBuilder().setArrayValue(makeLongArrayAnyValue(value)).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeDoubleArrayKeyValue(AttributeKey<?> key, List<Double> value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.getKey())
            .setValue(AnyValue.newBuilder().setArrayValue(makeDoubleArrayAnyValue(value)).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeBooleanArrayKeyValue(AttributeKey<?> key, List<Boolean> value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.getKey())
            .setValue(AnyValue.newBuilder().setArrayValue(makeBooleanArrayAnyValue(value)).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeStringArrayKeyValue(AttributeKey<?> key, List<String> value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.getKey())
            .setValue(AnyValue.newBuilder().setArrayValue(makeStringArrayAnyValue(value)).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeLongKeyValue(AttributeKey<?> key, long value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.getKey())
            .setValue(AnyValue.newBuilder().setIntValue(value).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeDoubleKeyValue(AttributeKey<?> key, double value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.getKey())
            .setValue(AnyValue.newBuilder().setDoubleValue(value).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeBooleanKeyValue(AttributeKey<?> key, boolean value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.getKey())
            .setValue(AnyValue.newBuilder().setBoolValue(value).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeStringKeyValue(AttributeKey<?> key, String value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.getKey())
            .setValue(AnyValue.newBuilder().setStringValue(value).build());

    return keyValueBuilder.build();
  }

  private static ArrayValue makeDoubleArrayAnyValue(List<Double> doubleArrayValue) {
    ArrayValue.Builder builder = ArrayValue.newBuilder();
    for (Double doubleValue : doubleArrayValue) {
      builder.addValues(AnyValue.newBuilder().setDoubleValue(doubleValue).build());
    }
    return builder.build();
  }

  private static ArrayValue makeLongArrayAnyValue(List<Long> longArrayValue) {
    ArrayValue.Builder builder = ArrayValue.newBuilder();
    for (Long intValue : longArrayValue) {
      builder.addValues(AnyValue.newBuilder().setIntValue(intValue).build());
    }
    return builder.build();
  }

  private static ArrayValue makeStringArrayAnyValue(List<String> stringArrayValue) {
    ArrayValue.Builder builder = ArrayValue.newBuilder();
    for (String string : stringArrayValue) {
      builder.addValues(AnyValue.newBuilder().setStringValue(string).build());
    }
    return builder.build();
  }

  private static ArrayValue makeBooleanArrayAnyValue(List<Boolean> booleanArrayValue) {
    ArrayValue.Builder builder = ArrayValue.newBuilder();
    for (Boolean bool : booleanArrayValue) {
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
