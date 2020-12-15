/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.otproto;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceAttributes;
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
                            ResourceAttributes.SERVICE_NAME,
                            "myservice.name",
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
                .setKey("service.name")
                .setValue(AnyValue.newBuilder().setStringValue("myservice.name").build())
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
    io.opentelemetry.proto.resource.v1.Resource emptyProtoResource =
        ResourceAdapter.toProtoResource(Resource.getEmpty());
    // we get one from the fallback service name
    assertThat(emptyProtoResource.getAttributesCount()).isEqualTo(1);
  }
}
