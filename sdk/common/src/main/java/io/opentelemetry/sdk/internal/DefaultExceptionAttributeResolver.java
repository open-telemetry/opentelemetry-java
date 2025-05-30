/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class DefaultExceptionAttributeResolver implements ExceptionAttributeResolver {

  private static final DefaultExceptionAttributeResolver INSTANCE =
      new DefaultExceptionAttributeResolver();

  private DefaultExceptionAttributeResolver() {}

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

    StringWriter stringWriter = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
      throwable.printStackTrace(printWriter);
    }
    String exceptionStacktrace = stringWriter.toString();
    if (exceptionStacktrace != null) {
      attributeSetter.setAttribute(
          ExceptionAttributeResolver.EXCEPTION_STACKTRACE, exceptionStacktrace);
    }
  }
}
