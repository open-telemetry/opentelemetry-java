/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of all data collected by the {@link io.opentelemetry.api.trace.Span}
 * class.
 */
@Immutable
@AutoValue
public abstract class TestSpanData implements SpanData {

  /**
   * Creates a new Builder for creating an SpanData instance.
   *
   * @return a new Builder.
   */
  public static Builder builder() {
    return new AutoValue_TestSpanData.Builder()
        .setParentSpanId(SpanId.getInvalid())
        .setInstrumentationLibraryInfo(InstrumentationLibraryInfo.getEmpty())
        .setLinks(Collections.emptyList())
        .setTotalRecordedLinks(0)
        .setAttributes(Attributes.empty())
        .setEvents(Collections.emptyList())
        .setTotalRecordedEvents(0)
        .setResource(Resource.getEmpty())
        .setTraceState(TraceState.getDefault())
        .setSampled(false)
        .setHasRemoteParent(false)
        .setTotalAttributeCount(0);
  }

  abstract boolean getInternalHasEnded();

  abstract boolean getInternalHasRemoteParent();

  @Override
  public final boolean hasEnded() {
    return getInternalHasEnded();
  }

  @Override
  public final boolean hasRemoteParent() {
    return getInternalHasRemoteParent();
  }

  /** A {@code Builder} class for {@link TestSpanData}. */
  @AutoValue.Builder
  public abstract static class Builder {

    abstract TestSpanData autoBuild();

    abstract List<Event> getEvents();

    abstract List<SpanData.Link> getLinks();

    /**
     * Create a new SpanData instance from the data in this.
     *
     * @return a new SpanData instance
     */
    public TestSpanData build() {
      // make unmodifiable copies of any collections
      setEvents(Collections.unmodifiableList(new ArrayList<>(getEvents())));
      setLinks(Collections.unmodifiableList(new ArrayList<>(getLinks())));
      return autoBuild();
    }

    /**
     * Set the trace id on this builder.
     *
     * @param traceId the trace id.
     * @return this builder (for chaining).
     */
    public abstract Builder setTraceId(String traceId);

    /**
     * Set the span id on this builder.
     *
     * @param spanId the span id.
     * @return this builder (for chaining).
     */
    public abstract Builder setSpanId(String spanId);

    public abstract Builder setSampled(boolean isSampled);

    /**
     * Set the {@link TraceState} on this builder.
     *
     * @param traceState the {@code TraceState}.
     * @return this.
     */
    public abstract Builder setTraceState(TraceState traceState);

    /**
     * The parent span id associated for this span, which may be null.
     *
     * @param parentSpanId the SpanId of the parent
     * @return this.
     */
    public abstract Builder setParentSpanId(String parentSpanId);

    /**
     * Set the {@link Resource} associated with this span. Must not be null.
     *
     * @param resource the Resource that generated this span.
     * @return this
     */
    public abstract Builder setResource(Resource resource);

    /**
     * Sets the instrumentation library of the tracer which created this span. Must not be null.
     *
     * @param instrumentationLibraryInfo the instrumentation library of the tracer which created
     *     this span.
     * @return this
     */
    public abstract Builder setInstrumentationLibraryInfo(
        InstrumentationLibraryInfo instrumentationLibraryInfo);

    /**
     * Set the name of the span. Must not be null.
     *
     * @param name the name.
     * @return this
     */
    public abstract Builder setName(String name);

    /**
     * Set the start timestamp of the span.
     *
     * @param epochNanos the start epoch timestamp in nanos.
     * @return this
     */
    public abstract Builder setStartEpochNanos(long epochNanos);

    /**
     * Set the end timestamp of the span.
     *
     * @param epochNanos the end epoch timestamp in nanos.
     * @return this
     */
    public abstract Builder setEndEpochNanos(long epochNanos);

    /**
     * Set the attributes that are associated with this span, in the form of {@link
     * ReadableAttributes}.
     *
     * @param attributes {@link ReadableAttributes} for this span.
     * @return this
     * @see ReadableAttributes
     */
    public abstract Builder setAttributes(ReadableAttributes attributes);

    /**
     * Set timed events that are associated with this span. Must not be null, may be empty.
     *
     * @param events A List&lt;Event&gt; of events associated with this span.
     * @return this
     * @see Event
     */
    public abstract Builder setEvents(List<Event> events);

    /**
     * Set the status for this span. Must not be null.
     *
     * @param status The Status of this span.
     * @return this
     */
    public abstract Builder setStatus(Status status);

    /**
     * Set the kind of span. Must not be null.
     *
     * @param kind The Kind of span.
     * @return this
     */
    public abstract Builder setKind(Kind kind);

    /**
     * Set the links associated with this span. Must not be null, may be empty.
     *
     * @param links A List&lt;Link&gt;
     * @return this
     */
    public abstract Builder setLinks(List<SpanData.Link> links);

    abstract Builder setInternalHasRemoteParent(boolean hasRemoteParent);

    /**
     * Sets to true if the span has a parent on a different process.
     *
     * @param hasRemoteParent A boolean indicating if the span has a remote parent.
     * @return this
     */
    public final Builder setHasRemoteParent(boolean hasRemoteParent) {
      return setInternalHasRemoteParent(hasRemoteParent);
    }

    abstract Builder setInternalHasEnded(boolean hasEnded);

    /**
     * Sets to true if the span has been ended.
     *
     * @param hasEnded A boolean indicating if the span has been ended.
     * @return this
     */
    public final Builder setHasEnded(boolean hasEnded) {
      return setInternalHasEnded(hasEnded);
    }

    /**
     * Set the total number of events recorded on this span.
     *
     * @param totalRecordedEvents The total number of events recorded.
     * @return this
     */
    public abstract Builder setTotalRecordedEvents(int totalRecordedEvents);

    /**
     * Set the total number of links recorded on this span.
     *
     * @param totalRecordedLinks The total number of links recorded.
     * @return this
     */
    public abstract Builder setTotalRecordedLinks(int totalRecordedLinks);

    /**
     * Set the total number of attributes recorded on this span.
     *
     * @param totalAttributeCount The total number of attributes recorded.
     * @return this
     */
    public abstract Builder setTotalAttributeCount(int totalAttributeCount);
  }
}
