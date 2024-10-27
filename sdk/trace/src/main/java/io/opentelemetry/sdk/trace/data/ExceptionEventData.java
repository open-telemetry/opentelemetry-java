/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

/** Data representation of an event for a recorded exception. */
@Immutable
public interface ExceptionEventData extends EventData {

  /**
   * Returns a new immutable {@link ExceptionEventData}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@link ExceptionEventData}.
   * @param exception the {@link Throwable exception} of the {@code Event}.
   * @param attributes the additional attributes of the {@link ExceptionEventData}.
   * @return a new immutable {@link ExceptionEventData}
   */
  static ExceptionEventData create(long epochNanos, Throwable exception, Attributes attributes) {
    return ImmutableExceptionEventData.create(epochNanos, exception, attributes);
  }

  /**
   * Returns a new immutable {@link ExceptionEventData}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@link ExceptionEventData}.
   * @param exception the {@link Throwable exception} of the {@code Event}.
   * @param attributes the additional attributes of the {@link ExceptionEventData}.
   * @param totalAttributeCount the total number of attributes for this {@code} Event.
   * @return a new immutable {@link ExceptionEventData}
   */
  static ExceptionEventData create(
      long epochNanos, Throwable exception, Attributes attributes, int totalAttributeCount) {
    return ImmutableExceptionEventData.create(
        epochNanos, exception, attributes, totalAttributeCount);
  }

  /**
   * Return the {@link Throwable exception} of the {@link ExceptionEventData}.
   *
   * @return the {@link Throwable exception} of the {@link ExceptionEventData}
   */
  Throwable getException();
}
