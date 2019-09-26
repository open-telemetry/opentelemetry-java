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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.sdk.internal.TimestampConverter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import java.util.List;
import java.util.Map;

/** The extend Span interface used by the SDK. */
public interface ReadableSpan {

  /**
   * Returns the {@link SpanContext} of the {@code Span}.
   *
   * <p>Equivalent with {@link Span#getContext()}.
   *
   * @return the {@link SpanContext} of the {@code Span}.
   * @since 0.1.0
   */
  SpanContext getSpanContext();

  /**
   * Returns the name of the {@code Span}.
   *
   * <p>The name can be changed during the lifetime of the Span by using the {@link
   * Span#updateName(String)} so this value cannot be cached.
   *
   * @return the name of the {@code Span}.
   * @since 0.1.0
   */
  String getName();

  /**
   * Returns the value of System.nanoTime() when the span was started.
   *
   * @return Long value representing the System.nanoTime().
   * @since 0.1.0
   */
  long getStartNanoTime();

  /**
   * Returns the end nano time (see {@link System#nanoTime()}). If the span has not ended, should
   * return the current nano time.
   *
   * @return Long value representing the end nano time.
   * @since 0.1.0
   */
  long getEndNanoTime();

  /**
   * Returns the kind of span (enum).
   *
   * @return The Kind of span.
   * @since 0.1.0
   */
  Kind getKind();

  /**
   * Returns the parent span id.
   *
   * @return The parent span id.
   * @since 0.1.0
   */
  SpanId getParentSpanId();

  /**
   * Returns the resource.
   *
   * @return The resource.
   * @since 0.1.0
   */
  Resource getResource();

  /**
   * Returns the status.
   *
   * @return The status.
   * @since 0.1.0
   */
  Status getStatus();

  /**
   * Gets the list of timed events currently held by thsi span.
   *
   * @return A list of TimedEvents.
   * @since 0.1.0
   */
  List<TimedEvent> getTimedEvents();

  /**
   * Returns a copy of the links in this span. The list must be a copy that does not leak out the
   * original (mutable) links.
   *
   * @return List of Links for this span.
   * @since 0.1.0
   */
  List<Link> getLinks();

  /**
   * Returns the attributes for this span. Must be immutable.
   *
   * @return The attributes for this span.
   * @since 0.1.0
   */
  Map<String, AttributeValue> getAttributes();

  /**
   * Returns the TimestampConverter used by this Span instance.
   *
   * @return The TimeStampConverter for this span.
   * @since 0.1.0
   */
  TimestampConverter getTimestampConverter();

  /**
   * Returns the number of child spans for this Span.
   *
   * @return the count of child spans.
   * @since 0.1.0
   */
  int getChildSpanCount();
}
