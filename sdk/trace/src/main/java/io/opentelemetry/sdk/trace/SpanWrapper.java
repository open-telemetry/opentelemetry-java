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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.internal.InstrumentationScopeUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable class that stores {@link SpanData} based on a {@link SdkSpan}.
 *
 * <p>This class stores a reference to a mutable {@link SdkSpan} ({@code delegate}) which it uses
 * only the immutable parts from, and a copy of all the mutable parts.
 *
 * <p>When adding a new field to {@link SdkSpan}, store a copy if and only if the field is mutable
 * in the {@link SdkSpan}. Otherwise retrieve it from the referenced {@link SdkSpan}.
 */
@Immutable
@AutoValue
abstract class SpanWrapper implements SpanData {
  abstract SdkSpan delegate();

  abstract List<LinkData> resolvedLinks();

  abstract List<EventData> resolvedEvents();

  abstract Attributes attributes();

  abstract int totalAttributeCount();

  abstract int totalRecordedEvents();

  abstract int totalRecordedLinks();

  abstract StatusData status();

  abstract String name();

  abstract long endEpochNanos();

  abstract boolean internalHasEnded();

  /**
   * Note: the collections that are passed into this creator method are assumed to be immutable to
   * preserve the overall immutability of the class.
   */
  static SpanWrapper create(
      SdkSpan delegate,
      List<LinkData> links,
      List<EventData> events,
      Attributes attributes,
      int totalAttributeCount,
      int totalRecordedEvents,
      int totalRecordedLinks,
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
        totalRecordedLinks,
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
  @Deprecated
  public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return InstrumentationScopeUtil.toInstrumentationLibraryInfo(
        delegate().getInstrumentationScopeInfo());
  }

  @Override
  public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return delegate().getInstrumentationScopeInfo();
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
    return totalRecordedLinks();
  }

  @Override
  public int getTotalAttributeCount() {
    return totalAttributeCount();
  }

  @Override
  public final String toString() {
    return "SpanData{"
        + "spanContext="
        + getSpanContext()
        + ", "
        + "parentSpanContext="
        + getParentSpanContext()
        + ", "
        + "resource="
        + getResource()
        + ", "
        + "instrumentationScopeInfo="
        + getInstrumentationScopeInfo()
        + ", "
        + "name="
        + getName()
        + ", "
        + "kind="
        + getKind()
        + ", "
        + "startEpochNanos="
        + getStartEpochNanos()
        + ", "
        + "endEpochNanos="
        + getEndEpochNanos()
        + ", "
        + "attributes="
        + getAttributes()
        + ", "
        + "totalAttributeCount="
        + getTotalAttributeCount()
        + ", "
        + "events="
        + getEvents()
        + ", "
        + "totalRecordedEvents="
        + getTotalRecordedEvents()
        + ", "
        + "links="
        + getLinks()
        + ", "
        + "totalRecordedLinks="
        + getTotalRecordedLinks()
        + ", "
        + "status="
        + getStatus()
        + ", "
        + "hasEnded="
        + hasEnded()
        + "}";
  }
}
