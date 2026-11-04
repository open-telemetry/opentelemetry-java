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

import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Status;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.trace.StatusCode;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

final class OpenTelemetryNoRecordEventsSpanImpl extends Span
    implements io.opentelemetry.api.trace.Span {
  private static final EnumSet<Options> NOT_RECORD_EVENTS_SPAN_OPTIONS =
      EnumSet.noneOf(Options.class);

  private OpenTelemetryNoRecordEventsSpanImpl(SpanContext context) {
    super(context, NOT_RECORD_EVENTS_SPAN_OPTIONS);
  }

  static OpenTelemetryNoRecordEventsSpanImpl create(SpanContext context) {
    return new OpenTelemetryNoRecordEventsSpanImpl(context);
  }

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(attributes, "attribute");
  }

  @Override
  public void addAnnotation(Annotation annotation) {
    Objects.requireNonNull(annotation, "annotation");
  }

  @Override
  public void putAttribute(String key, AttributeValue value) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(value, "value");
  }

  @Override
  public void putAttributes(Map<String, AttributeValue> attributes) {
    Objects.requireNonNull(attributes, "attributes");
  }

  @Override
  public void addMessageEvent(MessageEvent messageEvent) {
    Objects.requireNonNull(messageEvent, "messageEvent");
  }

  @Override
  public void addLink(Link link) {
    Objects.requireNonNull(link, "link");
  }

  @Override
  public void setStatus(Status status) {
    Objects.requireNonNull(status, "status");
  }

  @Override
  public io.opentelemetry.api.trace.Span setStatus(StatusCode canonicalCode) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span setStatus(StatusCode canonicalCode, String description) {
    return this;
  }

  @Override
  public void end(EndSpanOptions options) {
    Objects.requireNonNull(options, "options");
  }

  @Override
  public void end(long timestamp, TimeUnit unit) {
    // do nothing
  }

  @Override
  public io.opentelemetry.api.trace.Span setAttribute(String key, @Nullable String value) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span setAttribute(String key, long value) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span setAttribute(String key, double value) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span setAttribute(String key, boolean value) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span setAttribute(String key, Value<?> value) {
    return this;
  }

  @Override
  public <T> io.opentelemetry.api.trace.Span setAttribute(AttributeKey<T> key, @Nullable T value) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name, long timestamp, TimeUnit unit) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(String name, Attributes attributes) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span addEvent(
      String name, Attributes attributes, long timestamp, TimeUnit unit) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span recordException(Throwable exception) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span recordException(
      Throwable exception, Attributes additionalAttributes) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.Span updateName(String name) {
    return this;
  }

  @Override
  public io.opentelemetry.api.trace.SpanContext getSpanContext() {
    return SpanConverter.mapSpanContext(getContext());
  }

  @Override
  public boolean isRecording() {
    return false;
  }
}
