/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

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
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable class that stores {@link SpanData} based on a {@link RecordEventsReadableSpan}.
 *
 * <p>This class stores a reference to a mutable {@link RecordEventsReadableSpan} ({@code delegate})
 * which it uses only the immutable parts from, and a copy of all the mutable parts.
 *
 * <p>When adding a new field to {@link RecordEventsReadableSpan}, store a copy if and only if the
 * field is mutable in the {@link RecordEventsReadableSpan}. Otherwise retrieve it from the
 * referenced {@link RecordEventsReadableSpan}.
 */
@Immutable
@AutoValue
abstract class SpanWrapper implements SpanData {
  abstract RecordEventsReadableSpan delegate();

  abstract List<LinkData> resolvedLinks();

  abstract List<EventData> resolvedEvents();

  abstract Attributes attributes();

  abstract int totalAttributeCount();

  abstract int totalRecordedEvents();

  abstract StatusData status();

  abstract String name();

  abstract long endEpochNanos();

  abstract boolean internalHasEnded();

  /**
   * Note: the collections that are passed into this creator method are assumed to be immutable to
   * preserve the overall immutability of the class.
   */
  static SpanWrapper create(
      RecordEventsReadableSpan delegate,
      List<LinkData> links,
      List<EventData> events,
      Attributes attributes,
      int totalAttributeCount,
      int totalRecordedEvents,
      StatusData status,
      String name,
      long endEpochNanos,
      boolean hasEnded) {
    return new AutoValue_SpanWrapper(
        delegate,
        links,
        events,
        attributes,
        totalAttributeCount,
        totalRecordedEvents,
        status,
        name,
        endEpochNanos,
        hasEnded);
  }

  @Override
  public SpanContext getSpanContext() {
    return delegate().getSpanContext();
  }

  @Override
  public SpanContext getParentSpanContext() {
    return delegate().getParentSpanContext();
  }

  @Override
  public Resource getResource() {
    return delegate().getResource();
  }

  @Override
  public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return delegate().getInstrumentationLibraryInfo();
  }

  @Override
  public String getName() {
    return name();
  }

  @Override
  public SpanKind getKind() {
    return delegate().getKind();
  }

  @Override
  public long getStartEpochNanos() {
    return delegate().getStartEpochNanos();
  }

  @Override
  public Attributes getAttributes() {
    return attributes();
  }

  @Override
  public List<EventData> getEvents() {
    return resolvedEvents();
  }

  @Override
  public List<LinkData> getLinks() {
    return resolvedLinks();
  }

  @Override
  public StatusData getStatus() {
    return status();
  }

  @Override
  public long getEndEpochNanos() {
    return endEpochNanos();
  }

  @Override
  public boolean hasEnded() {
    return internalHasEnded();
  }

  @Override
  public int getTotalRecordedEvents() {
    return totalRecordedEvents();
  }

  @Override
  public int getTotalRecordedLinks() {
    return delegate().getTotalRecordedLinks();
  }

  @Override
  public int getTotalAttributeCount() {
    return totalAttributeCount();
  }
}
