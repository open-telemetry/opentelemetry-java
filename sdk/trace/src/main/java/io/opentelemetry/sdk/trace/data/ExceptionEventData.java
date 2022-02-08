/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import io.opentelemetry.api.common.Attributes;

public interface ExceptionEventData extends EventData {

  /**
   * Returns a new immutable {@link ExceptionEventData}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@link ExceptionEventData}.
   * @param exception the {@link Throwable exception} of the {@code Event}.
   * @param additionalAttributes the additional attributes of the {@link ExceptionEventData}.
   * @return a new immutable {@link ExceptionEventData}
   */
  static ExceptionEventData create(
      long epochNanos, Throwable exception, Attributes additionalAttributes) {
    return ImmutableExceptionEventData.create(epochNanos, exception, additionalAttributes);
  }

  /**
   * Return the {@link Throwable exception} of the {@link ExceptionEventData}.
   * @return the {@link Throwable exception} of the {@link ExceptionEventData}
   */
  Throwable getException();

  /**
   * Return the additional {@link Attributes} of the {@link ExceptionEventData}.
   * @return the additional {@link Attributes} of the {@link ExceptionEventData}.
   */
  Attributes getAdditionalAttributes();
}
