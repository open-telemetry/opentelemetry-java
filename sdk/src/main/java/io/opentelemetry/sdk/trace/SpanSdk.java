package io.opentelemetry.sdk.trace;

import io.opentelemetry.trace.Span;

/** The extend Span interface used by the SDK. */
public interface SpanSdk extends Span {

  /**
   * Returns the name of the {@code Span}.
   *
   * <p>The name can be changed during the lifetime of the Span by using the {@link
   * Span#updateName(String)} so this value cannot be cached.
   *
   * @return the name of the {@code Span}.
   */
  String getName();

  /**
   * Returns the proto representation of the collected data for this particular {@code Span}.
   *
   * @return the proto representation of the collected data for this particular {@code Span}.
   */
  openconsensus.proto.trace.v1.Span toSpanProto();
}
