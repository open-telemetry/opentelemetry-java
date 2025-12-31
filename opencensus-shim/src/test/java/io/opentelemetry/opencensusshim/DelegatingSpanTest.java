/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

/**
 * Ensures all methods for {@link Span} are appropriately proxied by {@link OpenTelemetrySpanImpl},
 * further ensuring the shim behaves as expected when under otel javaagent instrumentation.
 *
 * <p>This addresses a regression between the shim as used with the Otel SDK on the app classloader
 * and as instrumented via the javaagent. Details <a
 * href="https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/6876">here</a>.
 */
class DelegatingSpanTest {

  /*
  Verifies all methods on the otel Span interface are under test.
  Each case is enumerated in proxyMethodsProvider() to avoid false-positives (bad reflection) and maximize
  flexibility for special cases (e.g. getSpanContext() and isRecording())
   */
  @Test
  void verifyAllMethodsAreUnderTest() {
    List<Method> methods =
        delegateMethodsProvider()
            .map(
                pm -> {
                  try {
                    return getInterfaceMethod(
                        Span.class, (String) pm.get()[0], (Class<?>[]) pm.get()[1]);
                  } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toList());

    assertThat(methods)
        .describedAs("all interface methods are being tested")
        .containsAll(allInterfaceMethods(Span.class));
    //    assertThat(allInterfaceMethods(Span.class))
    //        .describedAs("all tested methods are on the Span interface")
    //        .containsAll(methods);
  }

  @ParameterizedTest
  @MethodSource("delegateMethodsProvider")
  void testit(String name, Class<?>[] params, VerificationMode timesCalled)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = getInterfaceMethod(Span.class, name, params);
    Span mockedSpan = Mockito.spy(Span.current());
    OpenTelemetrySpanImpl span = new OpenTelemetrySpanImpl(mockedSpan);
    assertDelegated(span, mockedSpan, method, timesCalled);
  }

  static List<Method> allInterfaceMethods(Class<?> clazz) {
    return Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()))
        .collect(Collectors.toList());
  }

  static Stream<Arguments> delegateMethodsProvider() {
    return Stream.of(
        Arguments.of("end", new Class<?>[] {}, times(1)),
        Arguments.of("end", new Class<?>[] {long.class, TimeUnit.class}, times(1)),
        Arguments.of("end", new Class<?>[] {Instant.class}, times(1)),
        Arguments.of("setAttribute", new Class<?>[] {String.class, String.class}, times(1)),
        Arguments.of("setAttribute", new Class<?>[] {AttributeKey.class, int.class}, times(1)),
        Arguments.of("setAttribute", new Class<?>[] {AttributeKey.class, Object.class}, times(1)),
        Arguments.of("setAttribute", new Class<?>[] {String.class, long.class}, times(1)),
        Arguments.of("setAttribute", new Class<?>[] {String.class, double.class}, times(1)),
        Arguments.of("setAttribute", new Class<?>[] {String.class, boolean.class}, times(1)),
        Arguments.of(
            "recordException", new Class<?>[] {Throwable.class, Attributes.class}, times(1)),
        Arguments.of("recordException", new Class<?>[] {Throwable.class}, times(1)),
        Arguments.of("setAllAttributes", new Class<?>[] {Attributes.class}, times(1)),
        Arguments.of("updateName", new Class<?>[] {String.class}, times(1)),
        Arguments.of("storeInContext", new Class<?>[] {Context.class}, times(1)),
        Arguments.of("addEvent", new Class<?>[] {String.class, Instant.class}, times(1)),
        Arguments.of(
            "addEvent", new Class<?>[] {String.class, long.class, TimeUnit.class}, times(1)),
        Arguments.of(
            "addEvent", new Class<?>[] {String.class, Attributes.class, Instant.class}, times(1)),
        Arguments.of("addEvent", new Class<?>[] {String.class}, times(1)),
        Arguments.of(
            "addEvent",
            new Class<?>[] {String.class, Attributes.class, long.class, TimeUnit.class},
            times(1)),
        Arguments.of("addEvent", new Class<?>[] {String.class, Attributes.class}, times(1)),
        Arguments.of("setStatus", new Class<?>[] {StatusCode.class, String.class}, times(1)),
        Arguments.of("setStatus", new Class<?>[] {StatusCode.class}, times(1)),
        //
        // special cases
        //
        // called never -- it's shared between OC and Otel Span types and is always true, so returns
        // `true`
        Arguments.of("isRecording", new Class<?>[] {}, times(0)),
        // called twice: once in constructor, then once during delegation
        Arguments.of("getSpanContext", new Class<?>[] {}, times(2)),
        // addLink is never called
        Arguments.of("addLink", new Class<?>[] {SpanContext.class}, times(0)),
        Arguments.of("addLink", new Class<?>[] {SpanContext.class, Attributes.class}, times(0)));
  }

  // gets default values for all cases, as mockito can't mock wrappers or primitives, including
  // String
  static Object valueLookup(Class<?> clazz) {
    if (clazz == int.class || clazz == Integer.class) {
      return Integer.valueOf(0).intValue();
    } else if (clazz == long.class || clazz == Long.class) {
      return Long.valueOf(0L).longValue();
    } else if (clazz == double.class || clazz == Double.class) {
      return Double.valueOf(0d).doubleValue();
    } else if (clazz == char.class || clazz == Character.class) {
      return Character.valueOf('\0').charValue();
    } else if (clazz == boolean.class || clazz == Boolean.class) {
      return Boolean.valueOf(false).booleanValue();
    } else if (clazz == float.class || clazz == Float.class) {
      return Float.valueOf(0f).floatValue();
    } else if (clazz == String.class) {
      return "";
    } else if (clazz == byte.class || clazz == Byte.class) {
      return Byte.valueOf((byte) 0).byteValue();
    } else if (clazz == short.class || clazz == Short.class) {
      return Short.valueOf((short) 0).shortValue();
    } else {
      return Mockito.mock(clazz);
    }
  }

  static <T> Method getInterfaceMethod(Class<T> clazz, String name, Class<?>[] params)
      throws NoSuchMethodException {
    Method method = clazz.getMethod(name, params);
    assertThat(method).isNotNull();
    return method;
  }

  static <T> void assertDelegated(T proxy, T delegate, Method method, VerificationMode mode)
      throws InvocationTargetException, IllegalAccessException {
    // Get parameters for method
    Class<?>[] parameterTypes = method.getParameterTypes();
    Object[] arguments = new Object[parameterTypes.length];
    for (int j = 0; j < arguments.length; j++) {
      arguments[j] = valueLookup(parameterTypes[j]);
    }

    // Invoke wrapper method
    method.invoke(proxy, arguments);

    // Ensure method was called on delegate exactly once with the correct arguments
    method.invoke(Mockito.verify(delegate, mode), arguments);
  }
}
