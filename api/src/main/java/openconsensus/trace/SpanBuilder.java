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

import java.util.List;
import java.util.concurrent.Callable;
import openconsensus.trace.data.Status;

/**
 * {@link SpanBuilder} is used to construct {@link Span} instances which define arbitrary scopes of
 * code that are sampled for distributed tracing as a single atomic unit.
 *
 * <p>This is a simple example where all the work is being done within a single scope and a single
 * thread and the Context is automatically propagated:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = Tracing.getTracer();
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
 *   private static final Tracer tracer = Tracing.getTracer();
 *   private Span mySpan;
 *
 *   public MyRpcInterceptor() {}
 *
 *   public void onRequest(String rpcName, Metadata metadata) {
 *     // Create a Span as a child of the remote Span.
 *     mySpan = tracer.spanBuilderWithRemoteParent(
 *         getTraceContextFromMetadata(metadata), rpcName).startSpan();
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
 *   private static final Tracer tracer = Tracing.getTracer();
 *   void DoWork(Span parent) {
 *     Span childSpan = tracer.spanBuilderWithExplicitParent("MyChildSpan", parent).startSpan();
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
 * <p>If your Java version is less than Java SE 7, see {@link SpanBuilder#startSpan} for usage
 * examples.
 *
 * @since 0.1.0
 */
public abstract class SpanBuilder {

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
  public abstract SpanBuilder setSampler(Sampler sampler);

  /**
   * Sets the {@code List} of parent links. Links are used to link {@link Span}s in different
   * traces. Used (for example) in batching operations, where a single batch handler processes
   * multiple requests from different traces.
   *
   * @param parentLinks new links to be added.
   * @return this.
   * @throws NullPointerException if {@code parentLinks} is {@code null}.
   * @since 0.1.0
   */
  public abstract SpanBuilder setParentLinks(List<Span> parentLinks);

  /**
   * Sets the option to record events even if not sampled for the newly created {@code Span}. If not
   * called, the implementation will provide a default.
   *
   * @param recordEvents new value determining if this {@code Span} should have events recorded.
   * @return this.
   * @since 0.1.0
   */
  public abstract SpanBuilder setRecordEvents(boolean recordEvents);

  /**
   * Sets the {@link Span.Kind} for the newly created {@code Span}. If not called, the
   * implementation will provide a default value {@link Span.Kind#INTERNAL}.
   *
   * @param spanKind the kind of the newly created {@code Span}.
   * @return this.
   * @since 0.1.0
   */
  public SpanBuilder setSpanKind(Span.Kind spanKind) {
    return this;
  }

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
   *   private static final Tracer tracer = Tracing.getTracer();
   *   void DoWork(Span parent) {
   *     Span childSpan = tracer.spanBuilderWithExplicitParent("MyChildSpan", parent).startSpan();
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
  public abstract Span startSpan();

  /**
   * Starts a new span and runs the given {@code Runnable} with the newly created {@code Span} as
   * the current {@code Span}, and ends the {@code Span} after the {@code Runnable} is run.
   *
   * <p>Any error will end up as a {@link Status#UNKNOWN}.
   *
   * <pre><code>
   * tracer.spanBuilder("MyRunnableSpan").startSpanAndRun(myRunnable);
   * </code></pre>
   *
   * <p>It is equivalent with the following code:
   *
   * <pre><code>
   * Span span = tracer.spanBuilder("MyRunnableSpan").startSpan();
   * Runnable newRunnable = tracer.withSpan(span, myRunnable);
   * try {
   *   newRunnable.run();
   * } finally {
   *   span.end();
   * }
   * </code></pre>
   *
   * @param runnable the {@code Runnable} to run in the {@code Span}.
   * @since 0.1.0
   */
  public abstract void startSpanAndRun(final Runnable runnable);

  /**
   * Starts a new span and calls the given {@code Callable} with the newly created {@code Span} as
   * the current {@code Span}, and ends the {@code Span} after the {@code Callable} is called.
   *
   * <p>Any error will end up as a {@link Status#UNKNOWN}.
   *
   * <pre><code>
   * MyResult myResult = tracer.spanBuilder("MyCallableSpan").startSpanAndCall(myCallable);
   * </code></pre>
   *
   * <p>It is equivalent with the following code:
   *
   * <pre><code>
   * Span span = tracer.spanBuilder("MyCallableSpan").startSpan();
   * {@code Callable<MyResult>} newCallable = tracer.withSpan(span, myCallable);
   * MyResult myResult = null;
   * try {
   *   myResult = newCallable.call();
   * } finally {
   *   span.end();
   * }
   * );
   * </code></pre>
   *
   * @param callable the {@code Callable} to run in the {@code Span}.
   * @param <V> the result type of method call.
   * @return the result of the {@code Callable#call}.
   * @throws Exception if the {@code Callable} throws an exception.
   * @since 0.1.0
   */
  public abstract <V> V startSpanAndCall(Callable<V> callable) throws Exception;
}
