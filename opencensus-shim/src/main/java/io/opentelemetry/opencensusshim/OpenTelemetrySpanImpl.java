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
import static io.opentelemetry.opencensusshim.SpanConverter.mapSpanContext;
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
import io.opencensus.trace.Status;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.Nullable;

class OpenTelemetrySpanImpl extends Span implements io.opentelemetry.api.trace.Span {
  private static final Logger LOGGER = Logger.getLogger(OpenTelemetrySpanImpl.class.getName());
  private static final EnumSet<Span.Options> RECORD_EVENTS_SPAN_OPTIONS =
      EnumSet.of(Span.Options.RECORD_EVENTS);

  private final io.opentelemetry.api.trace.Span otelSpan;

  OpenTelemetrySpanImpl(io.opentelemetry.api.trace.Span otelSpan) {
    super(mapSpanContext(otelSpan.getSpanContext()), RECORD_EVENTS_SPAN_OPTIONS);
    this.otelSpan = otelSpan;
  }

  @Override
  public void putAttribute(String key, AttributeValue value) {
    Preconditions.checkNotNull(key, "key");
    Preconditions.checkNotNull(value, "value");
    value.match(
        arg -> otelSpan.setAttribute(key, arg),
        arg -> otelSpan.setAttribute(key, arg),
        arg -> otelSpan.setAttribute(key, arg),
        arg -> otelSpan.setAttribute(key, arg),
        arg -> null);
  }

  @Override
  public void putAttributes(Map<String, AttributeValue> attributes) {
    Preconditions.checkNotNull(attributes, "attributes");
    attributes.forEach(this::putAttribute);
  }

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    mapAttributes(attributes, attributesBuilder);
    otelSpan.addEvent(description, attributesBuilder.build());
  }

  @Override
  public void addAnnotation(Annotation annotation) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    mapAttributes(annotation.getAttributes(), attributesBuilder);
    otelSpan.addEvent(annotation.getDescription(), attributesBuilder.build());
  }

  @Override
  public void addLink(Link link) {
    LOGGER.warning("OpenTelemetry does not support links added after a span is created.");
  }

  @Override
  public void addMessageEvent(MessageEvent messageEvent) {
    otelSpan.addEvent(
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
    otelSpan.setStatus(status.isOk() ? StatusCode.OK : StatusCode.ERROR);
  }

  @Override
  public io.opentelemetry.api.trace.Span setStatus(StatusCode canonicalCode, String description) {
    return otelSpan.setStatus(canonicalCode, description);
  }

  @Override
  public void end(EndSpanOptions options) {
    otelSpan.end();
  }

  @Override
  @SuppressWarnings("ParameterPackage")
  public void end(long timestamp, TimeUnit unit) {
    otelSpan.end(timestamp, unit);
  }

  @Override
  public <T> io.opentelemetry.api.trace.Span setAttribute(AttributeKey<T> key, @Nullable T value) {
    return otelSpan.setAttribute(key, value);
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name) {
    return otelSpan.addEvent(name);
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name, long timestamp, TimeUnit unit) {
    return otelSpan.addEvent(name, timestamp, unit);
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name, Attributes attributes) {
    return otelSpan.addEvent(name, attributes);
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(
      String name, Attributes attributes, long timestamp, TimeUnit unit) {
    return otelSpan.addEvent(name, attributes, timestamp, unit);
  }

  @Override
  public io.opentelemetry.api.trace.Span recordException(Throwable exception) {
    return otelSpan.recordException(exception);
  }

  @Override
  public io.opentelemetry.api.trace.Span recordException(
      Throwable exception, Attributes additionalAttributes) {
    return otelSpan.recordException(exception, additionalAttributes);
  }

  @Override
  public io.opentelemetry.api.trace.Span updateName(String name) {
    return otelSpan.updateName(name);
  }

  @Override
  public SpanContext getSpanContext() {
    return otelSpan.getSpanContext();
  }

  @Override
  public boolean isRecording() {
    return true;
  }

  private static void mapAttributes(
      Map<String, AttributeValue> attributes, AttributesBuilder attributesBuilder) {
    attributes.forEach(
        (s, attributeValue) ->
            attributeValue.match(
                setStringAttribute(attributesBuilder, s),
                setBooleanAttribute(attributesBuilder, s),
                setLongAttribute(attributesBuilder, s),
                setDoubleAttribute(attributesBuilder, s),
                arg -> null));
  }
}
