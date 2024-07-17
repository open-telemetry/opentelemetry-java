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
import java.util.logging.Logger;

class OpenTelemetrySpanImpl extends Span
    implements io.opentelemetry.api.trace.Span, DelegatingSpan {
  private static final Logger LOGGER = Logger.getLogger(OpenTelemetrySpanImpl.class.getName());
  private static final EnumSet<Span.Options> RECORD_EVENTS_SPAN_OPTIONS =
      EnumSet.of(Span.Options.RECORD_EVENTS);

  private final io.opentelemetry.api.trace.Span otelSpan;

  OpenTelemetrySpanImpl(io.opentelemetry.api.trace.Span otelSpan) {
    super(mapSpanContext(otelSpan.getSpanContext()), RECORD_EVENTS_SPAN_OPTIONS);
    this.otelSpan = otelSpan;
  }

  // otel

  @Override
  public io.opentelemetry.api.trace.Span getDelegate() {
    return otelSpan;
  }

  // opencensus

  @Override
  public void putAttribute(String key, AttributeValue value) {
    Preconditions.checkNotNull(key, "key");
    Preconditions.checkNotNull(value, "value");
    value.match(
        arg -> DelegatingSpan.super.setAttribute(key, arg),
        arg -> DelegatingSpan.super.setAttribute(key, arg),
        arg -> DelegatingSpan.super.setAttribute(key, arg),
        arg -> DelegatingSpan.super.setAttribute(key, arg),
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
    DelegatingSpan.super.addEvent(description, attributesBuilder.build());
  }

  @Override
  public void addAnnotation(Annotation annotation) {
    AttributesBuilder attributesBuilder = Attributes.builder();
    mapAttributes(annotation.getAttributes(), attributesBuilder);
    DelegatingSpan.super.addEvent(annotation.getDescription(), attributesBuilder.build());
  }

  @Override
  public void addLink(Link link) {
    LOGGER.warning("OpenTelemetry does not support links added after a span is created.");
  }

  @Override
  public void addMessageEvent(MessageEvent messageEvent) {
    DelegatingSpan.super.addEvent(
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
    DelegatingSpan.super.setStatus(status.isOk() ? StatusCode.OK : StatusCode.ERROR);
  }

  @Override
  public io.opentelemetry.api.trace.Span setStatus(StatusCode canonicalCode, String description) {
    return DelegatingSpan.super.setStatus(canonicalCode, description);
  }

  @Override
  public void end(EndSpanOptions options) {
    DelegatingSpan.super.end();
  }

  @Override
  public SpanContext getSpanContext() {
    return DelegatingSpan.super.getSpanContext();
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
