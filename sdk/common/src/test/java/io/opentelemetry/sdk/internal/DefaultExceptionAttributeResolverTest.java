/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DefaultExceptionAttributeResolverTest {

  @ParameterizedTest
  @MethodSource("setExceptionAttributesArgs")
  void setExceptionAttributes(
      boolean jvmStacktraceEnabled,
      Throwable throwable,
      int maxAttributeLength,
      String expectedMessage,
      String expectedType,
      Predicate<String> validStacktrace) {
    ExceptionAttributeResolver resolver =
        ExceptionAttributeResolver.getDefault(jvmStacktraceEnabled);
    Map<String, Object> attributes = new HashMap<>();

    resolver.setExceptionAttributes(
        new ExceptionAttributeResolver.AttributeSetter() {
          @Override
          public <T> void setAttribute(AttributeKey<T> key, @Nullable T value) {
            attributes.put(key.toString(), value);
          }
        },
        throwable,
        maxAttributeLength);

    assertThat(attributes.get(ExceptionAttributeResolver.EXCEPTION_MESSAGE.getKey()))
        .isEqualTo(expectedMessage);
    assertThat(attributes.get(ExceptionAttributeResolver.EXCEPTION_TYPE.getKey()))
        .isEqualTo(expectedType);
    assertThat((String) attributes.get(ExceptionAttributeResolver.EXCEPTION_STACKTRACE.getKey()))
        .matches(validStacktrace);
  }

  private static Stream<Arguments> setExceptionAttributesArgs() {
    return Stream.of(
        // When jvmStacktraceEnabled=true, limit is ignored
        Arguments.of(
            true,
            new Exception("error"),
            10,
            "error",
            "java.lang.Exception",
            predicate(stacktrace -> stacktrace.length() > 10)),
        // When jvmStacktraceEnabled=false, limit is adhered
        Arguments.of(
            false,
            new Exception("error"),
            10,
            "error",
            "java.lang.Exception",
            predicate(stacktrace -> stacktrace.length() == 10)));
  }

  private static Predicate<String> predicate(Predicate<String> predicate) {
    return predicate;
  }
}
