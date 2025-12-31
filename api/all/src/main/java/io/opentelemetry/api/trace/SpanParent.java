package io.opentelemetry.api.trace;

import io.opentelemetry.api.common.Attributes;

public interface SpanParent {

  /**
   * Records information about the {@link Throwable} to the {@link Span}.
   *
   * @param exception the {@link Throwable} to record.
   * @param additionalAttributes the additional {@link Attributes} to record.
   * @return this.
   */
  Span recordException(Throwable exception, Attributes additionalAttributes);
}
