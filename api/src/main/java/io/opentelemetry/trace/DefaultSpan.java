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

package io.opentelemetry.trace;

import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * The {@code DefaultSpan} is the default {@link Span} that is used when no {@code Span}
 * implementation is available. All operations are no-op except context propagation.
 *
 * <p>Used also to stop tracing, see {@link Tracer#withSpan}.
 *
 * @since 0.1.0
 */
@Immutable
final class DefaultSpan implements Span {

  static final Span INVALID =
      new DefaultSpan(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.getDefault(),
          TraceState.getDefault(),
          /* isRemote= */ false);

  private final String traceIdHex;
  private final String spanIdHex;
  private final byte traceFlags;
  private final TraceState traceState;
  private final boolean isRemote;

  DefaultSpan(
      String traceIdHex,
      String spanIdHex,
      byte traceFlags,
      TraceState traceState,
      boolean isRemote) {
    this.traceIdHex = traceIdHex != null ? traceIdHex : TraceId.getInvalid();
    this.spanIdHex = spanIdHex != null ? spanIdHex : SpanId.getInvalid();
    this.traceFlags = traceFlags;
    this.traceState = traceState != null ? traceState : TraceState.getDefault();
    this.isRemote = isRemote;
  }

  @Override
  public String getTraceIdAsHexString() {
    return traceIdHex;
  }

  @Override
  public String getSpanIdAsHexString() {
    return spanIdHex;
  }

  @Override
  public byte getTraceFlags() {
    return traceFlags;
  }

  @Override
  public TraceState getTraceState() {
    return traceState;
  }

  @Override
  public boolean isSampled() {
    return TraceFlags.isSampled(traceFlags);
  }

  @Override
  public boolean isRemote() {
    return isRemote;
  }

  @Override
  public void setAttribute(String key, String value) {}

  @Override
  public void setAttribute(String key, long value) {}

  @Override
  public void setAttribute(String key, double value) {}

  @Override
  public void setAttribute(String key, boolean value) {}

  @Override
  public <T> void setAttribute(AttributeKey<T> key, T value) {}

  @Override
  public void addEvent(String name) {}

  @Override
  public void addEvent(String name, long timestamp) {}

  @Override
  public void addEvent(String name, Attributes attributes) {}

  @Override
  public void addEvent(String name, Attributes attributes, long timestamp) {}

  @Override
  public void addEvent(Event event) {}

  @Override
  public void addEvent(Event event, long timestamp) {}

  @Override
  public void setStatus(Status status) {}

  @Override
  public void recordException(Throwable exception) {}

  @Override
  public void recordException(Throwable exception, Attributes additionalAttributes) {}

  @Override
  public void updateName(String name) {}

  @Override
  public void end() {}

  @Override
  public void end(EndSpanOptions endOptions) {}

  @Override
  public boolean isRecording() {
    return false;
  }

  @Override
  public String toString() {
    return "DefaultSpan";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DefaultSpan)) {
      return false;
    }
    DefaultSpan that = (DefaultSpan) o;
    return traceFlags == that.traceFlags
        && isRemote == that.isRemote
        && traceIdHex.equals(that.traceIdHex)
        && spanIdHex.equals(that.spanIdHex)
        && traceState.equals(that.traceState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(traceIdHex, spanIdHex, traceFlags, traceState, isRemote);
  }
}
