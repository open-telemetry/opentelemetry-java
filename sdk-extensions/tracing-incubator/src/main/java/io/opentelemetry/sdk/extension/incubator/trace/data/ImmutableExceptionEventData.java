/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.concurrent.Immutable;

/** An effectively immutable implementation of {@link ExceptionEventData}. */
@AutoValue
@Immutable
abstract class ImmutableExceptionEventData implements ExceptionEventData {

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param exception the {@link Throwable exception} of the {@code Event}.
   * @param additionalAttributes the additional {@link Attributes} of the {@code Event}.
   * @return a new immutable {@code Event<T>}
   */
  static ExceptionEventData create(
      long epochNanos, Throwable exception, Attributes additionalAttributes) {

    AttributesBuilder attributesBuilder = Attributes.builder();
    attributesBuilder.put(
        SemanticAttributes.EXCEPTION_TYPE, exception.getClass().getCanonicalName());
    String message = exception.getMessage();
    if (message != null) {
      attributesBuilder.put(SemanticAttributes.EXCEPTION_MESSAGE, message);
    }

    StringWriter stringWriter = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
      exception.printStackTrace(printWriter);
    }
    attributesBuilder.put(SemanticAttributes.EXCEPTION_STACKTRACE, stringWriter.toString());

    if (additionalAttributes != null) {
      attributesBuilder.putAll(additionalAttributes);
    }
    Attributes attributes = attributesBuilder.build();

    return new AutoValue_ImmutableExceptionEventData(
        SemanticAttributes.EXCEPTION_EVENT_NAME,
        attributes,
        epochNanos,
        attributes.size(),
        exception,
        additionalAttributes);
  }

  ImmutableExceptionEventData() {}
}
