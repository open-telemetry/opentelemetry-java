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
import java.util.Arrays;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span context. A span context contains the state that must propagate to
 * child {@link Span}s and across process boundaries. It contains the identifiers (a {@link TraceId
 * trace_id} and {@link SpanId span_id}) associated with the {@link Span} and a set of {@link
 * TraceFlags options}, as well as the {@link Tracestate tracestate} and the {@link boolean remote}
 * flag.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class SpanContext {

  private static final SpanContext INVALID =
      newBuilder()
          .setTraceId(TraceId.getInvalid())
          .setSpanId(SpanId.getInvalid())
          .setTraceFlags(TraceFlags.getDefault())
          .setTracestate(Tracestate.getDefault())
          .setRemote(false)
          .build();

  private static Builder newBuilder() {
    return new AutoValue_SpanContext.Builder();
  }

  /**
   * Returns the invalid {@code SpanContext} that can be used for no-op operations.
   *
   * @return the invalid {@code SpanContext}.
   */
  static SpanContext getInvalid() {
    return INVALID;
  }

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * @param traceId the trace identifier of the span context.
   * @param spanId the span identifier of the span context.
   * @param traceFlags the trace options for the span context.
   * @param tracestate the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @since 0.1.0
   */
  public static SpanContext create(
      TraceId traceId, SpanId spanId, TraceFlags traceFlags, Tracestate tracestate) {
    return SpanContext.newBuilder()
        .setTraceId(traceId)
        .setSpanId(spanId)
        .setTraceFlags(traceFlags)
        .setTracestate(tracestate)
        .setRemote(false)
        .build();
  }

  /**
   * Creates a new {@code SpanContext} that was propagated from a remote parent, with the given
   * identifiers and options.
   *
   * @param traceId the trace identifier of the span context.
   * @param spanId the span identifier of the span context.
   * @param traceFlags the trace options for the span context.
   * @param tracestate the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @since 0.1.0
   */
  public static SpanContext createFromRemoteParent(
      TraceId traceId, SpanId spanId, TraceFlags traceFlags, Tracestate tracestate) {
    return SpanContext.newBuilder()
        .setTraceId(traceId)
        .setSpanId(spanId)
        .setTraceFlags(traceFlags)
        .setTracestate(tracestate)
        .setRemote(true)
        .build();
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
   * Returns the {@code Tracestate} associated with this {@code SpanContext}.
   *
   * @return the {@code Tracestate} associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public abstract Tracestate getTracestate();

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

  /**
   * Note: This has a custom equals/hashcode implementation. The equals() method does not include
   * the {@code tracestate}.
   *
   * <p>TODO: is this intentional? If it is, it should be documented as to *why* this is the case.
   */
  @Override
  public final boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof SpanContext)) {
      return false;
    }

    SpanContext that = (SpanContext) obj;
    return getTraceId().equals(that.getTraceId())
        && getSpanId().equals(that.getSpanId())
        && getTraceFlags().equals(that.getTraceFlags())
        && isRemote() == that.isRemote();
  }

  /**
   * Note: This has a custom equals/hashcode implementation. The hashcode() method does not include
   * the {@code tracestate} or the {@code remote} flag.
   *
   * <p>TODO: is this intentional? If it is, it should be documented as to *why* this is the case.
   */
  @Override
  public final int hashCode() {
    return Arrays.hashCode(new Object[] {getTraceId(), getSpanId(), getTraceFlags()});
  }

  /** A {@code Builder} for a {@link SpanContext}. */
  @AutoValue.Builder
  abstract static class Builder {
    public abstract SpanContext build();

    public abstract Builder setTraceId(TraceId traceId);

    public abstract Builder setSpanId(SpanId traceId);

    public abstract Builder setTraceFlags(TraceFlags traceFlags);

    public abstract Builder setTracestate(Tracestate tracestate);

    public abstract Builder setRemote(boolean remote);
  }
}
