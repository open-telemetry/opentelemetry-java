/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.otlp;

import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.AttributeKeyImpl.BooleanArrayKey;
import io.opentelemetry.common.AttributeKeyImpl.BooleanKey;
import io.opentelemetry.common.AttributeKeyImpl.DoubleArrayKey;
import io.opentelemetry.common.AttributeKeyImpl.DoubleKey;
import io.opentelemetry.common.AttributeKeyImpl.LongArrayKey;
import io.opentelemetry.common.AttributeKeyImpl.LongKey;
import io.opentelemetry.common.AttributeKeyImpl.StringArrayKey;
import io.opentelemetry.common.AttributeKeyImpl.StringKey;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.List;

final class CommonAdapter {
  @SuppressWarnings("unchecked")
  public static <T> KeyValue toProtoAttribute(AttributeKey<T> key, T value) {
    KeyValue.Builder builder = KeyValue.newBuilder().setKey(key.get());
    switch (key.getType()) {
      case STRING:
        return makeStringKeyValue((StringKey) key, (String) value);
      case BOOLEAN:
        return makeBooleanKeyValue((BooleanKey) key, (boolean) value);
      case LONG:
        return makeLongKeyValue((LongKey) key, (Long) value);
      case DOUBLE:
        return makeDoubleKeyValue((DoubleKey) key, (Double) value);
      case BOOLEAN_ARRAY:
        return makeBooleanArrayKeyValue((BooleanArrayKey) key, (List<Boolean>) value);
      case LONG_ARRAY:
        return makeLongArrayKeyValue((LongArrayKey) key, (List<Long>) value);
      case DOUBLE_ARRAY:
        return makeDoubleArrayKeyValue((DoubleArrayKey) key, (List<Double>) value);
      case STRING_ARRAY:
        return makeStringArrayKeyValue((StringArrayKey) key, (List<String>) value);
    }
    return builder.setValue(AnyValue.getDefaultInstance()).build();
  }

  private static KeyValue makeLongArrayKeyValue(LongArrayKey key, List<Long> value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.get())
            .setValue(AnyValue.newBuilder().setArrayValue(makeLongArrayAnyValue(value)).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeDoubleArrayKeyValue(DoubleArrayKey key, List<Double> value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.get())
            .setValue(AnyValue.newBuilder().setArrayValue(makeDoubleArrayAnyValue(value)).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeBooleanArrayKeyValue(BooleanArrayKey key, List<Boolean> value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.get())
            .setValue(AnyValue.newBuilder().setArrayValue(makeBooleanArrayAnyValue(value)).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeStringArrayKeyValue(StringArrayKey key, List<String> value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.get())
            .setValue(AnyValue.newBuilder().setArrayValue(makeStringArrayAnyValue(value)).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeLongKeyValue(LongKey key, long value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.get())
            .setValue(AnyValue.newBuilder().setIntValue(value).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeDoubleKeyValue(DoubleKey key, double value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.get())
            .setValue(AnyValue.newBuilder().setDoubleValue(value).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeBooleanKeyValue(BooleanKey key, boolean value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.get())
            .setValue(AnyValue.newBuilder().setBoolValue(value).build());

    return keyValueBuilder.build();
  }

  private static KeyValue makeStringKeyValue(StringKey key, String value) {
    KeyValue.Builder keyValueBuilder =
        KeyValue.newBuilder()
            .setKey(key.get())
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
