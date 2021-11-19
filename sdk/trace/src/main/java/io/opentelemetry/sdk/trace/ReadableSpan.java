/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.trace.data.SpanData;
import javax.annotation.Nullable;

/** SDK representation of a {@code Span} that can be read. */
public interface ReadableSpan {

  /**
   * Returns the {@link SpanContext} of the {@code Span}.
   *
   * <p>Equivalent with {@link Span#getSpanContext()}.
   *
   * @return the {@link SpanContext} of the {@code Span}.
   */
  SpanContext getSpanContext();

  /**
   * Returns the parent {@link SpanContext} of the {@code Span}, or {@link SpanContext#getInvalid()}
   * if this is a root span.
   *
   * @return the parent {@link SpanContext} of the {@code Span}
   */
  SpanContext getParentSpanContext();

  /**
   * Returns the name of the {@code Span}.
   *
   * <p>The name can be changed during the lifetime of the Span by using the {@link
   * Span#updateName(String)} so this value cannot be cached.
   *
   * <p>Note: the implementation of this method performs locking to ensure thread-safe behavior.
   *
   * @return the name of the {@code Span}.
   */
  String getName();

  /**
   * This converts this instance into an immutable SpanData instance, for use in export.
   *
   * @return an immutable {@link SpanData} instance.
   */
  SpanData toSpanData();

  /**
   * Returns the instrumentation library specified when creating the tracer which produced this
   * span.
   *
   * @return an instance of {@link InstrumentationLibraryInfo} describing the instrumentation
   *     library
   */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  /**
   * Returns whether this Span has already been ended.
   *
   * <p>Note: the implementation of this method performs locking to ensure thread-safe behavior.
   *
   * @return {@code true} if the span has already been ended, {@code false} if not.
   */
  boolean hasEnded();

  /**
   * Returns the latency of the {@code Span} in nanos. If still active then returns now() - start
   * time.
   *
   * <p>Note: the implementation of this method performs locking to ensure thread-safe behavior.
   *
   * @return the latency of the {@code Span} in nanos.
   */
  long getLatencyNanos();

  /**
   * Returns the kind of the span.
   *
   * @return the kind of the span.
   */
  SpanKind getKind();

  /**
   * Returns the value for the given {@link AttributeKey}, or {@code null} if not found.
   *
   * <p>The attribute values can be changed during the lifetime of the Span by using {@link
   * Span#setAttribute}} so this value cannot be cached.
   *
   * <p>Note: the implementation of this method performs locking to ensure thread-safe behavior.
   *
   * @return the value for the given {@link AttributeKey}, or {@code null} if not found.
   */
  @Nullable
  <T> T getAttribute(AttributeKey<T> key);
}
