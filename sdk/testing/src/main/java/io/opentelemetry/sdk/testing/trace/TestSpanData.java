/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.trace;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.InstrumentationScopeUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Immutable representation of all data collected by the {@link io.opentelemetry.api.trace.Span}
 * class.
 */
public class TestSpanData implements SpanData {

  private final String name;
  private final SpanKind kind;
  private final SpanContext spanContext;
  private final SpanContext parentSpanContext;
  private final StatusData status;
  private final long startEpochNanos;
  private final Attributes attributes;
  private final List<EventData> events;
  private final List<LinkData> links;
  private final long endEpochNanos;
  private final int totalRecordedEvents;
  private final int totalRecordedLinks;
  private final int totalAttributeCount;
  private final Resource resource;
  private final boolean internalHasEnded;
  private final InstrumentationScopeInfo instrumentationScopeInfo;

  private TestSpanData(
      String name,
      SpanKind kind,
      SpanContext spanContext,
      SpanContext parentSpanContext,
      StatusData status,
      long startEpochNanos,
      Attributes attributes,
      List<EventData> events,
      List<LinkData> links,
      long endEpochNanos,
      int totalRecordedEvents,
      int totalRecordedLinks,
      int totalAttributeCount,
      Resource resource,
      boolean internalHasEnded,
      InstrumentationScopeInfo instrumentationScopeInfo) {
    this.name = name;
    this.kind = kind;
    this.spanContext = spanContext;
    this.parentSpanContext = parentSpanContext;
    this.status = status;
    this.startEpochNanos = startEpochNanos;
    this.attributes = attributes;
    this.events = events;
    this.links = links;
    this.endEpochNanos = endEpochNanos;
    this.totalRecordedEvents = totalRecordedEvents;
    this.totalRecordedLinks = totalRecordedLinks;
    this.totalAttributeCount = totalAttributeCount;
    this.resource = resource;
    this.internalHasEnded = internalHasEnded;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
  }

