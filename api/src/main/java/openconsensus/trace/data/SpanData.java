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
import openconsensus.trace.Span;
import openconsensus.trace.Span.Kind;
import openconsensus.trace.SpanContext;
import openconsensus.trace.SpanId;

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
   * @param events the events associated with the {@code Span}.
   * @param messageEvents the message events (or network events for backward compatibility)
   *     associated with the {@code Span}.
   * @param links the links associated with the {@code Span}.
   * @param childSpanCount the number of child spans that were generated while the span was active.
   * @param status the {@code Status} of the {@code Span}. {@code null} if the {@code Span} is still
   *     active.
   * @param endTimestamp the end {@code Timestamp} of the {@code Span}. {@code null} if the {@code
   *     Span} is still active.
   * @return a new immutable {@code SpanData}.
   * @since 0.1.0
   */
  public static SpanData create(
      SpanContext context,
      @Nullable SpanId parentSpanId,
      String name,
      @Nullable Kind kind,
      Timestamp startTimestamp,
      Attributes attributes,
      TimedEvents<Event> events,
      TimedEvents<MessageEvent> messageEvents,
      Links links,
      @Nullable Integer childSpanCount,
      @Nullable Status status,
      @Nullable Timestamp endTimestamp) {
    Utils.checkNotNull(messageEvents, "messageEvents");
    kind = kind == null ? Kind.INTERNAL : kind;
    return new AutoValue_SpanData(
        context,
        parentSpanId,
        name,
        kind,
        startTimestamp,
        attributes,
        events,
        messageEvents,
        links,
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
  public abstract Attributes getAttributes();

  /**
   * Returns the events recorded for this {@code Span}.
   *
   * @return the events recorded for this {@code Span}.
   * @since 0.1.0
   */
  public abstract TimedEvents<Event> getEvents();

  /**
   * Returns message events recorded for this {@code Span}.
   *
   * @return message events recorded for this {@code Span}.
   * @since 0.1.0
   */
  public abstract TimedEvents<MessageEvent> getMessageEvents();

  /**
   * Returns links recorded for this {@code Span}.
   *
   * @return links recorded for this {@code Span}.
   * @since 0.1.0
   */
  public abstract Links getLinks();

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
   * Returns the {@code Status} or {@code null} if {@code Span} is still active.
   *
   * @return the {@code Status} or {@code null} if {@code Span} is still active.
   * @since 0.1.0
   */
  @Nullable
  public abstract Status getStatus();

  /**
   * Returns the end {@code Timestamp} or {@code null} if the {@code Span} is still active.
   *
   * @return the end {@code Timestamp} or {@code null} if the {@code Span} is still active.
   * @since 0.1.0
   */
  @Nullable
  public abstract Timestamp getEndTimestamp();

  SpanData() {}

  /**
   * A timed event representation.
   *
   * @param <T> the type of value that is timed.
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class TimedEvent<T> {
    /**
     * Returns a new immutable {@code TimedEvent<T>}.
     *
     * @param timestamp the {@code Timestamp} of this event.
     * @param event the event.
     * @param <T> the type of value that is timed.
     * @return a new immutable {@code TimedEvent<T>}
     * @since 0.1.0
     */
    public static <T> TimedEvent<T> create(Timestamp timestamp, T event) {
      return new AutoValue_SpanData_TimedEvent<T>(timestamp, event);
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
    public abstract T getEvent();

    TimedEvent() {}
  }

  /**
   * A list of timed events and the number of dropped events representation.
   *
   * @param <T> the type of value that is timed.
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class TimedEvents<T> {
    /**
     * Returns a new immutable {@code TimedEvents<T>}.
     *
     * @param events the list of events.
     * @param droppedEventsCount the number of dropped events.
     * @param <T> the type of value that is timed.
     * @return a new immutable {@code TimedEvents<T>}
     * @since 0.1.0
     */
    public static <T> TimedEvents<T> create(List<TimedEvent<T>> events, int droppedEventsCount) {
      return new AutoValue_SpanData_TimedEvents<T>(
          Collections.unmodifiableList(
              new ArrayList<TimedEvent<T>>(Utils.checkNotNull(events, "events"))),
          droppedEventsCount);
    }

    /**
     * Returns the list of events.
     *
     * @return the list of events.
     * @since 0.1.0
     */
    public abstract List<TimedEvent<T>> getEvents();

    /**
     * Returns the number of dropped events.
     *
     * @return the number of dropped events.
     * @since 0.1.0
     */
    public abstract int getDroppedEventsCount();

    TimedEvents() {}
  }

  /**
   * A set of attributes and the number of dropped attributes representation.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class Attributes {
    /**
     * Returns a new immutable {@code Attributes}.
     *
     * @param attributeMap the set of attributes.
     * @param droppedAttributesCount the number of dropped attributes.
     * @return a new immutable {@code Attributes}.
     * @since 0.1.0
     */
    public static Attributes create(
        Map<String, AttributeValue> attributeMap, int droppedAttributesCount) {
      // TODO(bdrutu): Consider to use LinkedHashMap here and everywhere else, less test flakes
      // for others on account of determinism.
      return new AutoValue_SpanData_Attributes(
          Collections.unmodifiableMap(
              new HashMap<String, AttributeValue>(
                  Utils.checkNotNull(attributeMap, "attributeMap"))),
          droppedAttributesCount);
    }

    /**
     * Returns the set of attributes.
     *
     * @return the set of attributes.
     * @since 0.1.0
     */
    public abstract Map<String, AttributeValue> getAttributeMap();

    /**
     * Returns the number of dropped attributes.
     *
     * @return the number of dropped attributes.
     * @since 0.1.0
     */
    public abstract int getDroppedAttributesCount();

    Attributes() {}
  }

  /**
   * A list of links and the number of dropped links representation.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class Links {
    /**
     * Returns a new immutable {@code Links}.
     *
     * @param links the list of links.
     * @param droppedLinksCount the number of dropped links.
     * @return a new immutable {@code Links}.
     * @since 0.1.0
     */
    public static Links create(List<Link> links, int droppedLinksCount) {
      return new AutoValue_SpanData_Links(
          Collections.unmodifiableList(new ArrayList<Link>(Utils.checkNotNull(links, "links"))),
          droppedLinksCount);
    }

    /**
     * Returns the list of links.
     *
     * @return the list of links.
     * @since 0.1.0
     */
    public abstract List<Link> getLinks();

    /**
     * Returns the number of dropped links.
     *
     * @return the number of dropped links.
     * @since 0.1.0
     */
    public abstract int getDroppedLinksCount();

    Links() {}
  }
}
