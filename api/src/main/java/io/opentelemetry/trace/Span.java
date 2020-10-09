/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.context.Context;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An interface that represents a span. It has an associated {@link SpanContext}.
 *
 * <p>Spans are created by the {@link Builder#startSpan} method.
 *
 * <p>{@code Span} <b>must</b> be ended by calling {@link #end()}.
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface Span {

  /**
   * Type of span. Can be used to specify additional relationships between spans in addition to a
   * parent/child relationship.
   *
   * @since 0.1.0
   */
  enum Kind {
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
     * Indicates that the span describes consumer receiving a message from a broker. Unlike client
     * and server, there is no direct critical path latency relationship between producer and
     * consumer spans.
     *
     * @since 0.1.0
     */
    CONSUMER
  }

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * <p>If a null or empty String {@code value} is passed in, the behavior is undefined, and hence
   * strongly discouraged.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  void setAttribute(String key, @Nonnull String value);

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
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
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
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
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
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
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  <T> void setAttribute(AttributeKey<T> key, @Nonnull T value);

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @since 0.1.0
   */
  default void setAttribute(AttributeKey<Long> key, int value) {
    setAttribute(key, (long) value);
  }

  /**
   * Adds an event to the {@link Span}. The timestamp of the event will be the current time.
   *
   * @param name the name of the event.
   * @since 0.1.0
   */
  void addEvent(String name);

  /**
   * Adds an event to the {@link Span} with the given {@code timestamp}, as nanos since epoch. Note,
   * this {@code timestamp} is not the same as {@link System#nanoTime()} but may be computed using
   * it, for example, by taking a difference of readings from {@link System#nanoTime()} and adding
   * to the span start time.
   *
   * <p>When possible, it is preferred to use {@link #addEvent(String)} at the time the event
   * occurred.
   *
   * @param name the name of the event.
   * @param timestamp the explicit event timestamp in nanos since epoch.
   * @since 0.1.0
   */
  void addEvent(String name, long timestamp);

  /**
   * Adds an event to the {@link Span} with the given {@link Attributes}. The timestamp of the event
   * will be the current time.
   *
   * @param name the name of the event.
   * @param attributes the attributes that will be added; these are associated with this event, not
   *     the {@code Span} as for {@code setAttribute()}.
   * @since 0.1.0
   */
  void addEvent(String name, Attributes attributes);

  /**
   * Adds an event to the {@link Span} with the given {@link Attributes} and {@code timestamp}.
   * Note, this {@code timestamp} is not the same as {@link System#nanoTime()} but may be computed
   * using it, for example, by taking a difference of readings from {@link System#nanoTime()} and
   * adding to the span start time.
   *
   * <p>When possible, it is preferred to use {@link #addEvent(String)} at the time the event
   * occurred.
   *
   * @param name the name of the event.
   * @param attributes the attributes that will be added; these are associated with this event, not
   *     the {@code Span} as for {@code setAttribute()}.
   * @param timestamp the explicit event timestamp in nanos since epoch.
   * @since 0.1.0
   */
  void addEvent(String name, Attributes attributes, long timestamp);

  /**
   * Sets the status to the {@code Span}.
   *
   * <p>If used, this will override the default {@code Span} status. Default status code is {@link
   * StatusCanonicalCode#UNSET}.
   *
   * <p>Only the value of the last call will be recorded, and implementations are free to ignore
   * previous calls.
   *
   * @param canonicalCode the {@link StatusCanonicalCode} to set.
   * @since 0.9.0
   */
  void setStatus(StatusCanonicalCode canonicalCode);

  /**
   * Sets the status to the {@code Span}.
   *
   * <p>If used, this will override the default {@code Span} status. Default status code is {@link
   * StatusCanonicalCode#UNSET}.
   *
   * <p>Only the value of the last call will be recorded, and implementations are free to ignore
   * previous calls.
   *
   * @param canonicalCode the {@link StatusCanonicalCode} to set.
   * @param description the description of the {@code Status}.
   * @since 0.9.0
   */
  void setStatus(StatusCanonicalCode canonicalCode, String description);

  /**
   * Records information about the {@link Throwable} to the {@link Span}.
   *
   * <p>Note that {@link io.opentelemetry.trace.attributes.SemanticAttributes#EXCEPTION_ESCAPED}
   * cannot be determined by this function. You should record this attribute manually using {@link
   * #recordException(Throwable, Attributes)} if you know that an exception is escaping.
   *
   * @param exception the {@link Throwable} to record.
   * @since 0.7.0
   */
  void recordException(Throwable exception);

  /**
   * Records information about the {@link Throwable} to the {@link Span}.
   *
   * @param exception the {@link Throwable} to record.
   * @param additionalAttributes the additional {@link Attributes} to record.
   * @since 0.8.0
   */
  void recordException(Throwable exception, Attributes additionalAttributes);

  /**
   * Updates the {@code Span} name.
   *
   * <p>If used, this will override the name provided via {@code Span.Builder}.
   *
   * <p>Upon this update, any sampling behavior based on {@code Span} name will depend on the
   * implementation.
   *
   * @param name the {@code Span} name.
   * @since 0.1
   */
  void updateName(String name);

  /**
   * Marks the end of {@code Span} execution.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * @since 0.1.0
   */
  void end();

  /**
   * Marks the end of {@code Span} execution with the specified {@link EndSpanOptions}.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * <p>Use this method for specifying explicit end options, such as end {@code Timestamp}. When no
   * explicit values are required, use {@link #end()}.
   *
   * @param endOptions the explicit {@link EndSpanOptions} for this {@code Span}.
   * @since 0.1.0
   */
  void end(EndSpanOptions endOptions);

  /**
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   * @since 0.1.0
   */
  SpanContext getContext();

  /**
   * Returns {@code true} if this {@code Span} records tracing events (e.g. {@link
   * #addEvent(String)}, {@link #setAttribute(String, long)}).
   *
   * @return {@code true} if this {@code Span} records tracing events.
   * @since 0.1.0
   */
  boolean isRecording();

  /**
   * {@link Builder} is used to construct {@link Span} instances which define arbitrary scopes of
   * code that are sampled for distributed tracing as a single atomic unit.
   *
   * <p>This is a simple example where all the work is being done within a single scope and a single
   * thread and the Context is automatically propagated:
   *
   * <pre>{@code
   * class MyClass {
   *   private static final Tracer tracer = OpenTelemetry.getTracer();
   *   void doWork {
   *     // Create a Span as a child of the current Span.
   *     Span span = tracer.spanBuilder("MyChildSpan").startSpan();
   *     try (Scope ss = tracer.withSpan(span)) {
   *       tracer.getCurrentSpan().addEvent("my event");
   *       doSomeWork();  // Here the new span is in the current Context, so it can be used
   *                      // implicitly anywhere down the stack.
   *     } finally {
   *       span.end();
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p>There might be cases where you do not perform all the work inside one static scope and the
   * Context is automatically propagated:
   *
   * <pre>{@code
   * class MyRpcServerInterceptorListener implements RpcServerInterceptor.Listener {
   *   private static final Tracer tracer = OpenTelemetry.getTracer();
   *   private Span mySpan;
   *
   *   public MyRpcInterceptor() {}
   *
   *   public void onRequest(String rpcName, Metadata metadata) {
   *     // Create a Span as a child of the remote Span.
   *     mySpan = tracer.spanBuilder(rpcName)
   *         .setParent(getTraceContextFromMetadata(metadata)).startSpan();
   *   }
   *
   *   public void onExecuteHandler(ServerCallHandler serverCallHandler) {
   *     try (Scope ws = tracer.withSpan(mySpan)) {
   *       tracer.getCurrentSpan().addEvent("Start rpc execution.");
   *       serverCallHandler.run();  // Here the new span is in the current Context, so it can be
   *                                 // used implicitly anywhere down the stack.
   *     }
   *   }
   *
   *   // Called when the RPC is canceled and guaranteed onComplete will not be called.
   *   public void onCancel() {
   *     // IMPORTANT: DO NOT forget to ended the Span here as the work is done.
   *     mySpan.setStatus(Status.CANCELLED);
   *     mySpan.end();
   *   }
   *
   *   // Called when the RPC is done and guaranteed onCancel will not be called.
   *   public void onComplete(RpcStatus rpcStatus) {
   *     // IMPORTANT: DO NOT forget to ended the Span here as the work is done.
   *     mySpan.setStatus(rpcStatusToCanonicalTraceStatus(status);
   *     mySpan.end();
   *   }
   * }
   * }</pre>
   *
   * <p>This is a simple example where all the work is being done within a single scope and the
   * Context is manually propagated:
   *
   * <pre>{@code
   * class MyClass {
   *   private static final Tracer tracer = OpenTelemetry.getTracer();
   *   void DoWork(Span parent) {
   *     Span childSpan = tracer.spanBuilder("MyChildSpan")
   *         .setParent(parent).startSpan();
   *     childSpan.addEvent("my event");
   *     try {
   *       doSomeWork(childSpan); // Manually propagate the new span down the stack.
   *     } finally {
   *       // To make sure we end the span even in case of an exception.
   *       childSpan.end();  // Manually end the span.
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p>If your Java version is less than Java SE 7, see {@link Builder#startSpan} for usage
   * examples.
   *
   * @since 0.1.0
   */
  interface Builder {

    /**
     * Sets the parent to use from the specified {@code Context}. If not set, the value of {@code
     * Tracer.getCurrentSpan()} at {@link #startSpan()} time will be used as parent.
     *
     * <p>If no {@link Span} is available in the specified {@code Context}, the resulting {@code
     * Span} will become a root instance, as if {@link #setNoParent()} had been called.
     *
     * <p>If called multiple times, only the last specified value will be used. Observe that the
     * state defined by a previous call to {@link #setNoParent()} will be discarded.
     *
     * @param context the {@code Context}.
     * @return this.
     * @throws NullPointerException if {@code context} is {@code null}.
     * @since 0.7.0
     */
    Builder setParent(Context context);

    /**
     * Sets the option to become a root {@code Span} for a new trace. If not set, the value of
     * {@code Tracer.getCurrentSpan()} at {@link #startSpan()} time will be used as parent.
     *
     * <p>Observe that any previously set parent will be discarded.
     *
     * @return this.
     * @since 0.1.0
     */
    Builder setNoParent();

    /**
     * Adds a link to the newly created {@code Span}.
     *
     * <p>Links are used to link {@link Span}s in different traces. Used (for example) in batching
     * operations, where a single batch handler processes multiple requests from different traces or
     * the same trace.
     *
     * @param spanContext the context of the linked {@code Span}.
     * @return this.
     * @throws NullPointerException if {@code spanContext} is {@code null}.
     * @since 0.1.0
     */
    Builder addLink(SpanContext spanContext);

    /**
     * Adds a link to the newly created {@code Span}.
     *
     * <p>Links are used to link {@link Span}s in different traces. Used (for example) in batching
     * operations, where a single batch handler processes multiple requests from different traces or
     * the same trace.
     *
     * @param spanContext the context of the linked {@code Span}.
     * @param attributes the attributes of the {@code Link}.
     * @return this.
     * @throws NullPointerException if {@code spanContext} is {@code null}.
     * @throws NullPointerException if {@code attributes} is {@code null}.
     * @since 0.1.0
     */
    Builder addLink(SpanContext spanContext, Attributes attributes);

    /**
     * Sets an attribute to the newly created {@code Span}. If {@code Span.Builder} previously
     * contained a mapping for the key, the old value is replaced by the specified value.
     *
     * <p>If a null or empty String {@code value} is passed in, the behavior is undefined, and hence
     * strongly discouraged.
     *
     * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @param key the key for this attribute.
     * @param value the value for this attribute.
     * @return this.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @since 0.3.0
     */
    Builder setAttribute(String key, @Nonnull String value);

    /**
     * Sets an attribute to the newly created {@code Span}. If {@code Span.Builder} previously
     * contained a mapping for the key, the old value is replaced by the specified value.
     *
     * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @param key the key for this attribute.
     * @param value the value for this attribute.
     * @return this.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @since 0.3.0
     */
    Builder setAttribute(String key, long value);

    /**
     * Sets an attribute to the newly created {@code Span}. If {@code Span.Builder} previously
     * contained a mapping for the key, the old value is replaced by the specified value.
     *
     * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @param key the key for this attribute.
     * @param value the value for this attribute.
     * @return this.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @since 0.3.0
     */
    Builder setAttribute(String key, double value);

    /**
     * Sets an attribute to the newly created {@code Span}. If {@code Span.Builder} previously
     * contained a mapping for the key, the old value is replaced by the specified value.
     *
     * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
     * pre-allocate your keys, if possible.
     *
     * @param key the key for this attribute.
     * @param value the value for this attribute.
     * @return this.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @since 0.3.0
     */
    Builder setAttribute(String key, boolean value);

    /**
     * Sets an attribute to the newly created {@code Span}. If {@code Span.Builder} previously
     * contained a mapping for the key, the old value is replaced by the specified value.
     *
     * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
     *
     * @param key the key for this attribute.
     * @param value the value for this attribute.
     * @return this.
     * @throws NullPointerException if {@code key} is {@code null}.
     * @throws NullPointerException if {@code value} is {@code null}.
     * @since 0.3.0
     */
    <T> Builder setAttribute(AttributeKey<T> key, @Nonnull T value);

    /**
     * Sets the {@link Span.Kind} for the newly created {@code Span}. If not called, the
     * implementation will provide a default value {@link Span.Kind#INTERNAL}.
     *
     * @param spanKind the kind of the newly created {@code Span}.
     * @return this.
     * @since 0.1.0
     */
    Builder setSpanKind(Span.Kind spanKind);

    /**
     * Sets an explicit start timestamp for the newly created {@code Span}.
     *
     * <p>Use this method to specify an explicit start timestamp. If not called, the implementation
     * will use the timestamp value at {@link #startSpan()} time, which should be the default case.
     *
     * <p>Important this is NOT equivalent with System.nanoTime().
     *
     * @param startTimestamp the explicit start timestamp of the newly created {@code Span} in nanos
     *     since epoch.
     * @return this.
     * @since 0.1.0
     */
    Builder setStartTimestamp(long startTimestamp);

    /**
     * Starts a new {@link Span}.
     *
     * <p>Users <b>must</b> manually call {@link Span#end()} to end this {@code Span}.
     *
     * <p>Does not install the newly created {@code Span} to the current Context.
     *
     * <p>IMPORTANT: This method can be called only once per {@link Builder} instance and as the
     * last method called. After this method is called calling any method is undefined behavior.
     *
     * <p>Example of usage:
     *
     * <pre>{@code
     * class MyClass {
     *   private static final Tracer tracer = OpenTelemetry.getTracer();
     *   void DoWork(Span parent) {
     *     Span childSpan = tracer.spanBuilder("MyChildSpan")
     *          .setParent(parent)
     *          .startSpan();
     *     childSpan.addEvent("my event");
     *     try {
     *       doSomeWork(childSpan); // Manually propagate the new span down the stack.
     *     } finally {
     *       // To make sure we end the span even in case of an exception.
     *       childSpan.end();  // Manually end the span.
     *     }
     *   }
     * }
     * }</pre>
     *
     * @return the newly created {@code Span}.
     * @since 0.1.0
     */
    Span startSpan();
  }
}