  /**
   * Creates a new Builder for creating an SpanData instance.
   *
   * @return a new Builder.
   */
  public static Builder builder() {
    return new Builder()
        .setSpanContext(SpanContext.getInvalid())
        .setParentSpanContext(SpanContext.getInvalid())
        .setInstrumentationScopeInfo(InstrumentationScopeInfo.empty())
        .setLinks(Collections.emptyList())
        .setTotalRecordedLinks(0)
        .setAttributes(Attributes.empty())
        .setEvents(Collections.emptyList())
        .setTotalRecordedEvents(0)
        .setResource(Resource.empty())
        .setTotalAttributeCount(0);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public SpanKind getKind() {
    return kind;
  }

  @Override
  public SpanContext getSpanContext() {
    return spanContext;
  }

  @Override
  public SpanContext getParentSpanContext() {
    return parentSpanContext;
  }

  @Override
  public StatusData getStatus() {
    return status;
  }

  @Override
  public long getStartEpochNanos() {
    return startEpochNanos;
  }

  @Override
  public Attributes getAttributes() {
    return attributes;
  }

  @Override
  public List<EventData> getEvents() {
    return events;
  }

  @Override
  public List<LinkData> getLinks() {
    return links;
  }

  @Override
  public long getEndEpochNanos() {
    return endEpochNanos;
  }

  @Override
  public boolean hasEnded() {
    return internalHasEnded;
  }

  @Override
  public int getTotalRecordedEvents() {
    return totalRecordedEvents;
  }

  @Override
  public int getTotalRecordedLinks() {
    return totalRecordedLinks;
  }

  @Override
  public int getTotalAttributeCount() {
    return totalAttributeCount;
  }

  @Override
  @Deprecated
  public io.opentelemetry.sdk.common.InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return InstrumentationScopeUtil.toInstrumentationLibraryInfo(instrumentationScopeInfo);
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  @Override
  public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  @Override
  public String toString() {
    return "TestSpanData{"
        + "name="
        + name
        + ", "
        + "kind="
        + kind
        + ", "
        + "spanContext="
        + spanContext
        + ", "
        + "parentSpanContext="
        + parentSpanContext
        + ", "
        + "status="
        + status
        + ", "
        + "startEpochNanos="
        + startEpochNanos
        + ", "
        + "attributes="
        + attributes
        + ", "
        + "events="
        + events
        + ", "
        + "links="
        + links
        + ", "
        + "endEpochNanos="
        + endEpochNanos
        + ", "
        + "totalRecordedEvents="
        + totalRecordedEvents
        + ", "
        + "totalRecordedLinks="
        + totalRecordedLinks
        + ", "
        + "totalAttributeCount="
        + totalAttributeCount
        + ", "
        + "resource="
        + resource
        + ", "
        + "internalHasEnded="
        + internalHasEnded
        + ", "
        + "instrumentationScopeInfo="
        + instrumentationScopeInfo
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TestSpanData) {
      TestSpanData that = (TestSpanData) o;
      return this.name.equals(that.getName())
          && this.kind.equals(that.getKind())
          && this.spanContext.equals(that.getSpanContext())
          && this.parentSpanContext.equals(that.getParentSpanContext())
          && this.status.equals(that.getStatus())
          && this.startEpochNanos == that.getStartEpochNanos()
          && this.attributes.equals(that.getAttributes())
          && this.events.equals(that.getEvents())
          && this.links.equals(that.getLinks())
          && this.endEpochNanos == that.getEndEpochNanos()
          && this.totalRecordedEvents == that.getTotalRecordedEvents()
          && this.totalRecordedLinks == that.getTotalRecordedLinks()
          && this.totalAttributeCount == that.getTotalAttributeCount()
          && this.resource.equals(that.getResource())
          && this.internalHasEnded == that.hasEnded()
          && this.instrumentationScopeInfo.equals(that.getInstrumentationScopeInfo());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash *= 1000003;
    hash ^= name.hashCode();
    hash *= 1000003;
    hash ^= kind.hashCode();
    hash *= 1000003;
    hash ^= spanContext.hashCode();
    hash *= 1000003;
    hash ^= parentSpanContext.hashCode();
    hash *= 1000003;
    hash ^= status.hashCode();
    hash *= 1000003;
    hash ^= (int) ((startEpochNanos >>> 32) ^ startEpochNanos);
    hash *= 1000003;
    hash ^= attributes.hashCode();
    hash *= 1000003;
    hash ^= events.hashCode();
    hash *= 1000003;
    hash ^= links.hashCode();
    hash *= 1000003;
    hash ^= (int) ((endEpochNanos >>> 32) ^ endEpochNanos);
    hash *= 1000003;
    hash ^= totalRecordedEvents;
    hash *= 1000003;
    hash ^= totalRecordedLinks;
    hash *= 1000003;
    hash ^= totalAttributeCount;
    hash *= 1000003;
    hash ^= resource.hashCode();
    hash *= 1000003;
    hash ^= internalHasEnded ? 1231 : 1237;
    hash *= 1000003;
    hash ^= instrumentationScopeInfo.hashCode();
    return hash;
  }

  public static class Builder {
    @Nullable private String name;
    @Nullable private SpanKind kind;
    @Nullable private SpanContext spanContext;
    @Nullable private SpanContext parentSpanContext;
    @Nullable private StatusData status;
    @Nullable private Long startEpochNanos;
    @Nullable private Attributes attributes;
    @Nullable private List<EventData> events;
    @Nullable private List<LinkData> links;
    @Nullable private Long endEpochNanos;
    @Nullable private Integer totalRecordedEvents;
    @Nullable private Integer totalRecordedLinks;
    @Nullable private Integer totalAttributeCount;
    @Nullable private Resource resource;
    @Nullable private Boolean internalHasEnded;
    @Nullable private InstrumentationScopeInfo instrumentationScopeInfo;

    public Builder() {}

    /** Set the name. */
    public TestSpanData.Builder setName(String name) {
      if (name == null) {
        throw new NullPointerException("Null name");
      }
      this.name = name;
      return this;
    }

    /** Set the span kind. */
    public TestSpanData.Builder setKind(SpanKind kind) {
      if (kind == null) {
        throw new NullPointerException("Null kind");
      }
      this.kind = kind;
      return this;
    }

    /** Set the span context. */
    public TestSpanData.Builder setSpanContext(SpanContext spanContext) {
      if (spanContext == null) {
        throw new NullPointerException("Null spanContext");
      }
      this.spanContext = spanContext;
      return this;
    }

    /** Set the parent span context. */
    public TestSpanData.Builder setParentSpanContext(SpanContext parentSpanContext) {
      if (parentSpanContext == null) {
        throw new NullPointerException("Null parentSpanContext");
      }
      this.parentSpanContext = parentSpanContext;
      return this;
    }

    /** Set the status. */
    public TestSpanData.Builder setStatus(StatusData status) {
      if (status == null) {
        throw new NullPointerException("Null status");
      }
      this.status = status;
      return this;
    }

    /** Set the start epoch nanos. */
    public TestSpanData.Builder setStartEpochNanos(long startEpochNanos) {
      this.startEpochNanos = startEpochNanos;
      return this;
    }

    /** Set the attributes. */
    public TestSpanData.Builder setAttributes(Attributes attributes) {
      if (attributes == null) {
        throw new NullPointerException("Null attributes");
      }
      this.attributes = attributes;
      return this;
    }

    /** Set the events. */
    public TestSpanData.Builder setEvents(List<EventData> events) {
      if (events == null) {
        throw new NullPointerException("Null events");
      }
      this.events = events;
      return this;
    }

    /** Set the links. */
    public TestSpanData.Builder setLinks(List<LinkData> links) {
      if (links == null) {
        throw new NullPointerException("Null links");
      }
      this.links = links;
      return this;
    }

    /** Set the end epoch nanos. */
    public TestSpanData.Builder setEndEpochNanos(long endEpochNanos) {
      this.endEpochNanos = endEpochNanos;
      return this;
    }

    /** Set the total recorded events. */
    public TestSpanData.Builder setTotalRecordedEvents(int totalRecordedEvents) {
      this.totalRecordedEvents = totalRecordedEvents;
      return this;
    }

    /** Set the total recorded links. */
    public TestSpanData.Builder setTotalRecordedLinks(int totalRecordedLinks) {
      this.totalRecordedLinks = totalRecordedLinks;
      return this;
    }

    /** Set the total attribute count. */
    public TestSpanData.Builder setTotalAttributeCount(int totalAttributeCount) {
      this.totalAttributeCount = totalAttributeCount;
      return this;
    }

    /** Set the resource. */
    public TestSpanData.Builder setResource(Resource resource) {
      if (resource == null) {
        throw new NullPointerException("Null resource");
      }
      this.resource = resource;
      return this;
    }

    /** Set has ended. */
    public TestSpanData.Builder setHasEnded(boolean internalHasEnded) {
      this.internalHasEnded = internalHasEnded;
      return this;
    }

    /** Set the instrumentation library info. */
    @Deprecated
    public TestSpanData.Builder setInstrumentationLibraryInfo(
        io.opentelemetry.sdk.common.InstrumentationLibraryInfo instrumentationLibraryInfo) {
      if (instrumentationLibraryInfo == null) {
        throw new NullPointerException("Null instrumentationLibraryInfo");
      }
      return setInstrumentationScopeInfo(
          InstrumentationScopeUtil.toInstrumentationScopeInfo(instrumentationLibraryInfo));
    }

    /** Set the instrumentation scope info. */
    public TestSpanData.Builder setInstrumentationScopeInfo(
        InstrumentationScopeInfo instrumentationScopeInfo) {
      if (instrumentationScopeInfo == null) {
        throw new NullPointerException("Null instrumentationScopeInfo");
      }
      this.instrumentationScopeInfo = instrumentationScopeInfo;
      return this;
    }

    /** Build the test span data. */
    public TestSpanData build() {
      if (this.name == null
          || this.kind == null
          || this.spanContext == null
          || this.parentSpanContext == null
          || this.status == null
          || this.startEpochNanos == null
          || this.attributes == null
          || this.events == null
          || this.links == null
          || this.endEpochNanos == null
          || this.totalRecordedEvents == null
          || this.totalRecordedLinks == null
          || this.totalAttributeCount == null
          || this.resource == null
          || this.internalHasEnded == null
          || this.instrumentationScopeInfo == null) {
        StringBuilder missing = new StringBuilder();
        if (this.name == null) {
          missing.append(" name");
        }
        if (this.kind == null) {
          missing.append(" kind");
        }
        if (this.spanContext == null) {
          missing.append(" spanContext");
        }
        if (this.parentSpanContext == null) {
          missing.append(" parentSpanContext");
        }
        if (this.status == null) {
          missing.append(" status");
        }
        if (this.startEpochNanos == null) {
          missing.append(" startEpochNanos");
        }
        if (this.attributes == null) {
          missing.append(" attributes");
        }
        if (this.events == null) {
          missing.append(" events");
        }
        if (this.links == null) {
          missing.append(" links");
        }
        if (this.endEpochNanos == null) {
          missing.append(" endEpochNanos");
        }
        if (this.totalRecordedEvents == null) {
          missing.append(" totalRecordedEvents");
        }
        if (this.totalRecordedLinks == null) {
          missing.append(" totalRecordedLinks");
        }
        if (this.totalAttributeCount == null) {
          missing.append(" totalAttributeCount");
        }
        if (this.resource == null) {
          missing.append(" resource");
        }
        if (this.internalHasEnded == null) {
          missing.append(" internalHasEnded");
        }
        if (this.instrumentationScopeInfo == null) {
          missing.append(" instrumentationScopeInfo");
        }
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new TestSpanData(
          this.name,
          this.kind,
          this.spanContext,
          this.parentSpanContext,
          this.status,
          this.startEpochNanos,
          this.attributes,
          Collections.unmodifiableList(this.events),
          Collections.unmodifiableList(this.links),
          this.endEpochNanos,
          this.totalRecordedEvents,
          this.totalRecordedLinks,
          this.totalAttributeCount,
          this.resource,
          this.internalHasEnded,
          this.instrumentationScopeInfo);
    }
  }
}
