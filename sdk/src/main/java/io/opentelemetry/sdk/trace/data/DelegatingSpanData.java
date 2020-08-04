package io.opentelemetry.sdk.trace.data;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.List;

/**
 * A {@link SpanData} which delegates all methods to another {@link SpanData}. Extend this class
 * to modify the {@link SpanData} that will be exported, for example by creating a delegating
 * {@link io.opentelemetry.sdk.trace.export.SpanExporter} which wraps
 * {@link SpanData} with a custom implementation.
 *
 * <pre>{@code
 *
 * SpanDataWithClientType extends DelegatingSpanData {
 *
 *   private final ReadableAttributes attributes;
 *
 *   SpanDataWithClientType(SpanData delegate) {
 *     super(delegate);
 *     String clientType = ClientConfig.parseUserAgent(
 *       delegate.getAttributes().get(SemanticConventions.HTTP_USER_AGENT).getStringValue());
 *     Attributes.Builder newAttributes = Attributes.newBuilder();
 *     delegate.getAttributes().forEach(newAttributes::setAttribute);
 *     newAttributes.setAttribute("client_type", clientType);
 *     attributes = newAttributes.build();
 *   }
 *
 *   @Override
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
  public TraceId getTraceId() {
    return delegate.getTraceId();
  }

  @Override
  public SpanId getSpanId() {
    return delegate.getSpanId();
  }

  @Override
  public TraceFlags getTraceFlags() {
    return delegate.getTraceFlags();
  }

  @Override
  public TraceState getTraceState() {
    return delegate.getTraceState();
  }

  @Override
  public SpanId getParentSpanId() {
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
  public boolean getHasRemoteParent() {
    return delegate.getHasRemoteParent();
  }

  @Override
  public boolean getHasEnded() {
    return delegate.getHasEnded();
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
}
