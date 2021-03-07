/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * {@link SpanBuilder} is used to construct {@link Span} instances which define arbitrary scopes of
 * code that are sampled for distributed tracing as a single atomic unit.
 *
 * <p>This is a simple example where all the work is being done within a single scope and a single
 * thread and the Context is automatically propagated:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = OpenTelemetry.get().getTracer("com.anyco.rpc");
 *
 *   void doWork {
 *     // Create a Span as a child of the current Span.
 *     Span span = tracer.spanBuilder("MyChildSpan").startSpan();
 *     try (Scope ss = span.makeCurrent()) {
 *       span.addEvent("my event");
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
 *   private static final Tracer tracer = OpenTelemetry.get().getTracer("com.example.rpc");
 *   private Span mySpan;
 *
 *   public MyRpcInterceptor() {}
 *
 *   public void onRequest(String rpcName, Metadata metadata) {
 *     // Create a Span as a child of the remote Span.
 *     mySpan = tracer.spanBuilder(rpcName)
 *         .setParent(extractContextFromMetadata(metadata)).startSpan();
 *   }
 *
 *   public void onExecuteHandler(ServerCallHandler serverCallHandler) {
 *     try (Scope ws = mySpan.makeCurrent()) {
 *       Span.current().addEvent("Start rpc execution.");
 *       serverCallHandler.run();  // Here the new span is in the current Context, so it can be
 *                                 // used implicitly anywhere down the stack.
 *     }
 *   }
 *
 *   // Called when the RPC is canceled and guaranteed onComplete will not be called.
 *   public void onCancel() {
 *     // IMPORTANT: DO NOT forget to ended the Span here as the work is done.
 *     mySpan.setStatus(StatusCode.ERROR);
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
 *   private static final Tracer tracer = OpenTelemetry.get().getTracer("com.example.rpc");
 *
 *   void doWork(Span parent) {
 *     Span childSpan = tracer.spanBuilder("MyChildSpan")
 *         .setParent(Context.current().with(parent))
 *         .startSpan();
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
 * <p>see {@link SpanBuilder#startSpan} for usage examples.
 */
public interface SpanBuilder {

  /**
   * Sets the parent to use from the specified {@code Context}. If not set, the value of {@code
   * Span.current()} at {@link #startSpan()} time will be used as parent.
   *
   * <p>If no {@link Span} is available in the specified {@code Context}, the resulting {@code Span}
   * will become a root instance, as if {@link #setNoParent()} had been called.
   *
   * <p>If called multiple times, only the last specified value will be used. Observe that the state
   * defined by a previous call to {@link #setNoParent()} will be discarded.
   *
   * @param context the {@code Context}.
   * @return this.
   */
  SpanBuilder setParent(Context context);

  /**
   * Sets the option to become a root {@code Span} for a new trace. If not set, the value of {@code
   * Span.current()} at {@link #startSpan()} time will be used as parent.
   *
   * <p>Observe that any previously set parent will be discarded.
   *
   * @return this.
   */
  SpanBuilder setNoParent();

  /**
   * Adds a link to the newly created {@code Span}.
   *
   * <p>Links are used to link {@link Span}s in different traces. Used (for example) in batching
   * operations, where a single batch handler processes multiple requests from different traces or
   * the same trace.
   *
   * <p>Implementations may ignore calls with an {@linkplain SpanContext#isValid() invalid span
   * context}.
   *
   * @param spanContext the context of the linked {@code Span}.
   * @return this.
   */
  SpanBuilder addLink(SpanContext spanContext);

  /**
   * Adds a link to the newly created {@code Span}.
   *
   * <p>Links are used to link {@link Span}s in different traces. Used (for example) in batching
   * operations, where a single batch handler processes multiple requests from different traces or
   * the same trace.
   *
   * <p>Implementations may ignore calls with an {@linkplain SpanContext#isValid() invalid span
   * context}.
   *
   * @param spanContext the context of the linked {@code Span}.
   * @param attributes the attributes of the {@code Link}.
   * @return this.
   */
  SpanBuilder addLink(SpanContext spanContext, Attributes attributes);

  /**
   * Sets an attribute to the newly created {@code Span}. If {@code SpanBuilder} previously
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
   */
  SpanBuilder setAttribute(String key, String value);

  /**
   * Sets an attribute to the newly created {@code Span}. If {@code SpanBuilder} previously
   * contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  SpanBuilder setAttribute(String key, long value);

  /**
   * Sets an attribute to the newly created {@code Span}. If {@code SpanBuilder} previously
   * contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  SpanBuilder setAttribute(String key, double value);

  /**
   * Sets an attribute to the newly created {@code Span}. If {@code SpanBuilder} previously
   * contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  SpanBuilder setAttribute(String key, boolean value);

  /**
   * Sets an attribute to the newly created {@code Span}. If {@code SpanBuilder} previously
   * contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  <T> SpanBuilder setAttribute(AttributeKey<T> key, T value);

  /**
   * Sets the {@link SpanKind} for the newly created {@code Span}. If not called, the implementation
   * will provide a default value {@link SpanKind#INTERNAL}.
   *
   * @param spanKind the kind of the newly created {@code Span}.
   * @return this.
   */
  SpanBuilder setSpanKind(SpanKind spanKind);

  /**
   * Sets an explicit start timestamp for the newly created {@code Span}.
   *
   * <p>LIRInstruction.Use this method to specify an explicit start timestamp. If not called, the
   * implementation will use the timestamp value at {@link #startSpan()} time, which should be the
   * default case.
   *
   * <p>Important this is NOT equivalent with System.nanoTime().
   *
   * @param startTimestamp the explicit start timestamp from the epoch of the newly created {@code
   *     Span}.
   * @param unit the unit of the timestamp.
   * @return this.
   */
  SpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit);

  /**
   * Sets an explicit start timestamp for the newly created {@code Span}.
   *
   * <p>Use this method to specify an explicit start timestamp. If not called, the implementation
   * will use the timestamp value at {@link #startSpan()} time, which should be the default case.
   *
   * <p>Important this is NOT equivalent with System.nanoTime().
   *
   * @param startTimestamp the explicit start timestamp from the epoch of the newly created {@code
   *     Span}.
   * @return this.
   */
  default SpanBuilder setStartTimestamp(Instant startTimestamp) {
    if (startTimestamp == null) {
      return this;
    }
    return setStartTimestamp(
        SECONDS.toNanos(startTimestamp.getEpochSecond()) + startTimestamp.getNano(), NANOSECONDS);
  }

  /**
   * Starts a new {@link Span}.
   *
   * <p>Users <b>must</b> manually call {@link Span#end()} to end this {@code Span}.
   *
   * <p>Does not install the newly created {@code Span} to the current Context.
   *
   * <p>IMPORTANT: This method can be called only once per {@link SpanBuilder} instance and as the
   * last method called. After this method is called calling any method is undefined behavior.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * class MyClass {
   *   private static final Tracer tracer = OpenTelemetry.get().getTracer("com.example.rpc");
   *
   *   void doWork(Span parent) {
   *     Span childSpan = tracer.spanBuilder("MyChildSpan")
   *          .setParent(Context.current().with(parent))
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
   */
  Span startSpan();
}
