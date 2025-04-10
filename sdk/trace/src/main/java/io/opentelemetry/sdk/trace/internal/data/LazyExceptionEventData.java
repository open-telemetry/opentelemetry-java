/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal.data;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.data.ExceptionEventData;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An {@link ExceptionEventData} implementation with {@link #getAttributes()} lazily evaluated,
 * allowing the (relatively) expensive exception attribute rendering to take place off the hot path.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class LazyExceptionEventData implements ExceptionEventData {

  private static final AttributeKey<String> EXCEPTION_TYPE =
      AttributeKey.stringKey("exception.type");
  private static final AttributeKey<String> EXCEPTION_MESSAGE =
      AttributeKey.stringKey("exception.message");
  private static final AttributeKey<String> EXCEPTION_STACKTRACE =
      AttributeKey.stringKey("exception.stacktrace");
  private static final String EXCEPTION_EVENT_NAME = "exception";

  @Override
  public final String getName() {
    return EXCEPTION_EVENT_NAME;
  }

  // Autowire generates AutoValue_LazyExceptionEventData and $AutoValue_LazyExceptionEventData to
  // account to memoize getAttributes. Unfortunately, $AutoValue_LazyExceptionEventData's
  // exceptionMessage constructor param is properly annotated with @Nullable, but
  // AutoValue_LazyExceptionEventData's is not. So we suppress the NullAway false positive.
  @SuppressWarnings("NullAway")
  public static ExceptionEventData create(
      long epochNanos,
      Throwable exception,
      Attributes additionalAttributes,
      SpanLimits spanLimits) {
    // Compute exception message at initialization time to be conservative about possibility of
    // Exception#geMessage() not being thread safe
    return new AutoValue_LazyExceptionEventData(
        epochNanos, exception, spanLimits, exception.getMessage(), additionalAttributes);
  }

  abstract SpanLimits getSpanLimits();

  @Nullable
  abstract String getExceptionMessage();

  public abstract Attributes getAdditionalAttributes();

  @Override
  @Memoized
  public Attributes getAttributes() {
    Throwable exception = getException();
    SpanLimits spanLimits = getSpanLimits();
    Attributes additionalAttributes = getAdditionalAttributes();

    AttributesMap attributes =
        AttributesMap.create(
            spanLimits.getMaxNumberOfAttributes(), spanLimits.getMaxAttributeValueLength());
    String exceptionName = exception.getClass().getCanonicalName();
    String exceptionMessage = getExceptionMessage();
    StringWriter stringWriter = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
      exception.printStackTrace(printWriter);
    }
    String stackTrace = stringWriter.toString();

    if (exceptionName != null) {
      attributes.put(EXCEPTION_TYPE, exceptionName);
    }
    if (exceptionMessage != null) {
      attributes.put(EXCEPTION_MESSAGE, exceptionMessage);
    }
    if (stackTrace != null) {
      attributes.put(EXCEPTION_STACKTRACE, stackTrace);
    }

    additionalAttributes.forEach(attributes::put);

    return attributes.immutableCopy();
  }

  @Override
  public int getTotalAttributeCount() {
    // getAttributes() lazily adds 3 attributes to getAdditionalAttributes():
    // - exception.type
    // - exception.message
    // - exception.stacktrace
    return getAdditionalAttributes().size() + 3;
  }

  LazyExceptionEventData() {}
}
