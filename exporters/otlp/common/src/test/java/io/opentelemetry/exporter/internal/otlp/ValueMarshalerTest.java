/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import static io.opentelemetry.api.common.Value.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValueList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("BadImport")
class ValueMarshalerTest {

  @ParameterizedTest
  @MethodSource("serializeAnyValueArgs")
  void anyValueString_StatefulMarshaler(Value<?> value, AnyValue expectedSerializedValue) {
    MarshalerWithSize marshaler = AnyValueMarshaler.create(value);
    AnyValue serializedValue = parse(AnyValue.getDefaultInstance(), marshaler);
    assertThat(serializedValue).isEqualTo(expectedSerializedValue);
  }

  @ParameterizedTest
  @MethodSource("serializeAnyValueArgs")
  void anyValueString_StatelessMarshaler(Value<?> value, AnyValue expectedSerializedValue) {
    Marshaler marshaler = createMarshaler(AnyValueStatelessMarshaler.INSTANCE, value);
    AnyValue serializedValue = parse(AnyValue.getDefaultInstance(), marshaler);
    assertThat(serializedValue).isEqualTo(expectedSerializedValue);
  }

  private static Stream<Arguments> serializeAnyValueArgs() {
    return Stream.of(
        // primitives
        arguments(of("str"), AnyValue.newBuilder().setStringValue("str").build()),
        arguments(of(true), AnyValue.newBuilder().setBoolValue(true).build()),
        arguments(of(1), AnyValue.newBuilder().setIntValue(1).build()),
        arguments(of(1.1), AnyValue.newBuilder().setDoubleValue(1.1).build()),
        // heterogeneous array
        arguments(
            of(of("str"), of(true), of(1), of(1.1)),
            AnyValue.newBuilder()
                .setArrayValue(
                    ArrayValue.newBuilder()
                        .addValues(AnyValue.newBuilder().setStringValue("str").build())
                        .addValues(AnyValue.newBuilder().setBoolValue(true).build())
                        .addValues(AnyValue.newBuilder().setIntValue(1).build())
                        .addValues(AnyValue.newBuilder().setDoubleValue(1.1).build())
                        .build())
                .build()),
        // map
        arguments(
            of(KeyValue.of("key1", of("val1")), KeyValue.of("key2", of(2))),
            AnyValue.newBuilder()
                .setKvlistValue(
                    KeyValueList.newBuilder()
                        .addValues(
                            io.opentelemetry.proto.common.v1.KeyValue.newBuilder()
                                .setKey("key1")
                                .setValue(AnyValue.newBuilder().setStringValue("val1").build())
                                .build())
                        .addValues(
                            io.opentelemetry.proto.common.v1.KeyValue.newBuilder()
                                .setKey("key2")
                                .setValue(AnyValue.newBuilder().setIntValue(2).build())
                                .build())
                        .build())
                .build()),
        // map of maps
        arguments(
            of(
                Collections.singletonMap(
                    "child", of(Collections.singletonMap("grandchild", of("str"))))),
            AnyValue.newBuilder()
                .setKvlistValue(
                    KeyValueList.newBuilder()
                        .addValues(
                            io.opentelemetry.proto.common.v1.KeyValue.newBuilder()
                                .setKey("child")
                                .setValue(
                                    AnyValue.newBuilder()
                                        .setKvlistValue(
                                            KeyValueList.newBuilder()
                                                .addValues(
                                                    io.opentelemetry.proto.common.v1.KeyValue
                                                        .newBuilder()
                                                        .setKey("grandchild")
                                                        .setValue(
                                                            AnyValue.newBuilder()
                                                                .setStringValue("str")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()),
        // bytes
        arguments(
            of("hello world".getBytes(StandardCharsets.UTF_8)),
            AnyValue.newBuilder()
                .setBytesValue(ByteString.copyFrom("hello world".getBytes(StandardCharsets.UTF_8)))
                .build()),
        // empty values
        arguments(of(""), AnyValue.newBuilder().setStringValue("").build()),
        arguments(of(false), AnyValue.newBuilder().setBoolValue(false).build()),
        arguments(of(0), AnyValue.newBuilder().setIntValue(0).build()),
        arguments(of(0.0), AnyValue.newBuilder().setDoubleValue(0.0).build()),
        arguments(
            Value.of(Collections.emptyList()),
            AnyValue.newBuilder().setArrayValue(ArrayValue.newBuilder().build()).build()),
        arguments(
            of(Collections.emptyMap()),
            AnyValue.newBuilder().setKvlistValue(KeyValueList.newBuilder().build()).build()),
        arguments(of(new byte[0]), AnyValue.newBuilder().setBytesValue(ByteString.EMPTY).build())
        // TODO add test for true empty value
        // after https://github.com/open-telemetry/opentelemetry-java/pull/7973
        // arguments(Value.empty(), AnyValue.newBuilder().build())
        );
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
    // Our marshaler should produce the exact same length of serialized output (for example, field
    // default values are not outputted), so we check that here. The output itself may have slightly
    // different ordering, mostly due to the way we don't output oneof values in field order all the
    // tieme. If the lengths are equal and the resulting protos are equal, the marshaling is
    // guaranteed to be valid.
    assertThat(result.getSerializedSize()).isEqualTo(serialized.length);

    // We don't compare JSON strings due to some differences (particularly serializing enums as
    // numbers instead of names). This may improve in the future but what matters is what we produce
    // can be parsed.
    String json = toJson(marshaler);
    Message.Builder builder = prototype.newBuilderForType();
    try {
      JsonFormat.parser().merge(json, builder);
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }

    assertThat(builder.build()).isEqualTo(result);

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

  private static <T> Marshaler createMarshaler(StatelessMarshaler<T> marshaler, T data) {
    return new Marshaler() {
      private final MarshalerContext context = new MarshalerContext();
      private final int size = marshaler.getBinarySerializedSize(data, context);

      @Override
      public int getBinarySerializedSize() {
        return size;
      }

      @Override
      protected void writeTo(Serializer output) throws IOException {
        context.resetReadIndex();
        marshaler.writeTo(output, data, context);
      }
    };
  }
}
