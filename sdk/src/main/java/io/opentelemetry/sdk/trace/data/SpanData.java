/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ImmutableAttributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of all data collected by the {@link io.opentelemetry.trace.Span} class.
 *
 * @since 0.1.0
 */
@Immutable
public interface SpanData {

  /**
   * Gets the trace id for this span.
   *
   * @return the trace id.
   */
  TraceId getTraceId();

  /**
   * Gets the span id for this span.
   *
   * @return the span id.
   */
  SpanId getSpanId();

  /**
   * Gets the trace flags for this span.
   *
   * @return the trace flags for this span.
   */
  TraceFlags getTraceFlags();

  /**
   * Gets the {@code TraceState} for this span.
   *
   * @return the {@code TraceState} for this span.
   */
  TraceState getTraceState();

  /**
   * Returns the parent {@code SpanId}. If the {@code Span} is a root {@code Span}, the SpanId
   * returned will be invalid..
   *
   * @return the parent {@code SpanId} or an invalid SpanId if this is a root {@code Span}.
   * @since 0.1.0
   */
  SpanId getParentSpanId();

  /**
   * Returns the resource of this {@code Span}.
   *
   * @return the resource of this {@code Span}.
   * @since 0.1.0
   */
  Resource getResource();

  /**
   * Returns the instrumentation library specified when creating the tracer which produced this
   * {@code Span}.
   *
   * @return an instance of {@link InstrumentationLibraryInfo}
   */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  /**
   * Returns the name of this {@code Span}.
   *
   * @return the name of this {@code Span}.
   * @since 0.1.0
   */
  String getName();

  /**
   * Returns the kind of this {@code Span}.
   *
   * @return the kind of this {@code Span}.
   * @since 0.1.0
   */
  Kind getKind();

  /**
   * Returns the start epoch timestamp in nanos of this {@code Span}.
   *
   * @return the start epoch timestamp in nanos of this {@code Span}.
   * @since 0.1.0
   */
  long getStartEpochNanos();

  /**
   * Returns the attributes recorded for this {@code Span}.
   *
   * @return the attributes recorded for this {@code Span}.
   * @since 0.1.0
   */
  Attributes getAttributes();

  /**
   * Returns the timed events recorded for this {@code Span}.
   *
   * @return the timed events recorded for this {@code Span}.
   * @since 0.1.0
   */
  List<Event> getEvents();

  /**
   * Returns links recorded for this {@code Span}.
   *
   * @return links recorded for this {@code Span}.
   * @since 0.1.0
   */
  List<Link> getLinks();

  /**
   * Returns the {@code Status}.
   *
   * @return the {@code Status}.
   * @since 0.1.0
   */
  Status getStatus();

  /**
   * Returns the end epoch timestamp in nanos of this {@code Span}.
   *
   * @return the end epoch timestamp in nanos of this {@code Span}.
   * @since 0.1.0
   */
  long getEndEpochNanos();

  /**
   * Returns {@code true} if the parent is on a different process. {@code false} if this is a root
   * span.
   *
   * @return {@code true} if the parent is on a different process. {@code false} if this is a root
   *     span.
   * @since 0.3.0
   */
  boolean getHasRemoteParent();

  /**
   * Returns whether this Span has already been ended.
   *
   * @return {@code true} if the span has already been ended, {@code false} if not.
   * @since 0.4.0
   */
  boolean getHasEnded();

  /**
   * The total number of {@link Event} events that were recorded on this span. This number may be
   * larger than the number of events that are attached to this span, if the total number recorded
   * was greater than the configured maximum value. See: {@link TraceConfig#getMaxNumberOfEvents()}
   *
   * @return The total number of events recorded on this span.
   */
  int getTotalRecordedEvents();

  /**
   * The total number of {@link Link} links that were recorded on this span. This number may be
   * larger than the number of links that are attached to this span, if the total number recorded
   * was greater than the configured maximum value. See: {@link TraceConfig#getMaxNumberOfLinks()}
   *
   * @return The total number of links recorded on this span.
   */
  int getTotalRecordedLinks();

  /**
   * The total number of attributes that were recorded on this span. This number may be larger than
   * the number of attributes that are attached to this span, if the total number recorded was
   * greater than the configured maximum value. See: {@link TraceConfig#getMaxNumberOfAttributes()}
   *
   * @return The total number of attributes on this span.
   */
  int getTotalAttributeCount();

  /**
   * An immutable implementation of {@link io.opentelemetry.trace.Link}.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  abstract class Link implements io.opentelemetry.trace.Link {

    private static final Attributes DEFAULT_ATTRIBUTE_COLLECTION = ImmutableAttributes.empty();
    private static final int DEFAULT_ATTRIBUTE_COUNT = 0;

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @return a new immutable {@code Event<T>}
     * @since 0.1.0
     */
    public static Link create(SpanContext spanContext) {
      return new AutoValue_SpanData_Link(
          spanContext, DEFAULT_ATTRIBUTE_COLLECTION, DEFAULT_ATTRIBUTE_COUNT);
    }

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @param attributes the attributes of this {@code Link}.
     * @return a new immutable {@code Event<T>}
     * @since 0.1.0
     */
    public static Link create(SpanContext spanContext, Attributes attributes) {
      return new AutoValue_SpanData_Link(spanContext, attributes, attributes.size());
    }

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @param attributes the attributes of this {@code Link}.
     * @param totalAttributeCount the total number of attributed for this {@code Link}.
     * @return a new immutable {@code Event<T>}
     * @since 0.1.0
     */
    public static Link create(
        SpanContext spanContext, Attributes attributes, int totalAttributeCount) {
      return new AutoValue_SpanData_Link(spanContext, attributes, totalAttributeCount);
    }

    /**
     * The total number of attributes that were recorded on this Link. This number may be larger
     * than the number of attributes that are attached to this span, if the total number recorded
     * was greater than the configured maximum value. See: {@link
     * TraceConfig#getMaxNumberOfAttributesPerLink()}
     *
     * @return The number of attributes on this link.
     */
    public abstract int getTotalAttributeCount();

    Link() {}
  }

  interface Event extends io.opentelemetry.trace.Event {
    /**
     * Returns the epoch time in nanos of this event.
     *
     * @return the epoch time in nanos of this event.
     * @since 0.1.0
     */
    long getEpochNanos();

    /**
     * The total number of attributes that were recorded on this Event. This number may be larger
     * than the number of attributes that are attached to this span, if the total number recorded
     * was greater than the configured maximum value. See: {@link
     * TraceConfig#getMaxNumberOfAttributesPerEvent()}
     *
     * @return The total number of attributes on this event.
     */
    int getTotalAttributeCount();
  }
}
