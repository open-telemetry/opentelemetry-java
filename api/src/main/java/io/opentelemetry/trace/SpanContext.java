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
import com.google.auto.value.extension.memoized.Memoized;
import javax.annotation.Nullable;
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
      CharSequence traceId, CharSequence spanId, TraceFlags traceFlags, TraceState traceState) {
    return create(traceId, 0, spanId, 0, traceFlags, traceState, /* remote=*/ false);
  }

  /**
   * todo: javadoc me. This one has all the options for how ids are represented. The Strings taking
   * precedence over the longs, if you're asking for the base16 versions
   */
  @SuppressWarnings("InconsistentOverloads")
  public static SpanContext create(
      @Nullable String traceIdString,
      long traceIdHi,
      long traceIdLo,
      @Nullable String spanIdString,
      long spanId,
      TraceFlags traceFlags,
      TraceState traceState,
      boolean remote) {
    return new AutoValue_SpanContext(
        traceIdString,
        traceIdHi,
        traceIdLo,
        spanIdString,
        spanId,
        traceFlags,
        traceState, /* remote$=*/
        remote);
  }

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * @param traceId the trace identifier of the span context.
   * @param traceIdOffset the offset at which the traceId starts.
   * @param spanId the span identifier of the span context.
   * @param spanIdOffset the offset at which the spanId starts.
   * @param traceFlags the trace options for the span context.
   * @param traceState the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @since 0.1.0
   */
  @SuppressWarnings("InconsistentOverloads")
  public static SpanContext create(
      CharSequence traceId,
      int traceIdOffset,
      CharSequence spanId,
      int spanIdOffset,
      TraceFlags traceFlags,
      TraceState traceState,
      boolean remote) {
    return new AutoValue_SpanContext(
        traceId.subSequence(traceIdOffset, traceIdOffset + 32).toString(),
        0,
        0,
        spanId.subSequence(spanIdOffset, spanIdOffset + 16).toString(),
        0,
        traceFlags,
        traceState,
        /* remote$=*/ remote);
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
      CharSequence traceId, CharSequence spanId, TraceFlags traceFlags, TraceState traceState) {
    return create(traceId, 0, spanId, 0, traceFlags, traceState, /* remote=*/ true);
  }

  /**
   * Returns the trace identifier associated with this {@code SpanContext}.
   *
   * @return the trace identifier associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  @Memoized
  public String getTraceIdAsBase16() {
    String traceIdString = getTraceIdString();
    if (traceIdString != null) {
      return traceIdString;
    }
    return TraceId.fromLongs(getTraceIdHi(), getTraceIdLo()).toString();
  }

  @Nullable
  abstract String getTraceIdString();

  abstract long getTraceIdHi();

  abstract long getTraceIdLo();

  @Nullable
  abstract String getSpanIdString();

  abstract long getSpanIdLong();

  /**
   * Returns the span identifier associated with this {@code SpanContext}.
   *
   * @return the span identifier associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  @Memoized
  public String getSpanIdAsBase16() {
    String spanIdString = getSpanIdString();
    if (spanIdString != null) {
      return spanIdString;
    }
    return SpanId.fromLong(getSpanIdLong()).toString();
  }

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
    return TraceId.isValid(getTraceIdAsBase16()) && SpanId.isValid(getSpanIdAsBase16());
  }

  /**
   * Returns {@code true} if the {@code SpanContext} was propagated from a remote parent.
   *
   * @return {@code true} if the {@code SpanContext} was propagated from a remote parent.
   * @since 0.1.0
   */
  public abstract boolean isRemote();
}
