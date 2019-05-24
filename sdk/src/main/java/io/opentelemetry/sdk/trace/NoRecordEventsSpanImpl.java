/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.sdk.trace;

import com.google.common.base.Preconditions;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Status;
import java.util.Map;

/** Implementation for the {@link Span} class that does not record trace events. */
final class NoRecordEventsSpanImpl implements Span {

  // Contains the identifiers associated with this Span.
  private final SpanContext context;

  private NoRecordEventsSpanImpl(SpanContext context) {
    this.context = context;
  }

  static NoRecordEventsSpanImpl create(SpanContext context) {
    return new NoRecordEventsSpanImpl(context);
  }

  @Override
  public void setAttribute(String key, String value) {
    Preconditions.checkNotNull(key, "key");
    Preconditions.checkNotNull(value, "value");
  }

  @Override
  public void setAttribute(String key, long value) {
    Preconditions.checkNotNull(key, "key");
  }

  @Override
  public void setAttribute(String key, double value) {
    Preconditions.checkNotNull(key, "key");
  }

  @Override
  public void setAttribute(String key, boolean value) {
    Preconditions.checkNotNull(key, "key");
  }

  @Override
  public void setAttribute(String key, AttributeValue value) {
    Preconditions.checkNotNull(key, "key");
    Preconditions.checkNotNull(value, "value");
  }

  @Override
  public void addEvent(String name) {
    Preconditions.checkNotNull(name, "name");
  }

  @Override
  public void addEvent(String name, Map<String, AttributeValue> attributes) {
    Preconditions.checkNotNull(name, "name");
    Preconditions.checkNotNull(attributes, "attributes");
  }

  @Override
  public void addEvent(Event event) {
    Preconditions.checkNotNull(event, "event");
  }

  @Override
  public void addLink(Link link) {
    Preconditions.checkNotNull(link, link);
  }

  @Override
  public void setStatus(Status status) {
    Preconditions.checkNotNull(status, "status");
  }

  @Override
  public void updateName(String name) {
    Preconditions.checkNotNull(name, "name");
  }

  @Override
  public void end() {}

  @Override
  public SpanContext getContext() {
    return context;
  }

  @Override
  public boolean isRecordingEvents() {
    return false;
  }
}
