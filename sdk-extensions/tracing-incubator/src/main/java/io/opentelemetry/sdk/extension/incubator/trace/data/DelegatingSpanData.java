/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.data;

import static java.util.Objects.requireNonNull;

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

/**
 * A {@link SpanData} which delegates all methods to another {@link SpanData}. Extend this class to
 * modify the {@link SpanData} that will be exported, for example by creating a delegating {@link
 * io.opentelemetry.sdk.trace.export.SpanExporter} which wraps {@link SpanData} with a custom
 * implementation.
 * <pre>{@code
 * // class SpanDataWithClientType extends DelegatingSpanData {
 * //
 * //   private final Attributes attributes;
 * //
 * //   SpanDataWithClientType(SpanData delegate) {
 * //     super(delegate);
 * //     String clientType = ClientConfig.parseUserAgent(
 * //       delegate.getAttributes().get(SemanticAttributes.HTTP_USER_AGENT).getStringValue());
 * //     Attributes.Builder newAttributes = Attributes.builder(delegate.getAttributes());
 * //     newAttributes.setAttribute("client_type", clientType);
 * //     attributes = newAttributes.build();
 * //   }
 * //
 * //   @Override
 * //   public Attributes getAttributes() {
 * //     return attributes;
 * //   }
 * // }
 * }</pre>
 */
public abstract class DelegatingSpanData implements SpanData {

  private final SpanData delegate;

  protected DelegatingSpanData(SpanData delegate) {
    this.delegate = requireNonNull(delegate, "delegate");
  }

  @Override
  public SpanContext getSpanContext() {
    return delegate.getSpanContext();
  }

  @Override
  public SpanContext getParentSpanContext() {
    return delegate.getParentSpanContext();
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
  public SpanKind getKind() {
    return delegate.getKind();
  }

  @Override
  public long getStartEpochNanos() {
    return delegate.getStartEpochNanos();
  }

  @Override
  public Attributes getAttributes() {
    return delegate.getAttributes();
  }

  @Override
  public List<EventData> getEvents() {
    return delegate.getEvents();
  }

  @Override
  public List<LinkData> getLinks() {
    return delegate.getLinks();
  }

  @Override
  public StatusData getStatus() {
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
      return getSpanContext().equals(that.getSpanContext())
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

  @Override
  public int hashCode() {
    int code = 1;
    code *= 1000003;
    code ^= getSpanContext().hashCode();
    code *= 1000003;
    code ^= getParentSpanContext().hashCode();
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
        + "spanContext="
        + getSpanContext()
        + ", "
        + "parentSpanContext="
        + getParentSpanContext()
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
