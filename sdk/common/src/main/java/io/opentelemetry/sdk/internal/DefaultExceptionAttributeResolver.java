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
    attributeSetter.setAttribute(
        ExceptionAttributeResolver.EXCEPTION_TYPE, throwable.getClass().getCanonicalName());
    String message = throwable.getMessage();
    if (message != null) {
      attributeSetter.setAttribute(ExceptionAttributeResolver.EXCEPTION_MESSAGE, message);
    }
    StringWriter stringWriter = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
      throwable.printStackTrace(printWriter);
    }
    attributeSetter.setAttribute(
        ExceptionAttributeResolver.EXCEPTION_STACKTRACE, stringWriter.toString());
  }
}
