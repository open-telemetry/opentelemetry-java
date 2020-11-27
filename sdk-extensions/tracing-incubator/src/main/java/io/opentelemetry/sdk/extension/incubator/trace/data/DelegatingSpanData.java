/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.data;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;

/**
 * A {@link SpanData} which delegates all methods to another {@link SpanData}. Extend this class to
 * modify the {@link SpanData} that will be exported, for example by creating a delegating {@link
 * io.opentelemetry.sdk.trace.export.SpanExporter} which wraps {@link SpanData} with a custom
 * implementation.
 *
 * <pre>{@code
 * SpanDataWithClientType extends DelegatingSpanData {
 *
 *   private final ReadableAttributes attributes;
 *
 *   SpanDataWithClientType(SpanData delegate) {
 *     super(delegate);
 *     String clientType = ClientConfig.parseUserAgent(
 *       delegate.getAttributes().get(SemanticAttributes.HTTP_USER_AGENT).getStringValue());
 *     Attributes.Builder newAttributes = Attributes.builder(delegate.getAttributes());
 *     newAttributes.setAttribute("client_type", clientType);
 *     attributes = newAttributes.build();
 *   }
 *
 *   {@literal @}Override
 *   public ReadableAttributes getAttributes() {
 *     return attributes;
 *   }
 * }
 *
 * }</pre>
 */
public abstract class DelegatingSpanData implements SpanData {

  private final SpanData delegate;

  protected DelegatingSpanData(SpanData delegate) {
    this.delegate = requireNonNull(delegate, "delegate");
  }

  @Override
  public String getTraceId() {
    return delegate.getTraceId();
  }

  @Override
  public String getSpanId() {
    return delegate.getSpanId();
  }

  @Override
  public boolean isSampled() {
    return delegate.isSampled();
  }

  @Override
  public TraceState getTraceState() {
    return delegate.getTraceState();
  }

  @Override
  public String getParentSpanId() {
    return delegate.getParentSpanId();
  }

  @Override
  public Resource getResource() {
    return delegate.getResource();
  }

  @Override
  public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return delegate.getInstrumentationLibraryInfo();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public Kind getKind() {
    return delegate.getKind();
  }

  @Override
  public long getStartEpochNanos() {
    return delegate.getStartEpochNanos();
  }

  @Override
  public ReadableAttributes getAttributes() {
    return delegate.getAttributes();
  }

  @Override
  public List<Event> getEvents() {
    return delegate.getEvents();
  }

  @Override
  public List<Link> getLinks() {
    return delegate.getLinks();
  }

  @Override
  public Status getStatus() {
    return delegate.getStatus();
  }

  @Override
  public long getEndEpochNanos() {
    return delegate.getEndEpochNanos();
  }

  @Override
  public boolean hasEnded() {
    return delegate.hasEnded();
  }

  @Override
  public int getTotalRecordedEvents() {
    return delegate.getTotalRecordedEvents();
  }

  @Override
  public int getTotalRecordedLinks() {
    return delegate.getTotalRecordedLinks();
  }

  @Override
  public int getTotalAttributeCount() {
    return delegate.getTotalAttributeCount();
  }

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
          && getParentSpanId().equals(that.getParentSpanId())
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

  @Override
  public int hashCode() {
    int code = 1;
    code *= 1000003;
    code ^= getTraceId().hashCode();
    code *= 1000003;
    code ^= getSpanId().hashCode();
    code *= 1000003;
    code ^= getTraceId().hashCode();
    code *= 1000003;
    code ^= getTraceState().hashCode();
    code *= 1000003;
    code ^= getParentSpanId().hashCode();
    code *= 1000003;
    code ^= getResource().hashCode();
    code *= 1000003;
    code ^= getInstrumentationLibraryInfo().hashCode();
    code *= 1000003;
    code ^= getName().hashCode();
    code *= 1000003;
    code ^= getKind().hashCode();
    code *= 1000003;
    code ^= (int) ((getStartEpochNanos() >>> 32) ^ getStartEpochNanos());
    code *= 1000003;
    code ^= getAttributes().hashCode();
    code *= 1000003;
    code ^= getEvents().hashCode();
    code *= 1000003;
    code ^= getLinks().hashCode();
    code *= 1000003;
    code ^= getStatus().hashCode();
    code *= 1000003;
    code ^= (int) ((getEndEpochNanos() >>> 32) ^ getEndEpochNanos());
    code *= 1000003;
    code ^= hasEnded() ? 1231 : 1237;
    code *= 1000003;
    code ^= getTotalRecordedEvents();
    code *= 1000003;
    code ^= getTotalRecordedLinks();
    code *= 1000003;
    code ^= getTotalAttributeCount();
    return code;
  }

  @Override
  public String toString() {
    return "SpanDataImpl{"
        + "traceId="
        + getTraceId()
        + ", "
        + "spanId="
        + getSpanId()
        + ", "
        + "isSampled="
        + isSampled()
        + ", "
        + "traceState="
        + getTraceState()
        + ", "
        + "parentSpanId="
        + getParentSpanId()
        + ", "
        + "resource="
        + getResource()
        + ", "
        + "instrumentationLibraryInfo="
        + getInstrumentationLibraryInfo()
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
        + "attributes="
        + getAttributes()
        + ", "
        + "events="
        + getEvents()
        + ", "
        + "links="
        + getLinks()
        + ", "
        + "status="
        + getStatus()
        + ", "
        + "endEpochNanos="
        + getEndEpochNanos()
        + ", "
        + "hasEnded="
        + hasEnded()
        + ", "
        + "totalRecordedEvents="
        + getTotalRecordedEvents()
        + ", "
        + "totalRecordedLinks="
        + getTotalRecordedLinks()
        + ", "
        + "totalAttributeCount="
        + getTotalAttributeCount()
        + "}";
  }
}
