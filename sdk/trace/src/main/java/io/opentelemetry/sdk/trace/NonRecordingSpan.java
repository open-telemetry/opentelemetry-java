/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.internal.SpanInstrumentation;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Span implementation used from {@link io.opentelemetry.sdk.trace.SdkTracer} when starting a span
 * which is not recording. This span implementation behaves exactly like one returned from {@link
 * Span#wrap(SpanContext)} with the addition that {@link #end()} collects health metrics.
 */
@Immutable
final class NonRecordingSpan implements Span {

  private final SpanContext spanContext;
  private final SpanInstrumentation.Recording metricRecording;

  NonRecordingSpan(SpanContext spanContext, SpanInstrumentation.Recording metricRecording) {
    this.spanContext = spanContext;
    this.metricRecording = metricRecording;
  }

  @Override
  public Span setAttribute(String key, @Nullable String value) {
    return this;
  }

  @Override
  public Span setAttribute(String key, long value) {
    return this;
  }

  @Override
  public Span setAttribute(String key, double value) {
    return this;
  }

  @Override
  public Span setAttribute(String key, boolean value) {
    return this;
  }

  @Override
  public <T> Span setAttribute(AttributeKey<T> key, @Nullable T value) {
    return this;
  }

  @Override
  public Span setAllAttributes(Attributes attributes) {
    return this;
  }

  @Override
  public Span addEvent(String name) {
    return this;
  }

  @Override
  public Span addEvent(String name, long timestamp, TimeUnit unit) {
    return this;
  }

  @Override
  public Span addEvent(String name, Attributes attributes) {
    return this;
  }

  @Override
  public Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
    return this;
  }

  @Override
  public Span setStatus(StatusCode statusCode) {
    return this;
  }

  @Override
  public Span setStatus(StatusCode statusCode, String description) {
    return this;
  }

  @Override
  public Span recordException(Throwable exception) {
    return this;
  }

  @Override
  public Span recordException(Throwable exception, Attributes additionalAttributes) {
    return this;
  }

  @Override
  public Span updateName(String name) {
    return this;
  }

  @Override
  public void end() {
    metricRecording.recordSpanEnd();
  }

  @Override
  public void end(long timestamp, TimeUnit unit) {
    end();
  }

  @Override
  public SpanContext getSpanContext() {
    return spanContext;
  }

  @Override
  public boolean isRecording() {
    return false;
  }

  @Override
  public String toString() {
    return "NonRecordingSpan{" + spanContext + '}';
  }
}
