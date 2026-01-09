/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StackTraceRendererTest {

  private static final Random random = new Random();

  @ParameterizedTest
  @MethodSource("renderStacktraceArgs")
  void renderStacktrace_randomLength(Throwable throwable) {
    // Test equal stacktrace for random lengths to test edges
    for (int i = 0; i < 100; i++) {
      int length = random.nextInt(10_000);
      assertThat(new StackTraceRenderer(throwable, length).render())
          .isEqualTo(jdkStackTrace(throwable, length));
    }
  }

  @ParameterizedTest
  @MethodSource("renderStacktraceArgs")
  void renderStacktrace_fixedLengths(Throwable throwable) {
    assertThat(new StackTraceRenderer(throwable, 10).render())
        .isEqualTo(jdkStackTrace(throwable, 10));
    assertThat(new StackTraceRenderer(throwable, 100).render())
        .isEqualTo(jdkStackTrace(throwable, 100));
    assertThat(new StackTraceRenderer(throwable, 1000).render())
        .isEqualTo(jdkStackTrace(throwable, 1000));
    assertThat(new StackTraceRenderer(throwable, Integer.MAX_VALUE).render())
        .isEqualTo(jdkStackTrace(throwable, Integer.MAX_VALUE));
  }

  private static Stream<Arguments> renderStacktraceArgs() {
    Exception withCycle = new Exception("error1");
    Exception withCycleInner = new Exception("error2", withCycle);
    withCycle.initCause(withCycleInner);

    Exception withSuppressed = new Exception("error");
    withSuppressed.addSuppressed(new Exception("suppressed"));

    Exception withMultipleSuppressed = new Exception("error");
    withMultipleSuppressed.addSuppressed(new Exception("suppressed1"));
    withMultipleSuppressed.addSuppressed(new Exception("suppressed2"));

    Exception withNestedSuppressed = new Exception("error");
    withNestedSuppressed.addSuppressed(withSuppressed);
    withNestedSuppressed.addSuppressed(withMultipleSuppressed);

    Exception withKitchenSink = new Exception("kitchenSink", withCycle);
    withKitchenSink.addSuppressed(withMultipleSuppressed);
    withKitchenSink.addSuppressed(withNestedSuppressed);

    return Stream.of(
        // simple
        Arguments.of(new Exception()),
        Arguments.of(new Exception("error")),
        // with cause
        Arguments.of(new Exception(new Exception("cause"))),
        Arguments.of(new Exception("error", new Exception("cause"))),
        // with nested causes
        Arguments.of(
            new Exception(
                "error",
                new Exception(
                    "cause1",
                    new Exception(
                        "cause2",
                        new Exception(
                            "cause3", new Exception("cause4", new Exception("cause5"))))))),
        // with cause with circular reference
        Arguments.of(withCycle),
        // with suppressed
        Arguments.of(withSuppressed),
        Arguments.of(withMultipleSuppressed),
        Arguments.of(withNestedSuppressed),
        // with cause, cycle, and suppressed!
        Arguments.of(withKitchenSink));
  }

  private static String jdkStackTrace(Throwable exception, int limit) {
    StringWriter stringWriter = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
      exception.printStackTrace(printWriter);
    }
    String stacktrace = stringWriter.toString();
    return stacktrace.substring(0, Math.min(stacktrace.length(), limit));
  }
}
