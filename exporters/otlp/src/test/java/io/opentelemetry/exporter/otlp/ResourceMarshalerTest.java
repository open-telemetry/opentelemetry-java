/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.CodedOutputStream;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ResourceMarshalerTest {
  @Test
  void customMarshalAndSize() throws IOException {
    Resource resource =
        Resource.create(
            Attributes.builder()
                .put(AttributeKey.booleanKey("key_bool"), true)
                .put(AttributeKey.stringKey("key_string"), "string")
                .put(AttributeKey.longKey("key_int"), 100L)
                .put(AttributeKey.doubleKey("key_double"), 100.3)
                .put(
                    AttributeKey.stringArrayKey("key_string_array"),
                    Arrays.asList("string", "string"))
                .put(AttributeKey.longArrayKey("key_long_array"), Arrays.asList(12L, 23L))
                .put(AttributeKey.doubleArrayKey("key_double_array"), Arrays.asList(12.3, 23.1))
                .put(AttributeKey.booleanArrayKey("key_boolean_array"), Arrays.asList(true, false))
                .put(AttributeKey.booleanKey(""), true)
                .put(AttributeKey.stringKey("null_value"), null)
                .put(AttributeKey.stringKey("empty_value"), "")
                .build());

    io.opentelemetry.proto.resource.v1.Resource proto = ResourceAdapter.toProtoResource(resource);
    int protoSize = proto.getSerializedSize();

    ResourceMarshaler marshaler = ResourceMarshaler.create(resource);
    assertThat(marshaler).isNotNull();
    int customSize = marshaler.getSerializedSize();
    assertThat(customSize).isEqualTo(protoSize);

    ByteBuffer protoOutput = ByteBuffer.allocate(protoSize);
    proto.writeTo(CodedOutputStream.newInstance(protoOutput));

    ByteBuffer customOutput = ByteBuffer.allocate(customSize);
    marshaler.writeTo(CodedOutputStream.newInstance(customOutput));

    assertThat(customOutput).isEqualTo(protoOutput);
  }

  @Test
  void customMarshalAndSize_Empty() {
    assertThat(ResourceMarshaler.create(Resource.getEmpty())).isNull();
  }
}
