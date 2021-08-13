/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.internal.shaded.WeakConcurrentMap;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;

public final class JsonCommonAdapter {
  private static final WeakConcurrentMap<InstrumentationLibraryInfo, JSONObject>
      INSTRUMENTATION_LIBRARY_PROTO_CACHE = new WeakConcurrentMap.WithInlinedExpunction<>();

  /** Converts the provided {@link AttributeKey} to JSONArray. */
  @SuppressWarnings("unchecked")
  public static JSONObject toJsonAttribute(AttributeKey<?> key, Object value) {
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
    JSONObject keyValue = new JSONObject();
    keyValue.put("key", key.getKey());
    keyValue.put("value", new JSONObject());
    return keyValue;
  }

  private static JSONObject makeStringArrayKeyValue(AttributeKey<?> key, List<String> value) {
    JSONObject keyValueBuilder = new JSONObject();
    keyValueBuilder.put("key", key.getKey());
    JSONObject anyValue = new JSONObject();
    anyValue.put("array_value", makeStringArrayAnyValue(value));
    keyValueBuilder.put("value", anyValue);
    return keyValueBuilder;
  }

  private static JSONObject makeDoubleArrayKeyValue(AttributeKey<?> key, List<Double> value) {
    JSONObject keyValueBuilder = new JSONObject();
    keyValueBuilder.put("key", key.getKey());
    JSONObject anyValue = new JSONObject();
    anyValue.put("array_value", makeDoubleArrayAnyValue(value));
    keyValueBuilder.put("value", anyValue);
    return keyValueBuilder;
  }

  private static JSONObject makeLongArrayKeyValue(AttributeKey<?> key, List<Long> value) {
    JSONObject keyValueBuilder = new JSONObject();
    keyValueBuilder.put("key", key.getKey());
    JSONObject anyValue = new JSONObject();
    anyValue.put("array_value", makeLongArrayAnyValue(value));
    keyValueBuilder.put("value", anyValue);
    return keyValueBuilder;
  }

  @VisibleForTesting
  private static JSONObject makeBooleanArrayKeyValue(AttributeKey<?> key, List<Boolean> value) {
    JSONObject keyValueBuilder = new JSONObject();
    keyValueBuilder.put("key", key.getKey());
    JSONObject anyValue = new JSONObject();
    anyValue.put("array_value", makeBooleanArrayAnyValue(value));
    keyValueBuilder.put("value", anyValue);
    return keyValueBuilder;
  }

  private static JSONObject makeDoubleKeyValue(AttributeKey<?> key, Double value) {
    JSONObject keyValueBuilder = new JSONObject();
    keyValueBuilder.put("key", key.getKey());
    JSONObject anyValue = new JSONObject();
    anyValue.put("double_value", value);
    keyValueBuilder.put("value", anyValue);
    return keyValueBuilder;
  }

  private static JSONObject makeLongKeyValue(AttributeKey<?> key, Long value) {
    JSONObject keyValueBuilder = new JSONObject();
    keyValueBuilder.put("key", key.getKey());
    JSONObject anyValue = new JSONObject();
    anyValue.put("int_value", value);
    keyValueBuilder.put("value", anyValue);
    return keyValueBuilder;
  }

  private static JSONObject makeBooleanKeyValue(AttributeKey<?> key, boolean value) {
    JSONObject keyValueBuilder = new JSONObject();
    keyValueBuilder.put("key", key.getKey());
    JSONObject anyValue = new JSONObject();
    anyValue.put("bool_value", value);
    keyValueBuilder.put("value", anyValue);
    return keyValueBuilder;
  }

  static JSONObject makeStringKeyValue(AttributeKey<?> key, String value) {
    JSONObject keyValueBuilder = new JSONObject();
    keyValueBuilder.put("key", key.getKey());
    JSONObject anyValue = new JSONObject();
    anyValue.put("string_value", value);
    keyValueBuilder.put("value", anyValue);
    return keyValueBuilder;
  }

  static JSONArray makeBooleanArrayAnyValue(List<Boolean> booleanArrayValue) {
    JSONArray builder = new JSONArray();
    for (Boolean value : booleanArrayValue) {
      JSONObject boolValue = new JSONObject();
      boolValue.put("bool_value", value);
      builder.add(boolValue);
    }
    return builder;
  }

  static JSONArray makeLongArrayAnyValue(List<Long> longArrayValue) {
    JSONArray builder = new JSONArray();
    for (Long value : longArrayValue) {
      JSONObject longValue = new JSONObject();
      longValue.put("int_value", value);
      builder.add(longValue);
    }
    return builder;
  }

  static JSONArray makeDoubleArrayAnyValue(List<Double> doubleArrayValue) {
    JSONArray builder = new JSONArray();
    for (Double value : doubleArrayValue) {
      JSONObject doubleValue = new JSONObject();
      doubleValue.put("int_value", value);
      builder.add(doubleValue);
    }
    return builder;
  }

  static JSONArray makeStringArrayAnyValue(List<String> stringArrayValue) {
    JSONArray builder = new JSONArray();
    for (String value : stringArrayValue) {
      JSONObject stringValue = new JSONObject();
      stringValue.put("string_value", value);
      builder.add(stringValue);
    }
    return builder;
  }

  static JSONObject toProtoInstrumentationLibrary(
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    JSONObject cached = INSTRUMENTATION_LIBRARY_PROTO_CACHE.get(instrumentationLibraryInfo);
    if (cached == null) {
      cached = new JSONObject();
      cached.put("name", instrumentationLibraryInfo.getName());
      if (instrumentationLibraryInfo.getVersion() != null) {
        cached.put("version", instrumentationLibraryInfo.getVersion());
      }
      INSTRUMENTATION_LIBRARY_PROTO_CACHE.put(instrumentationLibraryInfo, cached);
    }
    return cached;
  }

  static JSONObject toProtoResource(Resource resource) {
    JSONObject builder = new JSONObject();
    JSONArray attributes = new JSONArray();
    resource.getAttributes().forEach((key, value) -> attributes.add(toJsonAttribute(key, value)));
    builder.put("attributes", attributes);
    return builder;
  }

  private JsonCommonAdapter() {}
}
