/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
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
        .setSpanContext(SpanContext.getInvalid())
        .setParentSpanContext(SpanContext.getInvalid())
        .setInstrumentationLibraryInfo(InstrumentationLibraryInfo.getEmpty())
        .setLinks(Collections.emptyList())
        .setTotalRecordedLinks(0)
        .setAttributes(Attributes.empty())
        .setEvents(Collections.emptyList())
        .setTotalRecordedEvents(0)
        .setResource(Resource.getEmpty())
        .setTotalAttributeCount(0);
  }

  abstract boolean getInternalHasEnded();

  @Override
  public final boolean hasEnded() {
    return getInternalHasEnded();
  }

  /** A {@code Builder} class for {@link TestSpanData}. */
  @AutoValue.Builder
  public abstract static class Builder {

    abstract TestSpanData autoBuild();

    abstract List<EventData> getEvents();

    abstract List<LinkData> getLinks();

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
     * Set the {@code SpanContext} on this builder.
     *
     * @param spanContext the {@code SpanContext}.
     * @return this builder (for chaining).
     */
    public abstract Builder setSpanContext(SpanContext spanContext);

    /**
     * The parent span context associated for this span, which may be null.
     *
     * @param parentSpanContext the SpanId of the parent
     * @return this.
     */
    public abstract Builder setParentSpanContext(SpanContext parentSpanContext);

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
     * Set the attributes that are associated with this span, in the form of {@link Attributes}.
     *
     * @param attributes {@link Attributes} for this span.
     * @return this
     * @see Attributes
     */
    public abstract Builder setAttributes(Attributes attributes);

    /**
     * Set timed events that are associated with this span. Must not be null, may be empty.
     *
     * @param events A List&lt;Event&gt; of events associated with this span.
     * @return this
     * @see EventData
     */
    public abstract Builder setEvents(List<EventData> events);

    /**
     * Set the status for this span. Must not be null.
     *
     * @param status The Status of this span.
     * @return this
     */
    public abstract Builder setStatus(StatusData status);

    /**
     * Set the kind of span. Must not be null.
     *
     * @param kind The Kind of span.
     * @return this
     */
    public abstract Builder setKind(SpanKind kind);

    /**
     * Set the links associated with this span. Must not be null, may be empty.
     *
     * @param links A List&lt;Link&gt;
     * @return this
     */
    public abstract Builder setLinks(List<LinkData> links);

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
