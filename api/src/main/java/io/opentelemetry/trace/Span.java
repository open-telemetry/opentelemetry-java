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

import java.util.Map;
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
   *     the {@code Span} as for {@code setAttribute()}.
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
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   * @since 0.1.0
   */
  SpanContext getContext();

  /**
   * Returns {@code true} if this {@code Span} records events (e.g. {@link #addEvent(String)}).
   *
   * @return {@code true} if this {@code Span} records events.
   * @since 0.1.0
   */
  boolean isRecordingEvents();

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
     * Sets the parent {@code Span} to use. If not set, the value of {@code Tracer.getCurrentSpan()}
     * at {@link #startSpan()} time will be used as parent.
     *
     * <p>This <b>must</b> be used to create a {@code Span} when manual Context propagation is used
     * OR when creating a root {@code Span} with a parent with an invalid {@link SpanContext}.
     *
     * <p>Observe this is the preferred method when the parent is a {@code Span} created within the
     * process. Using its {@code SpanContext} as parent remains as a valid, albeit inefficient,
     * operation.
     *
     * <p>If called multiple times, only the last specified value will be used. Observe that the
     * state defined by a previous call to {@link #setNoParent()} will be discarded.
     *
     * @param parent the {@code Span} used as parent.
     * @return this.
     * @throws NullPointerException if {@code parent} is {@code null}.
     * @see #setNoParent()
     * @since 0.1.0
     */
    Builder setParent(Span parent);

    /**
     * Sets the parent {@link SpanContext} to use. If not set, the value of {@code
     * Tracer.getCurrentSpan()} at {@link #startSpan()} time will be used as parent.
     *
     * <p>Similar to {@link #setParent(Span parent)} but this <b>must</b> be used to create a {@code
     * Span} when the parent is in a different process. This is only intended for use by RPC systems
     * or similar.
     *
     * <p>If no {@link SpanContext} is available, users must call {@link #setNoParent()} in order to
     * create a root {@code Span} for a new trace.
     *
     * <p>If called multiple times, only the last specified value will be used. Observe that the
     * state defined by a previous call to {@link #setNoParent()} will be discarded.
     *
     * @param remoteParent the {@link SpanContext} used as parent.
     * @return this.
     * @throws NullPointerException if {@code remoteParent} is {@code null}.
     * @see #setParent(Span parent)
     * @see #setNoParent()
     * @since 0.1.0
     */
    Builder setParent(SpanContext remoteParent);

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
     * Sets the {@link Sampler} to use. If not set, the implementation will provide a default.
     *
     * <p>Observe this is used only as a hint for the underlying implementation, which will decide
     * whether to sample or not this {@code Span}.
     *
     * @param sampler the {@code Sampler} to use when determining sampling for a {@code Span}.
     * @return this.
     * @since 0.1.0
     */
    Builder setSampler(Sampler sampler);

    /**
     * Adds a {@link Link} to the newly created {@code Span}.
     *
     * @param spanContext the context of the linked {@code Span}.
     * @return this.
     * @throws NullPointerException if {@code spanContext} is {@code null}.
     * @see #addLink(Link)
     * @since 0.1.0
     */
    Builder addLink(SpanContext spanContext);

    /**
     * Adds a {@link Link} to the newly created {@code Span}.
     *
     * @param spanContext the context of the linked {@code Span}.
     * @param attributes the attributes of the {@code Link}.
     * @return this.
     * @throws NullPointerException if {@code spanContext} is {@code null}.
     * @throws NullPointerException if {@code attributes} is {@code null}.
     * @see #addLink(Link)
     * @since 0.1.0
     */
    Builder addLink(SpanContext spanContext, Map<String, AttributeValue> attributes);

    /**
     * Adds a {@link Link} to the newly created {@code Span}.
     *
     * <p>Links are used to link {@link Span}s in different traces. Used (for example) in batching
     * operations, where a single batch handler processes multiple requests from different traces or
     * the same trace.
     *
     * @param link the {@link Link} to be added.
     * @return this.
     * @throws NullPointerException if {@code link} is {@code null}.
     * @since 0.1.0
     */
    Builder addLink(Link link);

    /**
     * Sets the option to record events even if not sampled for the newly created {@code Span}. If
     * not called, the implementation will provide a default.
     *
     * @param recordEvents new value determining if this {@code Span} should have events recorded.
     * @return this.
     * @since 0.1.0
     */
    Builder setRecordEvents(boolean recordEvents);

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
     * Starts a new {@link Span}.
     *
     * <p>Users <b>must</b> manually call {@link Span#end()} to end this {@code Span}.
     *
     * <p>Does not install the newly created {@code Span} to the current Context.
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
