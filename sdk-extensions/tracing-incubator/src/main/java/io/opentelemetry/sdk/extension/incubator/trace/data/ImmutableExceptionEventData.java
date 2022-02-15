/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.data;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
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

    return new AutoValue_ImmutableExceptionEventData(epochNanos, exception, additionalAttributes);
  }

  ImmutableExceptionEventData() {}

  @Override
  public String getName() {
    return SemanticAttributes.EXCEPTION_EVENT_NAME;
  }

  @Override
  @Memoized
  public Attributes getAttributes() {
    Throwable exception = getException();
    Attributes additionalAttributes = getAdditionalAttributes();
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
    attributesBuilder.putAll(additionalAttributes);

    return attributesBuilder.build();
  }

  @Override
  @Memoized
  public int getTotalAttributeCount() {
    return getAttributes().size();
  }
}
