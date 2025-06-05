/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.internal.ConfigUtil;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class DefaultExceptionAttributeResolver implements ExceptionAttributeResolver {

  private static final String ENABLE_JVM_STACKTRACE_PROPERTY =
      "otel.experimental.sdk.jvm_stacktrace";

  private static final DefaultExceptionAttributeResolver INSTANCE =
      new DefaultExceptionAttributeResolver(
          Boolean.parseBoolean(ConfigUtil.getString(ENABLE_JVM_STACKTRACE_PROPERTY, "false")));

  private final boolean jvmStacktraceEnabled;

  // Visible for testing
  DefaultExceptionAttributeResolver(boolean jvmStacktraceEnabled) {
    this.jvmStacktraceEnabled = jvmStacktraceEnabled;
  }

  public static ExceptionAttributeResolver getInstance() {
    return INSTANCE;
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
