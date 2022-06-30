/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.data.EventData;

/**
 * Data representation of an event for a recorded exception.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ExceptionEventData extends EventData {

  /**
   * Returns a new immutable {@link ExceptionEventData}.
   *
   * @param spanLimits limits applied to {@link ExceptionEventData}.
   * @param epochNanos epoch timestamp in nanos of the {@link ExceptionEventData}.
   * @param exception the {@link Throwable exception} of the {@code Event}.
   * @param additionalAttributes the additional attributes of the {@link ExceptionEventData}.
   * @return a new immutable {@link ExceptionEventData}
   */
  static ExceptionEventData create(
      SpanLimits spanLimits,
      long epochNanos,
      Throwable exception,
      Attributes additionalAttributes) {
    return ImmutableExceptionEventData.create(
        spanLimits, epochNanos, exception, additionalAttributes);
  }

  /**
   * Return the {@link Throwable exception} of the {@link ExceptionEventData}.
   *
   * @return the {@link Throwable exception} of the {@link ExceptionEventData}
   */
  Throwable getException();

  /**
   * Return the additional {@link Attributes attributes} of the {@link ExceptionEventData}.
   *
   * @return the additional {@link Attributes attributes} of the {@link ExceptionEventData}
   */
  Attributes getAdditionalAttributes();
}
