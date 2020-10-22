/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace.attributes;

public class EventSemanticAttributes {
  /**
   * The name of an event describing an exception.
   *
   * <p>Typically an event with that name should not be manually created. Instead {@link
   * io.opentelemetry.trace.Span#recordException(Throwable)} should be used.
   */
  public static final String EXCEPTION_EVENT_NAME = "exception";

  private EventSemanticAttributes() {}
}
