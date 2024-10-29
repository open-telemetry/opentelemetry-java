/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

/** An effectively immutable implementation of {@link ExceptionEventData}. */
@AutoValue
@Immutable
abstract class ImmutableExceptionEventData implements ExceptionEventData {

  private static final String EXCEPTION_EVENT_NAME = "exception";

  @Override
  public final String getName() {
    return EXCEPTION_EVENT_NAME;
  }

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param exception the {@link Throwable exception} of the {@code Event}.
   * @param attributes the additional {@link Attributes} of the {@code Event}.
   * @return a new immutable {@code Event<T>}
   */
  static ExceptionEventData create(long epochNanos, Throwable exception, Attributes attributes) {
    return create(epochNanos, exception, attributes, attributes.size());
  }

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param exception the {@link Throwable exception} of the {@code Event}.
   * @param attributes the additional {@link Attributes} of the {@code Event}.
   * @param totalAttributeCount the total number of attributes for this {@code} Event.
   * @return a new immutable {@code Event<T>}
   */
  static ExceptionEventData create(
      long epochNanos, Throwable exception, Attributes attributes, int totalAttributeCount) {
    return new AutoValue_ImmutableExceptionEventData(
        attributes, epochNanos, totalAttributeCount, exception);
  }

  ImmutableExceptionEventData() {}
}
