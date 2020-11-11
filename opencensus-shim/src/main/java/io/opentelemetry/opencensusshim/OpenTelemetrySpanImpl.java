package io.opentelemetry.opencensusshim;

import static io.opentelemetry.opencensusshim.SpanConverter.MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_COMPRESSED;
import static io.opentelemetry.opencensusshim.SpanConverter.MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_UNCOMPRESSED;
import static io.opentelemetry.opencensusshim.SpanConverter.MESSAGE_EVENT_ATTRIBUTE_KEY_TYPE;
import static io.opentelemetry.opencensusshim.SpanConverter.setBooleanAttribute;
import static io.opentelemetry.opencensusshim.SpanConverter.setDoubleAttribute;
import static io.opentelemetry.opencensusshim.SpanConverter.setLongAttribute;
import static io.opentelemetry.opencensusshim.SpanConverter.setStringAttribute;

import io.opencensus.common.Clock;
import io.opencensus.implcore.internal.TimestampConverter;
import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.implcore.trace.RecordEventsSpanImpl.StartEndHandler;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.config.TraceParams;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.StatusCode;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenTelemetrySpanImpl extends Span implements io.opentelemetry.api.trace.Span {

  private static final EnumSet<Span.Options> RECORD_EVENTS_SPAN_OPTIONS =
      EnumSet.of(Span.Options.RECORD_EVENTS);

  // The time converter used to convert nano time to Timestamp. This is needed because Java has
  // millisecond granularity for Timestamp and tracing events are recorded more often.
  private final TimestampConverter timestampConverter;

  private final RecordEventsSpanImpl ocSpan;
  private final io.opentelemetry.api.trace.Span otSpan;

  /**
   * Creates a new {@code Span}.
   *
   * @param context the context associated with this {@code Span}.
   * @param options the options associated with this {@code Span}. If {@code null} then default
   *     options will be set.
   * @param timestampConverter time stamp converter
   * @throws NullPointerException if context is {@code null}.
   * @throws IllegalArgumentException if the {@code SpanContext} is sampled but no RECORD_EVENTS
   *     options.
   * @since 0.5
   */
  protected OpenTelemetrySpanImpl(
      SpanContext context,
      @Nullable EnumSet<Options> options,
      RecordEventsSpanImpl ocSpan,
      TimestampConverter timestampConverter) {
    super(context, options);
    this.ocSpan = ocSpan;
    this.otSpan = SpanConverter.toOtelSpan(ocSpan);
    this.timestampConverter = timestampConverter;
  }

  public static OpenTelemetrySpanImpl startSpan(
      SpanContext context,
      String name,
      @Nullable Span.Kind kind,
      @Nullable SpanId parentSpanId,
      @Nullable Boolean hasRemoteParent,
      TraceParams traceParams,
      StartEndHandler startEndHandler,
      @Nullable TimestampConverter timestampConverter,
      Clock clock) {
    RecordEventsSpanImpl ocSpan =
        RecordEventsSpanImpl.startSpan(
            context,
            name,
            kind,
            parentSpanId,
            hasRemoteParent,
            traceParams,
            startEndHandler,
            timestampConverter,
            clock);
    return new OpenTelemetrySpanImpl(
        context, RECORD_EVENTS_SPAN_OPTIONS, ocSpan, timestampConverter);
  }

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {
    ocSpan.addAnnotation(description, attributes);
    AttributesBuilder attributesBuilder = Attributes.builder();
    attributes.forEach(
        (s, attributeValue) ->
            attributeValue.match(
                setStringAttribute(attributesBuilder, s),
                setBooleanAttribute(attributesBuilder, s),
                setLongAttribute(attributesBuilder, s),
                setDoubleAttribute(attributesBuilder, s),
                arg -> null));
    otSpan.addEvent(description, attributesBuilder.build());
  }

  @Override
  public void addAnnotation(Annotation annotation) {
    ocSpan.addAnnotation(annotation);
    AttributesBuilder attributesBuilder = Attributes.builder();
    annotation.getAttributes().forEach(
        (s, attributeValue) ->
            attributeValue.match(
                setStringAttribute(attributesBuilder, s),
                setBooleanAttribute(attributesBuilder, s),
                setLongAttribute(attributesBuilder, s),
                setDoubleAttribute(attributesBuilder, s),
                arg -> null));
    otSpan.addEvent(annotation.getDescription(), attributesBuilder.build());
  }

  @Override
  public void addLink(Link link) {
    // TODO(@zoercai): log not supported
    ocSpan.addLink(link);
  }

  @Override
  public void addMessageEvent(MessageEvent messageEvent) {
    ocSpan.addMessageEvent(messageEvent);
    otSpan.addEvent(
        String.valueOf(messageEvent.getMessageId()),
        Attributes.of(
            AttributeKey.stringKey(MESSAGE_EVENT_ATTRIBUTE_KEY_TYPE),
            messageEvent.getType().toString(),
            AttributeKey.longKey(MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_UNCOMPRESSED),
            messageEvent.getUncompressedMessageSize(),
            AttributeKey.longKey(MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_COMPRESSED),
            messageEvent.getCompressedMessageSize()));
  }

  @Override
  public void end(EndSpanOptions options) {
    ocSpan.end(options);
    otSpan.end();
  }

  @Override
  @SuppressWarnings("ParameterPackage")
  public void end(long timestamp, TimeUnit unit) {}

  /**
   * Returns the {@code TimestampConverter} used by this {@code Span}.
   *
   * @return the {@code TimestampConverter} used by this {@code Span}.
   */
  @Nullable
  TimestampConverter getTimestampConverter() {
    return timestampConverter;
  }

  @Override
  public io.opentelemetry.api.trace.Span setAttribute(String key, @Nonnull String value) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span setAttribute(String key, long value) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span setAttribute(String key, double value) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span setAttribute(String key, boolean value) {
    return null;
  }

  @Override
  public <T> io.opentelemetry.api.trace.Span setAttribute(AttributeKey<T> key, @Nonnull T value) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name, long timestamp, TimeUnit unit) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name, Attributes attributes) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span setStatus(StatusCode canonicalCode) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span setStatus(StatusCode canonicalCode, String description) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span recordException(Throwable exception) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span recordException(
      Throwable exception, Attributes additionalAttributes) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span updateName(String name) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.SpanContext getSpanContext() {
    return otSpan.getSpanContext();
  }

  @Override
  public boolean isRecording() {
    return true;
  }
}
