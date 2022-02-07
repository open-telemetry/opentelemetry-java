/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.io.PrintWriter;
import java.io.StringWriter;

enum DefaultSpanExceptionRecorder implements SpanExceptionRecorder {
  INSTANCE;

  @Override
  public AttributesBuilder recordException(
      Throwable exception, AttributesBuilder attributesBuilder) {

    attributesBuilder.put(
        SemanticAttributes.EXCEPTION_TYPE, exception.getClass().getCanonicalName());
    if (exception.getMessage() != null) {
      attributesBuilder.put(SemanticAttributes.EXCEPTION_MESSAGE, exception.getMessage());
    }
    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    attributesBuilder.put(SemanticAttributes.EXCEPTION_STACKTRACE, writer.toString());

    return attributesBuilder;
  }
}
