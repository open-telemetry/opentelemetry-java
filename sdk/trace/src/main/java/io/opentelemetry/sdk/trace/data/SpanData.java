/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of all data collected by the {@link io.opentelemetry.api.trace.Span}
 * class.
 */
@Immutable
public interface SpanData {

  /**
   * Gets the trace id for this span.
   *
   * @return the trace id.
   */
  String getTraceId();

  /**
   * Gets the span id for this span.
   *
   * @return the span id.
   */
  String getSpanId();

  /** Whether the 'sampled' option set on this span. */
  boolean isSampled();

  /**
   * Gets the {@code TraceState} for this span.
   *
   * @return the {@code TraceState} for this span.
   */
  TraceState getTraceState();

  /**
   * Returns the parent {@link SpanContext}. If the span is a root span, the {@link SpanContext}
   * returned will be invalid.
   */
  SpanContext getParentSpanContext();

  /**
   * Returns the parent {@code SpanId}. If the {@code Span} is a root {@code Span}, the SpanId
   * returned will be invalid.
   *
   * @return the parent {@code SpanId} or an invalid SpanId if this is a root {@code Span}.
   */
  default String getParentSpanId() {
    return getParentSpanContext().getSpanIdAsHexString();
  }

  /**
   * Returns {@code true} if the parent is on a different process. {@code false} if this is a root
   * span.
   *
   * @return {@code true} if the parent is on a different process. {@code false} if this is a root
   *     span.
   * @deprecated Use {@link #getParentSpanContext()}
   */
  @Deprecated
  default boolean hasRemoteParent() {
    return getParentSpanContext().isRemote();
  }

  /**
   * Returns the resource of this {@code Span}.
   *
   * @return the resource of this {@code Span}.
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
   */
  String getName();

  /**
   * Returns the kind of this {@code Span}.
   *
   * @return the kind of this {@code Span}.
   */
  Kind getKind();

  /**
   * Returns the start epoch timestamp in nanos of this {@code Span}.
   *
   * @return the start epoch timestamp in nanos of this {@code Span}.
   */
  long getStartEpochNanos();

  /**
   * Returns the attributes recorded for this {@code Span}.
   *
   * @return the attributes recorded for this {@code Span}.
   */
  Attributes getAttributes();

  /**
   * Returns the timed events recorded for this {@code Span}.
   *
   * @return the timed events recorded for this {@code Span}.
   */
  List<Event> getEvents();

  /**
   * Returns links recorded for this {@code Span}.
   *
   * @return links recorded for this {@code Span}.
   */
  List<Link> getLinks();

  /**
   * Returns the {@code Status}.
   *
   * @return the {@code Status}.
   */
  Status getStatus();

  /**
   * Returns the end epoch timestamp in nanos of this {@code Span}.
   *
   * @return the end epoch timestamp in nanos of this {@code Span}.
   */
  long getEndEpochNanos();

  /**
   * Returns whether this Span has already been ended.
   *
   * @return {@code true} if the span has already been ended, {@code false} if not.
   */
  boolean hasEnded();

  /**
   * The total number of {@link Event} events that were recorded on this span. This number may be
   * larger than the number of events that are attached to this span, if the total number recorded
   * was greater than the configured maximum value. See: {@link TraceConfig#getMaxNumberOfEvents()}
   *
   * @return The total number of events recorded on this span.
   */
  int getTotalRecordedEvents();

  /**
   * The total number of {@link ImmutableLink} links that were recorded on this span. This number
   * may be larger than the number of links that are attached to this span, if the total number
   * recorded was greater than the configured maximum value. See: {@link
   * TraceConfig#getMaxNumberOfLinks()}
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
   * A link to a {@link Span}.
   *
   * <p>Used (for example) in batching operations, where a single batch handler processes multiple
   * requests from different traces. Link can be also used to reference spans from the same trace.
   */
  interface Link {

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @return a new immutable {@code Event<T>}
     */
    static Link create(SpanContext spanContext) {
      return ImmutableLink.create(spanContext);
    }

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @param attributes the attributes of this {@code Link}.
     * @return a new immutable {@code Event<T>}
     */
    static Link create(SpanContext spanContext, Attributes attributes) {
      return ImmutableLink.create(spanContext, attributes);
    }

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @param attributes the attributes of this {@code Link}.
     * @param totalAttributeCount the total number of attributed for this {@code Link}.
     * @return a new immutable {@code Event<T>}
     */
    static Link create(SpanContext spanContext, Attributes attributes, int totalAttributeCount) {
      return ImmutableLink.create(spanContext, attributes, totalAttributeCount);
    }

