/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.Nullable;

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
  @Nullable
  public String getExceptionType(Throwable throwable) {
    return throwable.getClass().getCanonicalName();
  }

  @Override
  @Nullable
  public String getExceptionMessage(Throwable throwable) {
    return throwable.getMessage();
  }

  @Override
  @Nullable
  public String getExceptionStacktrace(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
      throwable.printStackTrace(printWriter);
    }
    return stringWriter.toString();
  }
}
