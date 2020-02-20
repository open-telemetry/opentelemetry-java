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

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span context. A span context contains the state that must propagate to
 * child {@link Span}s and across process boundaries. It contains the identifiers (a {@link TraceId
 * trace_id} and {@link SpanId span_id}) associated with the {@link Span} and a set of {@link
 * TraceFlags options}, as well as the {@link TraceState traceState} and the {@link boolean remote}
 * flag.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class SpanContext {

  private static final SpanContext INVALID =
      create(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.getDefault(),
          TraceState.getDefault());

  /**
   * Returns the invalid {@code SpanContext} that can be used for no-op operations.
   *
   * @return the invalid {@code SpanContext}.
   */
  public static SpanContext getInvalid() {
    return INVALID;
  }

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * @param traceId the trace identifier of the span context.
   * @param spanId the span identifier of the span context.
   * @param traceFlags the trace options for the span context.
   * @param traceState the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @since 0.1.0
   */
  public static SpanContext create(
      TraceId traceId, SpanId spanId, TraceFlags traceFlags, TraceState traceState) {
    return new AutoValue_SpanContext(traceId, spanId, traceFlags, traceState, /* remote=*/ false);
  }

  /**
   * Creates a new {@code SpanContext} that was propagated from a remote parent, with the given
   * identifiers and options.
   *
   * @param traceId the trace identifier of the span context.
   * @param spanId the span identifier of the span context.
   * @param traceFlags the trace options for the span context.
   * @param traceState the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @since 0.1.0
   */
  public static SpanContext createFromRemoteParent(
      TraceId traceId, SpanId spanId, TraceFlags traceFlags, TraceState traceState) {
    return new AutoValue_SpanContext(traceId, spanId, traceFlags, traceState, /* remote=*/ true);
  }

  /**
   * Returns the trace identifier associated with this {@code SpanContext}.
   *
   * @return the trace identifier associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public abstract TraceId getTraceId();

  /**
   * Returns the span identifier associated with this {@code SpanContext}.
   *
   * @return the span identifier associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public abstract SpanId getSpanId();

  /**
   * Returns the {@code TraceFlags} associated with this {@code SpanContext}.
   *
   * @return the {@code TraceFlags} associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public abstract TraceFlags getTraceFlags();

  /**
   * Returns the {@code TraceState} associated with this {@code SpanContext}.
   *
   * @return the {@code TraceState} associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public abstract TraceState getTraceState();

  /**
   * Returns {@code true} if this {@code SpanContext} is valid.
   *
   * @return {@code true} if this {@code SpanContext} is valid.
   * @since 0.1.0
   */
  public boolean isValid() {
    return getTraceId().isValid() && getSpanId().isValid();
  }

  /**
   * Returns {@code true} if the {@code SpanContext} was propagated from a remote parent.
   *
   * @return {@code true} if the {@code SpanContext} was propagated from a remote parent.
   * @since 0.1.0
   */
  public abstract boolean isRemote();
}
