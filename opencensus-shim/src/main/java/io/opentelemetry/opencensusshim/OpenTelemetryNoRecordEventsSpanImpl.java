package io.opentelemetry.opencensusshim;

import com.google.common.base.Preconditions;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Status;
import java.util.EnumSet;
import java.util.Map;

public class OpenTelemetryNoRecordEventsSpanImpl extends Span {
  private static final EnumSet<Options> NOT_RECORD_EVENTS_SPAN_OPTIONS =
      EnumSet.noneOf(Options.class);

  static OpenTelemetryNoRecordEventsSpanImpl create(SpanContext context) {
    return new OpenTelemetryNoRecordEventsSpanImpl(context);
  }

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {
    Preconditions.checkNotNull(description, "description");
    Preconditions.checkNotNull(attributes, "attribute");
  }

  @Override
  public void addAnnotation(Annotation annotation) {
    Preconditions.checkNotNull(annotation, "annotation");
  }

  @Override
  public void putAttribute(String key, AttributeValue value) {
    Preconditions.checkNotNull(key, "key");
    Preconditions.checkNotNull(value, "value");
  }

  @Override
  public void putAttributes(Map<String, AttributeValue> attributes) {
    Preconditions.checkNotNull(attributes, "attributes");
  }

  @Override
  public void addMessageEvent(MessageEvent messageEvent) {
    Preconditions.checkNotNull(messageEvent, "messageEvent");
  }

  @Override
  public void addLink(Link link) {
    Preconditions.checkNotNull(link, "link");
  }

  @Override
  public void setStatus(Status status) {
    Preconditions.checkNotNull(status, "status");
  }

  @Override
  public void end(EndSpanOptions options) {
    Preconditions.checkNotNull(options, "options");
  }

  private OpenTelemetryNoRecordEventsSpanImpl(SpanContext context) {
    super(context, NOT_RECORD_EVENTS_SPAN_OPTIONS);
  }
}
