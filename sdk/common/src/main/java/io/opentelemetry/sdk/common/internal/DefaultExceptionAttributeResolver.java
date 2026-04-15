/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The default {@link ExceptionAttributeResolver}, populating standard {@code exception.*} as
 * defined by the semantic conventions.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 *
 * @see ExceptionAttributeResolver#getDefault()
 * @see ExceptionAttributeResolver#getDefault(boolean) ()
 */
final class DefaultExceptionAttributeResolver implements ExceptionAttributeResolver {

  static final String ENABLE_JVM_STACKTRACE_PROPERTY = "otel.experimental.sdk.jvm_stacktrace";

  private final boolean jvmStacktraceEnabled;

  DefaultExceptionAttributeResolver(boolean jvmStacktraceEnabled) {
    this.jvmStacktraceEnabled = jvmStacktraceEnabled;
  }

  @Override
  public void setExceptionAttributes(
      AttributeSetter attributeSetter, Throwable throwable, int maxAttributeLength) {
    String exceptionType = throwable.getClass().getCanonicalName();
    if (exceptionType != null) {
      attributeSetter.setAttribute(ExceptionAttributeResolver.EXCEPTION_TYPE, exceptionType);
    }

    String exceptionMessage = throwable.getMessage();
    if (exceptionMessage != null) {
      attributeSetter.setAttribute(ExceptionAttributeResolver.EXCEPTION_MESSAGE, exceptionMessage);
    }

    String exceptionStacktrace =
        jvmStacktraceEnabled
            ? jvmStacktrace(throwable)
            : limitsAwareStacktrace(throwable, maxAttributeLength);
    attributeSetter.setAttribute(
        ExceptionAttributeResolver.EXCEPTION_STACKTRACE, exceptionStacktrace);
  }

  private static String jvmStacktrace(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
      throwable.printStackTrace(printWriter);
    }
    return stringWriter.toString();
  }

  private static String limitsAwareStacktrace(Throwable throwable, int maxAttributeLength) {
    return new StackTraceRenderer(throwable, maxAttributeLength).render();
  }
}
