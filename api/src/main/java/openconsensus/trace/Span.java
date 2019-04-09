/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.trace;

import java.util.Map;
import openconsensus.trace.data.AttributeValue;
import openconsensus.trace.data.Event;
import openconsensus.trace.data.Link;
import openconsensus.trace.data.MessageEvent;
import openconsensus.trace.data.Status;

/**
 * An abstract class that represents a span. It has an associated {@link SpanContext}.
 *
 * <p>Spans are created by the {@link SpanBuilder#startSpan} method.
 *
 * <p>{@code Span} <b>must</b> be ended by calling {@link #end()}.
 *
 * @since 0.1.0
 */
public abstract class Span {
  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  public abstract void setAttribute(String key, AttributeValue value);

  /**
   * Sets a set of attributes to the {@code Span}. The effect of this call is equivalent to that of
   * calling {@link #setAttribute(String, AttributeValue)} once for each element in the specified
   * map.
   *
   * @param attributes the attributes that will be added and associated with the {@code Span}.
   * @since 0.1.0
   */
  public abstract void setAttributes(Map<String, AttributeValue> attributes);

  /**
   * Adds an event to the {@code Span}.
   *
   * @param name the name of the event.
   * @since 0.1.0
   */
  public abstract void addEvent(String name);

  /**
   * Adds an event to the {@code Span}.
   *
   * @param name the name of the event.
   * @param attributes the attributes that will be added; these are associated with this event, not
   *     the {@code Span} as for {@link #setAttributes(Map)}.
   * @since 0.1.0
   */
  public abstract void addEvent(String name, Map<String, AttributeValue> attributes);

  /**
   * Adds an event to the {@code Span}.
   *
   * @param event the event to add.
   * @since 0.1.0
   */
  public abstract void addEvent(Event event);

  /**
   * Adds a MessageEvent to the {@code Span}.
   *
   * <p>This function can be used by higher level applications to record messaging event.
   *
   * <p>This method should always be overridden by users whose API versions are larger or equal to
   * {@code 0.12}.
   *
   * @param messageEvent the message to add.
   * @since 0.1.0
   */
  public abstract void addMessageEvent(MessageEvent messageEvent);

  /**
   * Adds a {@link Link} to the {@code Span}.
   *
   * <p>Used (for example) in batching operations, where a single batch handler processes multiple
   * requests from different traces.
   *
   * @param link the link to add.
   * @since 0.1.0
   */
  public abstract void addLink(Link link);

  /**
   * Sets the {@link Status} to the {@code Span}.
   *
   * <p>If used, this will override the default {@code Span} status. Default is {@link Status#OK}.
   *
   * <p>Only the value of the last call will be recorded, and implementations are free to ignore
   * previous calls.
   *
   * @param status the {@link Status} to set.
   * @since 0.1.0
   */
  public abstract void setStatus(Status status);

  /**
   * Marks the end of {@code Span} execution with the default options.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * @since 0.1.0
   */
  public abstract void end();

  /**
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   * @since 0.1.0
   */
  public abstract SpanContext getContext();

  /**
   * Returns {@code true} if this {@code Span} records events (e.g, {@link #addEvent(String)}.
   *
   * @return {@code true} if this {@code Span} records events.
   * @since 0.1.0
   */
  public abstract boolean isRecordingEvents();

  /**
   * Type of span. Can be used to specify additional relationships between spans in addition to a
   * parent/child relationship.
   *
   * @since 0.1.0
   */
  public enum Kind {
    /**
     * Undefined span kind.
     *
     * @since 0.1.0
     */
    UNDEFINED,

    /**
     * Indicates that the span covers server-side handling of an RPC or other remote request.
     *
     * @since 0.1.0
     */
    SERVER,

    /**
     * Indicates that the span covers the client-side wrapper around an RPC or other remote request.
     *
     * @since 0.1.0
     */
    CLIENT,

    /**
     * Indicates that the span describes producer sending a message to a broker. Unlike client and
     * server, there is no direct critical path latency relationship between producer and consumer
     * spans.
     *
     * @since 0.1.0
     */
    PRODUCER,

    /**
     * Indicates that the span describes consumer recieving a message from a broker. Unlike client
     * and server, there is no direct critical path latency relationship between producer and
     * consumer spans.
     *
     * @since 0.1.0
     */
    CONSUMER
  }
}
