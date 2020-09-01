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

import io.grpc.Context;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
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

  private static final DefaultSpan INVALID =
      new DefaultSpan(SpanContext.getInvalid(), Context.ROOT);

  // TODO: Do we need to change this to getInvalid(Context parent)?
  /**
   * Returns a {@link DefaultSpan} with an invalid {@link SpanContext}.
   *
   * @return a {@code DefaultSpan} with an invalid {@code SpanContext}.
   * @since 0.1.0
   */
  public static Span getInvalid() {
    return INVALID;
  }

  /**
   * Creates an instance of this class with a (parent) {@link Context}.
   *
   * @param spanContext the {@link SpanContext}.
   * @param parent the (parent) {@link Context}.
   * @return a {@link DefaultSpan}.
   * @since 0.1.0
   */
  public static Span create(SpanContext spanContext, Context parent) {
    return new DefaultSpan(spanContext, parent);
  }

  public static Context createInContext(SpanContext spanContext, Context parent) {
    return TracingContextUtils.withSpan(create(spanContext, parent), parent);
  }

  private final SpanContext spanContext;
  private final Context parent;

  DefaultSpan(SpanContext spanContext, Context parent) {
    this.spanContext = spanContext;
    this.parent = parent;
  }

  @Override
  public void setAttribute(String key, String value) {}

  @Override
  public void setAttribute(String key, long value) {}

  @Override
  public void setAttribute(String key, double value) {}

  @Override
  public void setAttribute(String key, boolean value) {}

  @Override
  public void setAttribute(String key, AttributeValue value) {}

  @Override
  public void addEvent(String name) {}

  @Override
  public void addEvent(String name, long timestamp) {}

  @Override
  public void addEvent(String name, Attributes attributes) {}

  @Override
  public void addEvent(String name, Attributes attributes, long timestamp) {}

  @Override
  public void addEvent(Event event) {}

  @Override
  public void addEvent(Event event, long timestamp) {}

  @Override
  public void setStatus(Status status) {}

  @Override
  public void recordException(Throwable exception) {}

  @Override
  public void recordException(Throwable exception, Attributes additionalAttributes) {}

  @Override
  public void updateName(String name) {}

  @Override
  public void end() {}

  @Override
  public void end(EndSpanOptions endOptions) {}

  @Override
  public SpanContext getContext() {
    return spanContext;
  }

  @Override
  public Context getParent() {
    return parent;
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
