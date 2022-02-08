/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An effectively immutable implementation of {@link ExceptionEventData}.
 */
@Immutable
final class ImmutableExceptionEventData implements ExceptionEventData {
  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param exception the {@link Throwable exception} of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @return a new immutable {@code Event<T>}
   */
  static ExceptionEventData create(long epochNanos, Throwable exception, Attributes attributes) {
    return new ImmutableExceptionEventData(epochNanos, exception, attributes);
  }

  private final long epochNanos;
  private final Throwable exception;
  private final Attributes additionalAttributes;
  private final Object lock = new Object();
  @Nullable private volatile Attributes mergedAttributes;

  ImmutableExceptionEventData(
      long epochNanos, Throwable exception, Attributes additionalAttributes) {
    Objects.requireNonNull(exception, "Null exception");
    Objects.requireNonNull(exception, "Null additionalAttributes");
    this.epochNanos = epochNanos;
    this.exception = exception;
    this.additionalAttributes = additionalAttributes;
  }

  @Override
  public String getName() {
    return SemanticAttributes.EXCEPTION_EVENT_NAME;
  }

  @Override
  public Attributes getAttributes() {
    if (mergedAttributes == null) {
      synchronized (lock) {
        if (mergedAttributes == null) {
          mergedAttributes = mergeAttributes(exception, additionalAttributes);
        }
      }
    }
    return mergedAttributes;
  }

  private static Attributes mergeAttributes(Throwable exception, Attributes additionalAttributes) {

    AttributesBuilder attributes = Attributes.builder();
    attributes.put(SemanticAttributes.EXCEPTION_TYPE, exception.getClass().getCanonicalName());
    if (exception.getMessage() != null) {
      attributes.put(SemanticAttributes.EXCEPTION_MESSAGE, exception.getMessage());
    }
    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    attributes.put(SemanticAttributes.EXCEPTION_STACKTRACE, writer.toString());
    attributes.putAll(additionalAttributes);
    return attributes.build();
  }

  @Override
  public long getEpochNanos() {
    return epochNanos;
  }

  @Override
  public int getTotalAttributeCount() {
    return getAttributes().size();
  }

  @Override
  public Throwable getException() {
    return exception;
  }

  @Override
  public Attributes getAdditionalAttributes() {
    return additionalAttributes;
  }

  @Override
  public String toString() {
    return "ImmutableExceptionEventData{"
        + "name=" + SemanticAttributes.EXCEPTION_EVENT_NAME + ", "
        + "exception=" + exception + ", "
        + "additionalAttributes=" + additionalAttributes + ", "
        + "epochNanos=" + epochNanos
        + "}";
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ImmutableExceptionEventData) {
      ImmutableExceptionEventData that = (ImmutableExceptionEventData) o;
      return this.exception.equals(that.getException())
          && this.additionalAttributes.equals(that.getAdditionalAttributes())
          && this.epochNanos == that.getEpochNanos();
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc *= 1000003;
    hc ^= exception.hashCode();
    hc *= 1000003;
    hc ^= additionalAttributes.hashCode();
    hc *= 1000003;
    hc ^= (int) ((epochNanos >>> 32) ^ epochNanos);
    return hc;
  }
}
