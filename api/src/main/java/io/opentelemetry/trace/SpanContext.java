/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.trace;

import java.util.Arrays;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span context. A span context contains the state that must propagate to
 * child {@link Span}s and across process boundaries. It contains the identifiers (a {@link TraceId
 * trace_id} and {@link SpanId span_id}) associated with the {@link Span} and a set of {@link
 * TraceOptions options}.
 *
 * @since 0.1.0
 */
@Immutable
public final class SpanContext {
  private final TraceId traceId;
  private final SpanId spanId;
  private final TraceOptions traceOptions;
  private final Tracestate tracestate;

  /**
   * Returns the blank {@code SpanContext} that can be used for no-op operations.
   *
   * @return the blank {@code SpanContext}.
   * @since 0.1.0
   */
  public static SpanContext getBlank() {
    return BLANK;
  }

  private static final SpanContext BLANK =
      new SpanContext(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceOptions.getDefault(),
          Tracestate.getDefault());

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * @param traceId the trace identifier of the span context.
   * @param spanId the span identifier of the span context.
   * @param traceOptions the trace options for the span context.
   * @param tracestate the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @since 0.1.0
   */
  public static SpanContext create(
      TraceId traceId, SpanId spanId, TraceOptions traceOptions, Tracestate tracestate) {
    return new SpanContext(traceId, spanId, traceOptions, tracestate);
  }

  /**
   * Returns the trace identifier associated with this {@code SpanContext}.
   *
   * @return the trace identifier associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public TraceId getTraceId() {
    return traceId;
  }

  /**
   * Returns the span identifier associated with this {@code SpanContext}.
   *
   * @return the span identifier associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public SpanId getSpanId() {
    return spanId;
  }

  /**
   * Returns the {@code TraceOptions} associated with this {@code SpanContext}.
   *
   * @return the {@code TraceOptions} associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public TraceOptions getTraceOptions() {
    return traceOptions;
  }

  /**
   * Returns the {@code Tracestate} associated with this {@code SpanContext}.
   *
   * @return the {@code Tracestate} associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public Tracestate getTracestate() {
    return tracestate;
  }

  /**
   * Returns true if this {@code SpanContext} is valid.
   *
   * @return true if this {@code SpanContext} is valid.
   * @since 0.1.0
   */
  public boolean isValid() {
    return traceId.isValid() && spanId.isValid();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof SpanContext)) {
      return false;
    }

    SpanContext that = (SpanContext) obj;
    return traceId.equals(that.traceId)
        && spanId.equals(that.spanId)
        && traceOptions.equals(that.traceOptions);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[] {traceId, spanId, traceOptions});
  }

  @Override
  public String toString() {
    return "SpanContext{traceId="
        + traceId
        + ", spanId="
        + spanId
        + ", traceOptions="
        + traceOptions
        + "}";
  }

  private SpanContext(
      TraceId traceId, SpanId spanId, TraceOptions traceOptions, Tracestate tracestate) {
    this.traceId = traceId;
    this.spanId = spanId;
    this.traceOptions = traceOptions;
    this.tracestate = tracestate;
  }
}
