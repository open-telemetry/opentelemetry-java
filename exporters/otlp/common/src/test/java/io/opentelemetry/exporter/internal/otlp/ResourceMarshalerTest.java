/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ResourceMarshalerTest {

  @Test
  void marshalResourceWithEntities() {
    Entity entity =
        Entity.builder("process", Attributes.of(stringKey("process.pid"), "1234"))
            .setDescription(Attributes.of(stringKey("process.executable.name"), "java"))
            .setSchemaUrl("http://process.schema")
            .build();

    ResourceBuilder builder =
        io.opentelemetry.sdk.resources.Resource.builder().put("service.name", "my-service");
    EntityUtil.addEntity(builder, entity);
    io.opentelemetry.sdk.resources.Resource resourceWithEntity = builder.build();

    Resource proto =
        parse(Resource.getDefaultInstance(), ResourceMarshaler.create(resourceWithEntity));

    assertThat(proto.getAttributesList()).hasSize(3);
    assertThat(proto.getAttributesList().stream().map(a -> a.getKey()))
        .containsExactlyInAnyOrder("service.name", "process.pid", "process.executable.name");

    assertThat(proto.getEntityRefsList()).hasSize(1);
    assertThat(proto.getEntityRefs(0).getType()).isEqualTo("process");
    assertThat(proto.getEntityRefs(0).getSchemaUrl()).isEqualTo("http://process.schema");
    assertThat(proto.getEntityRefs(0).getIdKeysList()).containsExactly("process.pid");
    assertThat(proto.getEntityRefs(0).getDescriptionKeysList())
        .containsExactly("process.executable.name");
  }

  @SuppressWarnings("unchecked")
  private static <T extends Message> T parse(T prototype, Marshaler marshaler) {
    byte[] serialized = toByteArray(marshaler);
    T result;
    try {
      result = (T) prototype.newBuilderForType().mergeFrom(serialized).build();
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }
    assertThat(result.getSerializedSize()).isEqualTo(serialized.length);

    // Compare JSON
    String json = toJson(marshaler);
    Message.Builder protoBuilder = prototype.newBuilderForType();
    try {
      JsonFormat.parser().merge(json, protoBuilder);
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }
    assertThat(protoBuilder.build()).isEqualTo(result);

    return result;
  }

  private static byte[] toByteArray(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeBinaryTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bos.toByteArray();
  }

  private static String toJson(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeJsonTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new String(bos.toByteArray(), StandardCharsets.UTF_8);
  }
}
