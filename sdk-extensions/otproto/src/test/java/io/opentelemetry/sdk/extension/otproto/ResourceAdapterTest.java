/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.otproto;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class ResourceAdapterTest {
  @Test
  void toProtoResource() {
    assertThat(
            ResourceAdapter.toProtoResource(
                    Resource.create(
                        Attributes.of(
                            booleanKey("key_bool"),
                            true,
                            stringKey("key_string"),
                            "string",
                            longKey("key_int"),
                            100L,
                            doubleKey("key_double"),
                            100.3)))
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
