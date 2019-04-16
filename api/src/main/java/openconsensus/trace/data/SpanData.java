/*
 * Copyright 2017, OpenConsensus Authors
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

package openconsensus.trace.data;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import openconsensus.common.Timestamp;
import openconsensus.internal.Utils;
import openconsensus.trace.AttributeValue;
import openconsensus.trace.Event;
import openconsensus.trace.Link;
import openconsensus.trace.Span;
import openconsensus.trace.Span.Kind;
import openconsensus.trace.SpanContext;
import openconsensus.trace.SpanId;
import openconsensus.trace.Status;

/**
 * Immutable representation of all data collected by the {@link Span} class.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class SpanData {

  /**
   * Returns a new immutable {@code SpanData}.
   *
   * @param context the {@code SpanContext} of the {@code Span}.
   * @param parentSpanId the parent {@code SpanId} of the {@code Span}. {@code null} if the {@code
   *     Span} is a root.
   * @param name the name of the {@code Span}.
   * @param kind the kind of the {@code Span}.
   * @param startTimestamp the start {@code Timestamp} of the {@code Span}.
   * @param attributes the attributes associated with the {@code Span}.
   * @param timedEvents the events associated with the {@code Span}.
   * @param links the links associated with the {@code Span}.
   * @param childSpanCount the number of child spans that were generated while the span was active.
   * @param status the {@code Status} of the {@code Span}. {@code null} if the {@code Span} is still
   *     active.
   * @param endTimestamp the end {@code Timestamp} of the {@code Span}.
   * @return a new immutable {@code SpanData}.
   * @since 0.1.0
   */
  public static SpanData create(
      SpanContext context,
      @Nullable SpanId parentSpanId,
      String name,
      Kind kind,
      Timestamp startTimestamp,
      Map<String, AttributeValue> attributes,
      List<TimedEvent> timedEvents,
      List<Link> links,
      boolean isAsynchronous,
      @Nullable Integer childSpanCount,
      Status status,
      Timestamp endTimestamp) {
    return new AutoValue_SpanData(
        context,
        parentSpanId,
        name,
        kind,
        startTimestamp,
        Collections.unmodifiableMap(new HashMap<>(Utils.checkNotNull(attributes, "attributes"))),
        Collections.unmodifiableList(
            new ArrayList<>(Utils.checkNotNull(timedEvents, "timedEvents"))),
        Collections.unmodifiableList(new ArrayList<>(Utils.checkNotNull(links, "links"))),
        isAsynchronous,
        childSpanCount,
        status,
        endTimestamp);
  }

  /**
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   * @since 0.1.0
   */
  public abstract SpanContext getContext();

  /**
   * Returns the parent {@code SpanId} or {@code null} if the {@code Span} is a root {@code Span}.
   *
   * @return the parent {@code SpanId} or {@code null} if the {@code Span} is a root {@code Span}.
   * @since 0.1.0
   */
  @Nullable
  public abstract SpanId getParentSpanId();

  /**
   * Returns the name of this {@code Span}.
   *
   * @return the name of this {@code Span}.
   * @since 0.1.0
   */
  public abstract String getName();

  /**
   * Returns the kind of this {@code Span}.
   *
   * @return the kind of this {@code Span}.
   * @since 0.1.0
   */
  public abstract Kind getKind();

  /**
   * Returns the start {@code Timestamp} of this {@code Span}.
   *
   * @return the start {@code Timestamp} of this {@code Span}.
   * @since 0.1.0
   */
  public abstract Timestamp getStartTimestamp();

  /**
   * Returns the attributes recorded for this {@code Span}.
   *
   * @return the attributes recorded for this {@code Span}.
   * @since 0.1.0
   */
  public abstract Map<String, AttributeValue> getAttributes();

  /**
   * Returns the timed events recorded for this {@code Span}.
   *
   * @return the timed events recorded for this {@code Span}.
   * @since 0.1.0
   */
  public abstract List<TimedEvent> getTimedEvents();

  /**
   * Returns links recorded for this {@code Span}.
   *
   * @return links recorded for this {@code Span}.
   * @since 0.1.0
   */
  public abstract List<Link> getLinks();

  /**
   * Returns true if this {@code Span} represents an asynchronous operation.
   *
   * @return true if this {@code Span} represent an asynchronous operation.
   */
  public abstract boolean getIsAsynchronous();

  /**
   * Returns the number of child spans that were generated while the {@code Span} was running. If
   * not {@code null} allows service implementations to detect missing child spans.
   *
   * <p>This information is not always available.
   *
   * @return the number of child spans that were generated while the {@code Span} was running.
   * @since 0.1.0
   */
  @Nullable
  public abstract Integer getChildSpanCount();

  /**
   * Returns the {@code Status}.
   *
   * @return the {@code Status}.
   * @since 0.1.0
   */
  public abstract Status getStatus();

  /**
   * Returns the end {@code Timestamp}.
   *
   * @return the end {@code Timestamp}.
   * @since 0.1.0
   */
  public abstract Timestamp getEndTimestamp();

  SpanData() {}

  /**
   * A timed event representation.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class TimedEvent {
    /**
     * Returns a new immutable {@code TimedEvent<T>}.
     *
     * @param timestamp the {@code Timestamp} of this event.
     * @param event the event.
     * @param <T> the type of value that is timed.
     * @return a new immutable {@code TimedEvent<T>}
     * @since 0.1.0
     */
    public static <T> TimedEvent create(Timestamp timestamp, Event event) {
      return new AutoValue_SpanData_TimedEvent(timestamp, event);
    }

    /**
     * Returns the {@code Timestamp} of this event.
     *
     * @return the {@code Timestamp} of this event.
     * @since 0.1.0
     */
    public abstract Timestamp getTimestamp();

    /**
     * Returns the event.
     *
     * @return the event.
     * @since 0.1.0
     */
    public abstract Event getEvent();

    TimedEvent() {}
  }
}
