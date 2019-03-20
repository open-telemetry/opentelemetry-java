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

import openconsensus.internal.Utils;
import openconsensus.trace.internal.BaseMessageEventUtils;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * An abstract class that represents a span. It has an associated {@link SpanContext} and a set of
 * {@link Options}.
 *
 * <p>Spans are created by the {@link SpanBuilder#startSpan} method.
 *
 * <p>{@code Span} <b>must</b> be ended by calling {@link #end()} or {@link #end(EndSpanOptions)}
 *
 * @since 0.1.0
 */
public abstract class Span {
  private static final Map<String, AttributeValue> EMPTY_ATTRIBUTES = Collections.emptyMap();

  // Contains the identifiers associated with this Span.
  private final SpanContext context;

  // Contains the options associated with this Span. This object is immutable.
  private final Set<Options> options;

  /**
   * {@code Span} options. These options are NOT propagated to child spans. These options determine
   * features such as whether a {@code Span} should record any annotations or events.
   *
   * @since 0.1.0
   */
  public enum Options {
    /**
     * This option is set if the Span is part of a sampled distributed trace OR {@link
     * SpanBuilder#setRecordEvents(boolean)} was called with true.
     *
     * @since 0.1.0
     */
    RECORD_EVENTS;
  }

  private static final Set<Options> DEFAULT_OPTIONS =
      Collections.unmodifiableSet(EnumSet.noneOf(Options.class));

  /**
   * Creates a new {@code Span}.
   *
   * @param context the context associated with this {@code Span}.
   * @param options the options associated with this {@code Span}. If {@code null} then default
   *     options will be set.
   * @throws NullPointerException if context is {@code null}.
   * @throws IllegalArgumentException if the {@code SpanContext} is sampled but no RECORD_EVENTS
   *     options.
   * @since 0.1.0
   */
  protected Span(SpanContext context, @Nullable EnumSet<Options> options) {
    this.context = Utils.checkNotNull(context, "context");
    this.options =
        options == null
            ? DEFAULT_OPTIONS
            : Collections.<Options>unmodifiableSet(EnumSet.copyOf(options));
    Utils.checkArgument(
        !context.getTraceOptions().isSampled() || this.options.contains(Options.RECORD_EVENTS),
        "Span is sampled, but does not have RECORD_EVENTS set.");
  }

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  public void putAttribute(String key, AttributeValue value) {
    // Not final because for performance reasons we want to override this in the implementation.
    // Also a default implementation is needed to not break the compatibility (users may extend this
    // for testing).
    Utils.checkNotNull(key, "key");
    Utils.checkNotNull(value, "value");
    putAttributes(Collections.singletonMap(key, value));
  }

  /**
   * Sets a set of attributes to the {@code Span}. The effect of this call is equivalent to that of
   * calling {@link #putAttribute(String, AttributeValue)} once for each element in the specified
   * map.
   *
   * @param attributes the attributes that will be added and associated with the {@code Span}.
   * @since 0.1.0
   */
  public void putAttributes(Map<String, AttributeValue> attributes) {
    // Not final because we want to start overriding this method from the beginning, this will
    // allow us to remove the addAttributes faster. All implementations MUST override this method.
    Utils.checkNotNull(attributes, "attributes");
    addAttributes(attributes);
  }

  /**
   * Sets a set of attributes to the {@code Span}. The effect of this call is equivalent to that of
   * calling {@link #putAttribute(String, AttributeValue)} once for each element in the specified
   * map.
   *
   * @deprecated Use {@link #putAttributes(Map)}
   * @param attributes the attributes that will be added and associated with the {@code Span}.
   * @since 0.1.0
   */
  @Deprecated
  public void addAttributes(Map<String, AttributeValue> attributes) {
    putAttributes(attributes);
  }

  /**
   * Adds an annotation to the {@code Span}.
   *
   * @param description the description of the annotation time event.
   * @since 0.1.0
   */
  public final void addAnnotation(String description) {
    Utils.checkNotNull(description, "description");
    addAnnotation(description, EMPTY_ATTRIBUTES);
  }

  /**
   * Adds an annotation to the {@code Span}.
   *
   * @param description the description of the annotation time event.
   * @param attributes the attributes that will be added; these are associated with this annotation,
   *     not the {@code Span} as for {@link #putAttributes(Map)}.
   * @since 0.1.0
   */
  public abstract void addAnnotation(String description, Map<String, AttributeValue> attributes);

  /**
   * Adds an annotation to the {@code Span}.
   *
   * @param annotation the annotations to add.
   * @since 0.1.0
   */
  public abstract void addAnnotation(Annotation annotation);

  /**
   * Adds a NetworkEvent to the {@code Span}.
   *
   * <p>This function is only intended to be used by RPC systems (either client or server), not by
   * higher level applications.
   *
   * @param networkEvent the network event to add.
   * @deprecated Use {@link #addMessageEvent}.
   * @since 0.1.0
   */
  @Deprecated
  public void addNetworkEvent(NetworkEvent networkEvent) {
    addMessageEvent(BaseMessageEventUtils.asMessageEvent(networkEvent));
  }

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
  public void addMessageEvent(MessageEvent messageEvent) {
    // Default implementation by invoking addNetworkEvent() so that any existing derived classes,
    // including implementation and the mocked ones, do not need to override this method explicitly.
    Utils.checkNotNull(messageEvent, "messageEvent");
    addNetworkEvent(BaseMessageEventUtils.asNetworkEvent(messageEvent));
  }

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
   * previous calls. If the status is set via {@link EndSpanOptions.Builder#setStatus(Status)} that
   * will always be the last call.
   *
   * @param status the {@link Status} to set.
   * @since 0.1.0
   */
  public void setStatus(Status status) {
    // Implemented as no-op for backwards compatibility (for example gRPC extends Span in tests).
    // Implementation must override this method.
    Utils.checkNotNull(status, "status");
  }

  /**
   * Marks the end of {@code Span} execution with the given options.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * @param options the options to be used for the end of the {@code Span}.
   * @since 0.1.0
   */
  public abstract void end(EndSpanOptions options);

  /**
   * Marks the end of {@code Span} execution with the default options.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * @since 0.1.0
   */
  public final void end() {
    end(EndSpanOptions.DEFAULT);
  }

  /**
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   * @since 0.1.0
   */
  public final SpanContext getContext() {
    return context;
  }

  /**
   * Returns the options associated with this {@code Span}.
   *
   * @return the options associated with this {@code Span}.
   * @since 0.1.0
   */
  public final Set<Options> getOptions() {
    return options;
  }

  /**
   * Type of span. Can be used to specify additional relationships between spans in addition to a
   * parent/child relationship.
   *
   * @since 0.1.0
   */
  public enum Kind {
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
    CLIENT
  }
}
