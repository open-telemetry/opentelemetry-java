/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link SpanData} implementation with a builder that can be used to modify parts of a {@link
 * SpanData}.
 *
 * <pre>{@code
 * String clientType = ClientConfig.parseUserAgent(
 *   data.getAttributes().get(SemanticAttributes.HTTP_USER_AGENT).getStringValue());
 * Attributes newAttributes = Attributes.builder(data.getAttributes())
 *   .setAttribute("client_type", clientType)
 *   .build();
 * data = io.opentelemetry.sdk.extension.incubator.trace.data.SpanData.builder(data)
 *   .setAttributes(newAttributes)
 *   .build();
 * exporter.export(data);
 *
 * }</pre>
 */
// AutoValue generated hashCode is fine but we need to define equals to accept the base SpanData
// type.
@Immutable
@AutoValue
public abstract class SpanDataBuilder implements SpanData {

  /**
   * Returns a {@link SpanDataBuilder.Builder} populated with the information in the provided {@link
   * SpanData}.
   */
  public static SpanDataBuilder.Builder builder(SpanData spanData) {
    return new AutoValue_SpanDataBuilder.Builder()
        .setTraceId(spanData.getTraceId())
        .setSpanId(spanData.getSpanId())
        .setSampled(spanData.isSampled())
        .setTraceState(spanData.getTraceState())
        .setParentSpanContext(spanData.getParentSpanContext())
        .setResource(spanData.getResource())
        .setInstrumentationLibraryInfo(spanData.getInstrumentationLibraryInfo())
        .setName(spanData.getName())
        .setKind(spanData.getKind())
        .setStartEpochNanos(spanData.getStartEpochNanos())
        .setAttributes(spanData.getAttributes())
        .setEvents(spanData.getEvents())
        .setLinks(spanData.getLinks())
        .setStatus(spanData.getStatus())
        .setEndEpochNanos(spanData.getEndEpochNanos())
        .setHasEnded(spanData.hasEnded())
        .setTotalRecordedEvents(spanData.getTotalRecordedEvents())
        .setTotalRecordedLinks(spanData.getTotalRecordedLinks())
        .setTotalAttributeCount(spanData.getTotalAttributeCount());
  }

  public final SpanDataBuilder.Builder toBuilder() {
    return autoToBuilder();
  }

  abstract Builder autoToBuilder();

  abstract boolean getInternalHasEnded();

  @Override
  public final boolean hasEnded() {
    return getInternalHasEnded();
  }

  // AutoValue won't generate equals that compares with SpanData interface but generates hash code
  // fine.
  @SuppressWarnings("EqualsHashCode")
  @Override
  public final boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (o instanceof SpanData) {
      SpanData that = (SpanData) o;
      return getTraceId().equals(that.getTraceId())
          && getSpanId().equals(that.getSpanId())
          && isSampled() == that.isSampled()
          && getTraceState().equals(that.getTraceState())
          && getParentSpanContext().equals(that.getParentSpanContext())
          && getResource().equals(that.getResource())
          && getInstrumentationLibraryInfo().equals(that.getInstrumentationLibraryInfo())
          && getName().equals(that.getName())
          && getKind().equals(that.getKind())
          && getStartEpochNanos() == that.getStartEpochNanos()
          && getAttributes().equals(that.getAttributes())
          && getEvents().equals(that.getEvents())
          && getLinks().equals(that.getLinks())
          && getStatus().equals(that.getStatus())
          && getEndEpochNanos() == that.getEndEpochNanos()
          && hasEnded() == that.hasEnded()
          && getTotalRecordedEvents() == that.getTotalRecordedEvents()
          && getTotalRecordedLinks() == that.getTotalRecordedLinks()
          && getTotalAttributeCount() == that.getTotalAttributeCount();
    }
    return false;
  }

  /** A {@code Builder} class for {@link SpanDataBuilder}. */
  @AutoValue.Builder
  abstract static class Builder {

    public final SpanData build() {
      return autoBuild();
    }

    abstract SpanDataBuilder autoBuild();

    public abstract Builder setTraceId(String traceId);

    public abstract Builder setSpanId(String spanId);

    public abstract Builder setSampled(boolean isSampled);

    public abstract Builder setTraceState(TraceState traceState);

    public abstract Builder setParentSpanContext(SpanContext parentSpanContext);

    public abstract Builder setResource(Resource resource);

    public abstract Builder setInstrumentationLibraryInfo(
        InstrumentationLibraryInfo instrumentationLibraryInfo);

    public abstract Builder setName(String name);

    public abstract Builder setStartEpochNanos(long epochNanos);

    public abstract Builder setEndEpochNanos(long epochNanos);

    public abstract Builder setAttributes(Attributes attributes);

    public abstract Builder setEvents(List<Event> events);

    public abstract Builder setStatus(Status status);

    public abstract Builder setKind(Kind kind);

    public abstract Builder setLinks(List<Link> links);

    abstract Builder setInternalHasEnded(boolean hasEnded);

    public final Builder setHasEnded(boolean hasEnded) {
      return setInternalHasEnded(hasEnded);
    }

    public abstract Builder setTotalRecordedEvents(int totalRecordedEvents);

    public abstract Builder setTotalRecordedLinks(int totalRecordedLinks);

    public abstract Builder setTotalAttributeCount(int totalAttributeCount);
  }
}
