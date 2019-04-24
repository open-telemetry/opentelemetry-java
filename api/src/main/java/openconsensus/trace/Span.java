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

/**
 * An interface that represents a span. It has an associated {@link SpanContext}.
 *
 * <p>Spans are created by the {@link SpanBuilder#startSpan} method.
 *
 * <p>{@code Span} <b>must</b> be ended by calling {@link #end()}.
 *
 * @since 0.1.0
 */
public interface Span {
  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  void setAttribute(String key, String value);

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  void setAttribute(String key, long value);

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  void setAttribute(String key, double value);

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  void setAttribute(String key, boolean value);

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  void setAttribute(String key, AttributeValue value);

  /**
   * Adds an event to the {@code Span}.
   *
   * @param name the name of the event.
   * @since 0.1.0
   */
  void addEvent(String name);

  /**
   * Adds an event to the {@code Span}.
   *
   * @param name the name of the event.
   * @param attributes the attributes that will be added; these are associated with this event, not
   *     the {@code Span} as for {@code setAttributes()}.
   * @since 0.1.0
   */
  void addEvent(String name, Map<String, AttributeValue> attributes);

  /**
   * Adds an event to the {@code Span}.
   *
   * @param event the event to add.
   * @since 0.1.0
   */
  void addEvent(Event event);

  /**
   * Adds a {@link Link} to the {@code Span}.
   *
   * <p>Used (for example) in batching operations, where a single batch handler processes multiple
   * requests from different traces.
   *
   * @param link the link to add.
   * @since 0.1.0
   */
  void addLink(Link link);

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
  void setStatus(Status status);

  /**
   * Updates the {@code Span} name.
   *
   * <p>If used, this will override the name provided via {@code SpanBuilder}.
   *
   * <p>Upon this update, any sampling behavior based on {@code Span} name will depend on the
   * implementation.
   *
   * @param name the {@code Span} name.
   * @since 0.1
   */
  void updateName(String name);

  /**
   * Marks the end of {@code Span} execution with the default options.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * @since 0.1.0
   */
  void end();

  /**
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   * @since 0.1.0
   */
  SpanContext getContext();

  /**
   * Returns {@code true} if this {@code Span} records events (e.g, {@link #addEvent(String)}.
   *
   * @return {@code true} if this {@code Span} records events.
   * @since 0.1.0
   */
  boolean isRecordingEvents();

  /**
   * Type of span. Can be used to specify additional relationships between spans in addition to a
   * parent/child relationship.
   *
   * @since 0.1.0
   */
  public enum Kind {
    /**
     * Default value. Indicates that the span is used internally.
     *
     * @since 0.1.0
     */
    INTERNAL,

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
