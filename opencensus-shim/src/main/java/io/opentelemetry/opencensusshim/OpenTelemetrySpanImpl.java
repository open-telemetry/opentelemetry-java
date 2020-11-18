/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.opencensusshim;

import static io.opentelemetry.opencensusshim.SpanConverter.MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_COMPRESSED;
import static io.opentelemetry.opencensusshim.SpanConverter.MESSAGE_EVENT_ATTRIBUTE_KEY_SIZE_UNCOMPRESSED;
import static io.opentelemetry.opencensusshim.SpanConverter.MESSAGE_EVENT_ATTRIBUTE_KEY_TYPE;
import static io.opentelemetry.opencensusshim.SpanConverter.setBooleanAttribute;
import static io.opentelemetry.opencensusshim.SpanConverter.setDoubleAttribute;
import static io.opentelemetry.opencensusshim.SpanConverter.setLongAttribute;
import static io.opentelemetry.opencensusshim.SpanConverter.setStringAttribute;

import com.google.common.base.Preconditions;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.StatusCode;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public class OpenTelemetrySpanImpl extends Span implements io.opentelemetry.api.trace.Span {
  private static final Logger LOGGER = Logger.getLogger(OpenTelemetrySpanImpl.class.getName());
  private static final EnumSet<Span.Options> RECORD_EVENTS_SPAN_OPTIONS =
      EnumSet.of(Span.Options.RECORD_EVENTS);

  private final io.opentelemetry.api.trace.Span otSpan;

  OpenTelemetrySpanImpl(io.opentelemetry.api.trace.Span otSpan) {
    super(
        SpanContext.create(
            TraceId.fromBytes(otSpan.getSpanContext().getTraceIdBytes()),
            SpanId.fromBytes(otSpan.getSpanContext().getSpanIdBytes()),
            TraceOptions.builder().setIsSampled(true).build(),
            Tracestate.builder().build()),
        RECORD_EVENTS_SPAN_OPTIONS);
    this.otSpan = otSpan;
  }

  @Override
  public void putAttribute(String key, AttributeValue value) {
    Preconditions.checkNotNull(key, "key");
    Preconditions.checkNotNull(value, "value");
    value.match(
        arg -> otSpan.setAttribute(key, arg),
        arg -> otSpan.setAttribute(key, arg),
        arg -> otSpan.setAttribute(key, arg),
        arg -> otSpan.setAttribute(key, arg),
        arg -> null);
  }

  @Override
  public void putAttributes(Map<String, AttributeValue> attributes) {
    Preconditions.checkNotNull(attributes, "attributes");
    for (Map.Entry<String, AttributeValue> attribute : attributes.entrySet()) {
      putAttribute(attribute.getKey(), attribute.getValue());
    }
  }

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {
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
    AttributesBuilder attributesBuilder = Attributes.builder();
    annotation
        .getAttributes()
        .forEach(
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
    LOGGER.warning("OpenTelemetry does not support links added after a span is created.");
  }

  @Override
  public void addMessageEvent(MessageEvent messageEvent) {
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
  public void setStatus(Status status) {
    Preconditions.checkNotNull(status, "status");
    otSpan.setStatus(status.isOk() ? StatusCode.OK : StatusCode.ERROR);
  }

  @Override
  public io.opentelemetry.api.trace.Span setStatus(StatusCode canonicalCode, String description) {
    return null;
  }

  @Override
  public io.opentelemetry.api.trace.Span setStatus(StatusCode canonicalCode) {
    return null;
  }

  @Override
  public void end(EndSpanOptions options) {
    otSpan.end();
  }

  @Override
  @SuppressWarnings("ParameterPackage")
  public void end(long timestamp, TimeUnit unit) {}

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
  public io.opentelemetry.api.trace.Span addEvent(
      String name, Attributes attributes, long timestamp, TimeUnit unit) {
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