    /**
     * Returns the {@code SpanContext}.
     *
     * @return the {@code SpanContext}.
     */
    SpanContext getContext();

    /**
     * Returns the set of attributes.
     *
     * @return the set of attributes.
     */
    Attributes getAttributes();

    /**
     * The total number of attributes that were recorded on this Link. This number may be larger
     * than the number of attributes that are attached to this span, if the total number recorded
     * was greater than the configured maximum value. See: {@link
     * TraceConfig#getMaxNumberOfAttributesPerLink()}
     *
     * @return The number of attributes on this link.
     */
    int getTotalAttributeCount();
  }

  interface Event {

    /**
     * Returns a new immutable {@code Event}.
     *
     * @param epochNanos epoch timestamp in nanos of the {@code Event}.
     * @param name the name of the {@code Event}.
     * @param attributes the attributes of the {@code Event}.
     * @return a new immutable {@code Event<T>}
     */
    static Event create(long epochNanos, String name, Attributes attributes) {
      return ImmutableEvent.create(epochNanos, name, attributes);
    }

    /**
     * Returns a new immutable {@code Event}.
     *
     * @param epochNanos epoch timestamp in nanos of the {@code Event}.
     * @param name the name of the {@code Event}.
     * @param attributes the attributes of the {@code Event}.
     * @param totalAttributeCount the total number of attributes for this {@code} Event.
     * @return a new immutable {@code Event<T>}
     */
    static Event create(
        long epochNanos, String name, Attributes attributes, int totalAttributeCount) {
      return ImmutableEvent.create(epochNanos, name, attributes, totalAttributeCount);
    }

    /**
     * Return the name of the {@code Event}.
     *
     * @return the name of the {@code Event}.
     */
    String getName();

    /**
     * Return the attributes of the {@code Event}.
     *
     * @return the attributes of the {@code Event}.
     */
    Attributes getAttributes();

    /**
     * Returns the epoch time in nanos of this event.
     *
     * @return the epoch time in nanos of this event.
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

    /**
     * Returns the dropped attributes count of this event.
     *
     * @return the dropped attributes count of this event.
     */
    default int getDroppedAttributesCount() {
      return getTotalAttributeCount() - getAttributes().size();
    }
  }

  /**
   * Defines the status of a {@link Span} by providing a standard {@link StatusCode} in conjunction
   * with an optional descriptive message.
   */
  interface Status {

    /**
     * Returns a {@link Status} indicating the operation has been validated by an application
     * developer or operator to have completed successfully.
     */
    static Status ok() {
      return ImmutableStatus.OK;
    }

    /** Returns the default {@link Status}. */
    static Status unset() {
      return ImmutableStatus.UNSET;
    }

    /** Returns a {@link Status} indicating an error occurred. */
    static Status error() {
      return ImmutableStatus.ERROR;
    }

    /**
     * Returns a {@link Status} with the given {@code code} and {@code description}. If {@code
     * description} is {@code null}, the returned {@link Status} does not have a description.
     */
    static Status create(StatusCode code, @Nullable String description) {
      return ImmutableStatus.create(code, description);
    }

    /** Returns the status code. */
    StatusCode getStatusCode();

    /**
     * Returns the description of this {@code Status} for human consumption.
     *
     * @return the description of this {@code Status}.
     */
    @Nullable
    String getDescription();

    /**
     * Returns {@code true} if this {@code Status} is UNSET, i.e., not an error.
     *
     * @return {@code true} if this {@code Status} is UNSET.
     */
    // TODO: Consider to remove this in a future PR. Avoid too many changes in the initial PR.
    default boolean isUnset() {
      return StatusCode.UNSET == getStatusCode();
    }

    /**
     * Returns {@code true} if this {@code Status} is ok, i.e., status is not set, or has been
     * overridden to be ok by an operator.
     *
     * @return {@code true} if this {@code Status} is OK or UNSET.
     */
    // TODO: Consider to remove this in a future PR. Avoid too many changes in the initial PR.
    default boolean isOk() {
      return isUnset() || StatusCode.OK == getStatusCode();
    }
  }
}
