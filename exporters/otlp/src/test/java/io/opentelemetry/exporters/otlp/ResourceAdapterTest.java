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

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.proto.common.v1.AttributeKeyValue;
import io.opentelemetry.proto.common.v1.AttributeKeyValue.ValueType;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ResourceAdapter}. */
@RunWith(JUnit4.class)
public class ResourceAdapterTest {
  @Test
  public void toProtoResource() {
    assertThat(
            ResourceAdapter.toProtoResource(
                    Resource.create(
                        ImmutableMap.of(
                            "key_bool",
                            AttributeValue.booleanAttributeValue(true),
                            "key_string",
                            AttributeValue.stringAttributeValue("string"),
                            "key_int",
                            AttributeValue.longAttributeValue(100),
                            "key_double",
                            AttributeValue.doubleAttributeValue(100.3))))
                .getAttributesList())
        .containsExactly(
            AttributeKeyValue.newBuilder()
                .setKey("key_bool")
                .setBoolValue(true)
                .setType(ValueType.BOOL)
                .build(),
            AttributeKeyValue.newBuilder()
                .setKey("key_string")
                .setStringValue("string")
                .setType(ValueType.STRING)
                .build(),
            AttributeKeyValue.newBuilder()
                .setKey("key_int")
                .setIntValue(100)
                .setType(ValueType.INT)
                .build(),
            AttributeKeyValue.newBuilder()
                .setKey("key_double")
                .setDoubleValue(100.3)
                .setType(ValueType.DOUBLE)
                .build());
  }

  @Test
  public void toProtoResource_Empty() {
    assertThat(ResourceAdapter.toProtoResource(Resource.getEmpty()))
        .isEqualTo(io.opentelemetry.proto.resource.v1.Resource.newBuilder().build());
  }
}
