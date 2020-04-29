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

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.internal.Utils;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.annotation.concurrent.Immutable;

/**
 * The {@code DefaultSpan} is the default {@link Span} that is used when no {@code Span}
 * implementation is available. All operations are no-op except context propagation.
 *
 * <p>Used also to stop tracing, see {@link Tracer#withSpan}.
 *
 * @since 0.1.0
 */
@Immutable
public final class DefaultSpan implements Span {

  private static final Random random = new Random();
  private static final DefaultSpan INVALID = new DefaultSpan(SpanContext.getInvalid());

  /**
   * Returns a {@link DefaultSpan} with an invalid {@link SpanContext}.
   *
   * @return a {@code DefaultSpan} with an invalid {@code SpanContext}.
   * @since 0.1.0
   */
  public static DefaultSpan getInvalid() {
    return INVALID;
  }

  /**
   * Creates an instance of this class with the {@link SpanContext}.
   *
   * @param spanContext the {@code SpanContext}.
   * @return a {@link DefaultSpan}.
   * @since 0.1.0
   */
  public static DefaultSpan create(SpanContext spanContext) {
    return new DefaultSpan(spanContext);
  }

  static DefaultSpan createRandom() {
    return new DefaultSpan(
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceFlags.getDefault(),
            TraceState.getDefault()));
  }

  private final SpanContext spanContext;

  DefaultSpan(SpanContext spanContext) {
    this.spanContext = spanContext;
  }

  @Override
  public void setAttribute(String key, String value) {
    Objects.requireNonNull(key, "key");
  }

  @Override
  public void setAttribute(String key, long value) {
    Objects.requireNonNull(key, "key");
  }

  @Override
  public void setAttribute(String key, double value) {
    Objects.requireNonNull(key, "key");
  }

  @Override
  public void setAttribute(String key, boolean value) {
    Objects.requireNonNull(key, "key");
  }

  @Override
  public void setAttribute(String key, AttributeValue value) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(value, "value");
  }

  @Override
  public void addEvent(String name) {}

  @Override
  public void addEvent(String name, long timestamp) {
    Objects.requireNonNull(name, "name");
    Utils.checkArgument(timestamp >= 0, "Negative timestamp");
  }

  @Override
  public void addEvent(String name, Map<String, AttributeValue> attributes) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(attributes, "attributes");
  }

  @Override
  public void addEvent(String name, Map<String, AttributeValue> attributes, long timestamp) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(attributes, "attributes");
    Utils.checkArgument(timestamp >= 0, "Negative timestamp");
  }

  @Override
  public void addEvent(Event event) {
    Objects.requireNonNull(event, "event");
  }

  @Override
  public void addEvent(Event event, long timestamp) {
    Objects.requireNonNull(event, "event");
    Utils.checkArgument(timestamp >= 0, "Negative timestamp");
  }

  @Override
  public void setStatus(Status status) {
    Objects.requireNonNull(status, "status");
  }

  @Override
  public void updateName(String name) {
    Objects.requireNonNull(name, "name");
  }

  @Override
  public void end() {}

  @Override
  public void end(EndSpanOptions endOptions) {
    Objects.requireNonNull(endOptions, "endOptions");
  }

  @Override
  public SpanContext getContext() {
    return spanContext;
  }

  @Override
  public boolean isRecording() {
    return false;
  }

  @Override
  public String toString() {
    return "DefaultSpan";
  }
}
