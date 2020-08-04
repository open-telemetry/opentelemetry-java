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

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ResourceAdapter}. */
class ResourceAdapterTest {
  @Test
  void toProtoResource() {
    assertThat(
            ResourceAdapter.toProtoResource(
                    Resource.create(
                        Attributes.of(
                            "key_bool",
                            AttributeValue.booleanAttributeValue(true),
                            "key_string",
                            AttributeValue.stringAttributeValue("string"),
                            "key_int",
                            AttributeValue.longAttributeValue(100),
                            "key_double",
                            AttributeValue.doubleAttributeValue(100.3))))
                .getAttributesList())
        .containsExactlyInAnyOrder(
            KeyValue.newBuilder()
                .setKey("key_bool")
                .setValue(AnyValue.newBuilder().setBoolValue(true).build())
                .build(),
            KeyValue.newBuilder()
                .setKey("key_string")
                .setValue(AnyValue.newBuilder().setStringValue("string").build())
                .build(),
            KeyValue.newBuilder()
                .setKey("key_int")
                .setValue(AnyValue.newBuilder().setIntValue(100).build())
                .build(),
            KeyValue.newBuilder()
                .setKey("key_double")
                .setValue(AnyValue.newBuilder().setDoubleValue(100.3).build())
                .build());
  }

  @Test
  void toProtoResource_Empty() {
    assertThat(ResourceAdapter.toProtoResource(Resource.getEmpty()))
        .isEqualTo(io.opentelemetry.proto.resource.v1.Resource.newBuilder().build());
  }
}
