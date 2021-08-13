/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.json;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.alibaba.fastjson.JSONObject;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonCommonAdapterTest {

  @Test
  void toProtoAttribute_Bool() {
    JSONObject keyValue = new JSONObject();
    keyValue.put("key", "key");
    JSONObject anyValue = new JSONObject();
    anyValue.put("bool_value", true);
    keyValue.put("value", anyValue);
    assertThat(JsonCommonAdapter.toJsonAttribute(booleanKey("key"), true)).isEqualTo(keyValue);
  }

  @Test
  void toProtoAttribute_BoolArray() {
    List<Boolean> valueList = Arrays.asList(true, false);
    JSONObject keyValue = new JSONObject();
    keyValue.put("key", "key");
    JSONObject anyValue = new JSONObject();
    anyValue.put("array_value", JsonCommonAdapter.makeBooleanArrayAnyValue(valueList));
    keyValue.put("value", anyValue);

    assertThat(JsonCommonAdapter.toJsonAttribute(booleanArrayKey("key"), valueList))
        .isEqualTo(keyValue);
  }

  @Test
  void toProtoAttribute_String() {
    JSONObject keyValue = new JSONObject();
    keyValue.put("key", "key");
    JSONObject anyValue = new JSONObject();
    anyValue.put("string_value", "string");
    keyValue.put("value", anyValue);
    assertThat(JsonCommonAdapter.toJsonAttribute(stringKey("key"), "string")).isEqualTo(keyValue);
  }

  @Test
  void toProtoAttribute_StringArray() {
    List<String> valueList = Arrays.asList("string1", "string2");
    JSONObject keyValue = new JSONObject();
    keyValue.put("key", "key");
    JSONObject anyValue = new JSONObject();
    anyValue.put("array_value", JsonCommonAdapter.makeStringArrayAnyValue(valueList));
    keyValue.put("value", anyValue);

    assertThat(JsonCommonAdapter.toJsonAttribute(stringArrayKey("key"), valueList))
        .isEqualTo(keyValue);
  }

  @Test
  void toProtoAttribute_Int() {
    JSONObject keyValue = new JSONObject();
    keyValue.put("key", "key");
    JSONObject anyValue = new JSONObject();
    anyValue.put("int_value", 100L);
    keyValue.put("value", anyValue);

    assertThat(JsonCommonAdapter.toJsonAttribute(longKey("key"), 100L)).isEqualTo(keyValue);
  }

  @Test
  void toProtoAttribute_IntArray() {
    List<Long> valueList = Arrays.asList(100L, 200L);
    JSONObject keyValue = new JSONObject();
    keyValue.put("key", "key");
    JSONObject anyValue = new JSONObject();
    anyValue.put("array_value", JsonCommonAdapter.makeLongArrayAnyValue(valueList));
    keyValue.put("value", anyValue);
    assertThat(JsonCommonAdapter.toJsonAttribute(longArrayKey("key"), Arrays.asList(100L, 200L)))
        .isEqualTo(keyValue);
  }

  @Test
  void toProtoAttribute_Double() {
    JSONObject keyValue = new JSONObject();
    keyValue.put("key", "key");
    JSONObject anyValue = new JSONObject();
    anyValue.put("double_value", 100.3d);
    keyValue.put("value", anyValue);

    assertThat(JsonCommonAdapter.toJsonAttribute(doubleKey("key"), 100.3d)).isEqualTo(keyValue);
  }

  @Test
  void toProtoAttribute_DoubleArray() {
    List<Double> valueList = Arrays.asList(100.3, 200.5);
    JSONObject keyValue = new JSONObject();
    keyValue.put("key", "key");
    JSONObject anyValue = new JSONObject();
    anyValue.put("array_value", JsonCommonAdapter.makeDoubleArrayAnyValue(valueList));
    keyValue.put("value", anyValue);
    assertThat(
            JsonCommonAdapter.toJsonAttribute(doubleArrayKey("key"), Arrays.asList(100.3, 200.5)))
        .isEqualTo(keyValue);
  }

  @Test
  void toProtoInstrumentationLibrary() {
    InstrumentationLibraryInfo info = InstrumentationLibraryInfo.create("name", "version");
    JSONObject instrumentationLibrary = JsonCommonAdapter.toProtoInstrumentationLibrary(info);
    assertThat(instrumentationLibrary.get("name")).isEqualTo("name");
    assertThat(instrumentationLibrary.get("version")).isEqualTo("version");
    // Memoized
    assertThat(JsonCommonAdapter.toProtoInstrumentationLibrary(info))
        .isSameAs(instrumentationLibrary);
  }

  @Test
  void toProtoInstrumentationLibrary_NoVersion() {
    JSONObject instrumentationLibrary =
        JsonCommonAdapter.toProtoInstrumentationLibrary(
            InstrumentationLibraryInfo.create("name", null));
    assertThat(instrumentationLibrary.get("name")).isEqualTo("name");
    assertThat(instrumentationLibrary.get("version")).isNull();
  }
}
