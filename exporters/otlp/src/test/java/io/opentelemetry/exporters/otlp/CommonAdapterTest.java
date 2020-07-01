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
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
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
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setBoolValue(true).build())
                .build());
  }

  @Test
  public void toProtoAttribute_String() {
    assertThat(CommonAdapter.toProtoAttribute("key", AttributeValue.stringAttributeValue("string")))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setStringValue("string").build())
                .build());
  }

  @Test
  public void toProtoAttribute_Int() {
    assertThat(CommonAdapter.toProtoAttribute("key", AttributeValue.longAttributeValue(100)))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setIntValue(100).build())
                .build());
  }

  @Test
  public void toProtoAttribute_Double() {
    assertThat(CommonAdapter.toProtoAttribute("key", AttributeValue.doubleAttributeValue(100.3)))
        .isEqualTo(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setDoubleValue(100.3).build())
                .build());
  }

  // todo: add tests for the 4 array types.

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
