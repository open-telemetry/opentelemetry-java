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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.proto.common.v1.AttributeKeyValue;
import io.opentelemetry.proto.common.v1.AttributeKeyValue.ValueType;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CommonAdapter}. */
@RunWith(JUnit4.class)
public class CommonAdapterTest {
  @Test
  public void toProtoAttribute_Bool() {
    assertThat(CommonAdapter.toProtoAttribute("key", AttributeValue.booleanAttributeValue(true)))
        .isEqualTo(
            AttributeKeyValue.newBuilder()
                .setKey("key")
                .setBoolValue(true)
                .setType(ValueType.BOOL)
                .build());
  }

  @Test
  public void toProtoAttribute_String() {
    assertThat(CommonAdapter.toProtoAttribute("key", AttributeValue.stringAttributeValue("string")))
        .isEqualTo(
            AttributeKeyValue.newBuilder()
                .setKey("key")
                .setStringValue("string")
                .setType(ValueType.STRING)
                .build());
  }

  @Test
  public void toProtoAttribute_Int() {
    assertThat(CommonAdapter.toProtoAttribute("key", AttributeValue.longAttributeValue(100)))
        .isEqualTo(
            AttributeKeyValue.newBuilder()
                .setKey("key")
                .setIntValue(100)
                .setType(ValueType.INT)
                .build());
  }

  @Test
  public void toProtoAttribute_Double() {
    assertThat(CommonAdapter.toProtoAttribute("key", AttributeValue.doubleAttributeValue(100.3)))
        .isEqualTo(
            AttributeKeyValue.newBuilder()
                .setKey("key")
                .setDoubleValue(100.3)
                .setType(ValueType.DOUBLE)
                .build());
  }

  @Test
  public void toProtoInstrumentationLibrary() {
    InstrumentationLibrary instrumentationLibrary =
        CommonAdapter.toProtoInstrumentationLibrary(
            InstrumentationLibraryInfo.create("name", "version"));
    assertThat(instrumentationLibrary.getName()).isEqualTo("name");
    assertThat(instrumentationLibrary.getVersion()).isEqualTo("version");
  }

  @Test
  public void toProtoInstrumentationLibrary_NoVersion() {
    InstrumentationLibrary instrumentationLibrary =
        CommonAdapter.toProtoInstrumentationLibrary(
            InstrumentationLibraryInfo.create("name", null));
    assertThat(instrumentationLibrary.getName()).isEqualTo("name");
    assertThat(instrumentationLibrary.getVersion()).isEmpty();
  }
}
