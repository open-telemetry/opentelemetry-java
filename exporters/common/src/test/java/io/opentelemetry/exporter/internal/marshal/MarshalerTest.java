/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MarshalerTest {

  @Test
  void writeTo_NoSelfSuppressionError() throws IOException {
    Marshaler marshaler =
        new Marshaler() {
          @Override
          public int getBinarySerializedSize() {
            return 0;
          }

          @Override
          protected void writeTo(Serializer output) throws IOException {
            for (int i = 0; i < (50 * 1024 + 100) / 8; i++) {
              output.writeFixed64Value(i);
            }
          }
        };
    OutputStream os = mock(OutputStream.class);

    IOException error = new IOException("error!");
    doThrow(error).when(os).write(any(), anyInt(), anyInt());
    doThrow(error).when(os).write(any());
    doThrow(error).when(os).write(anyInt());
    doThrow(error).when(os).flush();

    // If an exception is thrown writing bytes, and that same exception is thrown in the #close
    // method cleaning up the AutoCloseable resource, an IllegalArgumentException will be thrown
    // indicating illegal self suppression. Ensure an IOException is thrown instead.
    assertThatThrownBy(() -> marshaler.writeBinaryTo(os)).isInstanceOf(IOException.class);
    assertThatThrownBy(() -> marshaler.writeJsonTo(os)).isInstanceOf(IOException.class);
  }

  /**
   * This test ensures that instances where serializer produces runtime exceptions are properly
   * converted back to checked {@link IOException}.
   *
   * <p>At various points in {@link Serializer}, we use {@code .forEach}-style methods which accept
   * {@link java.util.function.Consumer} and {@link java.util.function.BiConsumer}. These consumers
   * that any checked exceptions are caught and rethrown as runtime exceptions. Its essential that
   * these runtime exceptions are re-converted back to checked {@link IOException} because calling
   * code's error handling is built on top of {@link IOException}. Failure to convert to {@link
   * IOException} leads to issues like this: <a
   * href="https://github.com/open-telemetry/opentelemetry-java/issues/6946">#6946</a>.
   */
  @ParameterizedTest
  @MethodSource("writeToArgs")
  void writeTo_NoRuntimeExceptions(Writer writer) throws IOException {
    Marshaler marshaler =
        new Marshaler() {
          @Override
          public int getBinarySerializedSize() {
            return 0;
          }

          @Override
          protected void writeTo(Serializer output) throws IOException {
            writer.writeTo(output);
          }
        };

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      assertThatThrownBy(() -> marshaler.writeBinaryTo(baos)).isInstanceOf(IOException.class);
    }
  }

  private static Stream<Arguments> writeToArgs() {
    ProtoFieldInfo fieldInfo = ProtoFieldInfo.create(0, 0, "name");
    MarshalerContext context = new MarshalerContext();
    ThrowingMarshaler<String> throwingMarshaler = new ThrowingMarshaler<>(new IOException("error"));
    ThrowingMarshaler2<String, String> throwingMarshaler2 =
        new ThrowingMarshaler2<>(new IOException("error"));
    ThrowingMarshaler2<AttributeKey<?>, Object> throwingMarshaler2Attributes =
        new ThrowingMarshaler2<>(new IOException("error"));

    return Stream.of(
        Arguments.of(
            asWriter(
                output ->
                    output.serializeRepeatedMessageWithContext(
                        fieldInfo,
                        Collections.singleton("value"),
                        throwingMarshaler,
                        context,
                        MarshalerContext.key()))),
        Arguments.of(
            asWriter(
                output ->
                    output.serializeRepeatedMessageWithContext(
                        fieldInfo,
                        Collections.singletonMap("key", "value"),
                        throwingMarshaler2,
                        context,
                        MarshalerContext.key()))),
        Arguments.of(
            asWriter(
                output ->
                    output.serializeRepeatedMessageWithContext(
                        fieldInfo,
                        Attributes.builder().put("key", "value").build(),
                        throwingMarshaler2Attributes,
                        context))));
  }

  private static Writer asWriter(Writer writer) {
    return writer;
  }

  @FunctionalInterface
  private interface Writer {
    void writeTo(Serializer output) throws IOException;
  }

  private static class ThrowingMarshaler<T> implements StatelessMarshaler<T> {

    private final IOException exception;

    private ThrowingMarshaler(IOException exception) {
      this.exception = exception;
    }

    @Override
    public int getBinarySerializedSize(T value, MarshalerContext context) {
      return 0;
    }

    @Override
    public void writeTo(Serializer output, T value, MarshalerContext context) throws IOException {
      throw exception;
    }
  }

  private static class ThrowingMarshaler2<K, V> implements StatelessMarshaler2<K, V> {

    private final IOException exception;

    private ThrowingMarshaler2(IOException exception) {
      this.exception = exception;
    }

    @Override
    public int getBinarySerializedSize(K key, V value, MarshalerContext context) {
      return 0;
    }

    @Override
    public void writeTo(Serializer output, K key, V value, MarshalerContext context)
        throws IOException {
      throw exception;
    }
  }
}
