/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.otlp;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CommonAdapter}. */
class CommonAdapterTest {
  @Test
  void toProtoAttribute_Bool() {
    assertThat(CommonAdapter.toProtoAttribute(booleanKey("key"), true))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setBoolValue(true).build())
                .build());
  }

  @Test
  void toProtoAttribute_BoolArray() {
    assertThat(CommonAdapter.toProtoAttribute(booleanArrayKey("key"), Arrays.asList(true, false)))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(
                    AnyValue.newBuilder()
                        .setArrayValue(
                            ArrayValue.newBuilder()
                                .addValues(AnyValue.newBuilder().setBoolValue(true).build())
                                .addValues(AnyValue.newBuilder().setBoolValue(false).build())
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoAttribute_String() {
    assertThat(CommonAdapter.toProtoAttribute(stringKey("key"), "string"))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setStringValue("string").build())
                .build());
  }

  @Test
  void toProtoAttribute_StringArray() {
    assertThat(
            CommonAdapter.toProtoAttribute(
                stringArrayKey("key"), Arrays.asList("string1", "string2")))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(
                    AnyValue.newBuilder()
                        .setArrayValue(
                            ArrayValue.newBuilder()
                                .addValues(AnyValue.newBuilder().setStringValue("string1").build())
                                .addValues(AnyValue.newBuilder().setStringValue("string2").build())
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoAttribute_Int() {
    assertThat(CommonAdapter.toProtoAttribute(longKey("key"), 100L))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setIntValue(100).build())
                .build());
  }

  @Test
  void toProtoAttribute_IntArray() {
    assertThat(CommonAdapter.toProtoAttribute(longArrayKey("key"), Arrays.asList(100L, 200L)))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(
                    AnyValue.newBuilder()
                        .setArrayValue(
                            ArrayValue.newBuilder()
                                .addValues(AnyValue.newBuilder().setIntValue(100).build())
                                .addValues(AnyValue.newBuilder().setIntValue(200).build())
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoAttribute_Double() {
    assertThat(CommonAdapter.toProtoAttribute(doubleKey("key"), 100.3d))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setDoubleValue(100.3).build())
                .build());
  }

  @Test
  void toProtoAttribute_DoubleArray() {
    assertThat(CommonAdapter.toProtoAttribute(doubleArrayKey("key"), Arrays.asList(100.3, 200.5)))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(
                    AnyValue.newBuilder()
                        .setArrayValue(
                            ArrayValue.newBuilder()
                                .addValues(AnyValue.newBuilder().setDoubleValue(100.3).build())
                                .addValues(AnyValue.newBuilder().setDoubleValue(200.5).build())
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoInstrumentationLibrary() {
    InstrumentationLibrary instrumentationLibrary =
        CommonAdapter.toProtoInstrumentationLibrary(
            InstrumentationLibraryInfo.create("name", "version"));
    assertThat(instrumentationLibrary.getName()).isEqualTo("name");
    assertThat(instrumentationLibrary.getVersion()).isEqualTo("version");
  }

  @Test
  void toProtoInstrumentationLibrary_NoVersion() {
    InstrumentationLibrary instrumentationLibrary =
        CommonAdapter.toProtoInstrumentationLibrary(
            InstrumentationLibraryInfo.create("name", null));
    assertThat(instrumentationLibrary.getName()).isEqualTo("name");
    assertThat(instrumentationLibrary.getVersion()).isEmpty();
  }
}
