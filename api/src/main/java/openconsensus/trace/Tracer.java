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

import com.google.errorprone.annotations.MustBeClosed;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import openconsensus.context.Scope;
import openconsensus.context.propagation.BinaryFormat;
import openconsensus.context.propagation.HttpTextFormat;
import openconsensus.resource.Resource;

/**
 * Tracer is a simple, interface for {@link Span} creation and in-process context interaction.
 *
 * <p>Users may choose to use manual or automatic Context propagation. Because of that this class
 * offers APIs to facilitate both usages.
 *
 * <p>The automatic context propagation is done using {@link io.grpc.Context} which is a gRPC
 * independent implementation for in-process Context propagation mechanism which can carry
 * scoped-values across API boundaries and between threads. Users of the library must propagate the
 * {@link io.grpc.Context} between different threads.
 *
 * <p>Example usage with automatic context propagation:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = Trace.getTracer();
 *   void doWork() {
 *     Span span = tracer.spanBuilder("MyClass.DoWork").startSpan();
 *     try(Scope ss = tracer.withSpan(span)) {
 *       tracer.getCurrentSpan().addEvent("Starting the work.");
 *       doWorkInternal();
 *       tracer.getCurrentSpan().addEvent("Finished working.");
 *     } finally {
 *       span.end();
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Example usage with manual context propagation:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = Trace.getTracer();
 *   void doWork(Span parent) {
 *     Span childSpan = tracer.spanBuilderWithExplicitParent("MyChildSpan", parent).startSpan();
 *     childSpan.addEvent("Starting the work.");
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
 * @since 0.1.0
 */
public interface Tracer {
  /**
   * Gets the current Span from the current Context.
   *
   * <p>To install a {@link Span} to the current Context use {@link #withSpan(Span)}.
   *
   * <p>startSpan methods do NOT modify the current Context {@code Span}.
   *
   * @return a default {@code Span} that does nothing and has an invalid {@link SpanContext} if no
   *     {@code Span} is associated with the current Context, otherwise the current {@code Span}
   *     from the Context.
   * @since 0.1.0
   */
  Span getCurrentSpan();

  /**
   * Enters the scope of code where the given {@link Span} is in the current Context, and returns an
   * object that represents that scope. The scope is exited when the returned object is closed.
   *
   * <p>Supports try-with-resource idiom.
   *
   * <p>Can be called with {@link BlankSpan} to enter a scope of code where tracing is stopped.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * private static Tracer tracer = Trace.getTracer();
   * void doWork() {
   *   // Create a Span as a child of the current Span.
   *   Span span = tracer.spanBuilder("my span").startSpan();
   *   try (Scope ws = tracer.withSpan(span)) {
   *     tracer.getCurrentSpan().addEvent("my event");
   *     doSomeOtherWork();  // Here "span" is the current Span.
   *   }
   *   span.end();
   * }
   * }</pre>
   *
   * <p>Prior to Java SE 7, you can use a finally block to ensure that a resource is closed
   * regardless of whether the try statement completes normally or abruptly.
   *
   * <p>Example of usage prior to Java SE7:
   *
   * <pre>{@code
   * private static Tracer tracer = Trace.getTracer();
   * void doWork() {
   *   // Create a Span as a child of the current Span.
   *   Span span = tracer.spanBuilder("my span").startSpan();
   *   Scope ws = tracer.withSpan(span);
   *   try {
   *     tracer.getCurrentSpan().addEvent("my event");
   *     doSomeOtherWork();  // Here "span" is the current Span.
   *   } finally {
   *     ws.close();
   *   }
   *   span.end();
   * }
   * }</pre>
   *
   * @param span The {@link Span} to be set to the current Context.
   * @return an object that defines a scope where the given {@link Span} will be set to the current
   *     Context.
   * @throws NullPointerException if {@code span} is {@code null}.
   * @since 0.1.0
   */
  @MustBeClosed
  Scope withSpan(Span span);

  /**
   * Returns a {@link Runnable} that runs the given task with the given {@code Span} in the current
   * context.
   *
   * <p>Users may consider to use {@link Span.Builder#startSpanAndRun(Runnable)}.
   *
   * <p>Any error will end up as a {@link Status#UNKNOWN}.
   *
   * <p>IMPORTANT: Caller must manually propagate the entire {@code io.grpc.Context} when wraps a
   * {@code Runnable}, see the examples.
   *
   * <p>IMPORTANT: Caller must manually end the {@code Span} within the {@code Runnable}, or after
   * the {@code Runnable} is executed.
   *
   * <p>Example with Executor wrapped with {@link io.grpc.Context#currentContextExecutor}:
   *
   * <pre><code>
   * class MyClass {
   *   private static Tracer tracer = Trace.getTracer();
   *   void handleRequest(Executor executor) {
   *     Span span = tracer.spanBuilder("MyRunnableSpan").startSpan();
   *     executor.execute(tracer.withSpan(span, new Runnable() {
   *      {@literal @}Override
   *       public void run() {
   *         try {
   *           sendResult();
   *         } finally {
   *           span.end();
   *         }
   *       }
   *     }));
   *   }
   * }
   * </code></pre>
   *
   * <p>Example without Executor wrapped with {@link io.grpc.Context#currentContextExecutor}:
   *
   * <pre><code>
   * class MyClass {
   *   private static Tracer tracer = Trace.getTracer();
   *   void handleRequest(Executor executor) {
   *     Span span = tracer.spanBuilder("MyRunnableSpan").startSpan();
   *     executor.execute(Context.wrap(tracer.withSpan(span, new Runnable() {
   *      {@literal @}Override
   *       public void run() {
   *         try {
   *           sendResult();
   *         } finally {
   *           span.end();
   *         }
   *       }
   *     })));
   *   }
   * }
   * </code></pre>
   *
   * @param span the {@code Span} to be set as current.
   * @param runnable the {@code Runnable} to withSpan in the {@code Span}.
   * @return the {@code Runnable}.
   * @since 0.1.0
   */
  Runnable withSpan(Span span, Runnable runnable);

  /**
   * Returns a {@link Callable} that runs the given task with the given {@code Span} in the current
   * context.
   *
   * <p>Users may consider to use {@link Span.Builder#startSpanAndCall(Callable)}.
   *
   * <p>Any error will end up as a {@link Status#UNKNOWN}.
   *
   * <p>IMPORTANT: Caller must manually propagate the entire {@code io.grpc.Context} when wraps a
   * {@code Callable}, see the examples.
   *
   * <p>IMPORTANT: Caller must manually end the {@code Span} within the {@code Callable}, or after
   * the {@code Callable} is executed.
   *
   * <p>Example with Executor wrapped with {@link io.grpc.Context#currentContextExecutor}:
   *
   * <pre><code>
   * class MyClass {
   *   private static Tracer tracer = Trace.getTracer();
   *   void handleRequest(Executor executor) {
   *     Span span = tracer.spanBuilder("MyRunnableSpan").startSpan();
   *     executor.execute(tracer.withSpan(span, new Callable&lt;MyResult&gt;() {
   *      {@literal @}Override
   *       public MyResult call() throws Exception {
   *         try {
   *           return sendResult();
   *         } finally {
   *           span.end();
   *         }
   *       }
   *     }));
   *   }
   * }
   * </code></pre>
   *
   * <p>Example without Executor wrapped with {@link io.grpc.Context#currentContextExecutor}:
   *
   * <pre><code>
   * class MyClass {
   *   private static Tracer tracer = Trace.getTracer();
   *   void handleRequest(Executor executor) {
   *     Span span = tracer.spanBuilder("MyRunnableSpan").startSpan();
   *     executor.execute(Context.wrap(tracer.withSpan(span, new Callable&lt;MyResult&gt;() {
   *      {@literal @}Override
   *       public MyResult call() throws Exception {
   *         try {
   *           return sendResult();
   *         } finally {
   *           span.end();
   *         }
   *       }
   *     })));
   *   }
   * }
   * </code></pre>
   *
   * @param span the {@code Span} to be set as current.
   * @param callable the {@code Callable} to run in the {@code Span}.
   * @param <V> the result type of method call.
   * @return the {@code Callable}.
   * @since 0.1.0
   */
  <V> Callable<V> withSpan(Span span, final Callable<V> callable);

  /**
   * Returns a {@link Span.Builder} to create and start a new child {@link Span} as a child of to
   * the current {@code Span} if any, otherwise creates a root {@code Span}.
   *
   * <p>See {@link Span.Builder} for usage examples.
   *
   * <p>This <b>must</b> be used to create a {@code Span} when automatic Context propagation is
   * used.
   *
   * <p>This is equivalent with:
   *
   * <pre>{@code
   * tracer.spanBuilderWithExplicitParent("MySpanName",tracer.getCurrentSpan());
   * }</pre>
   *
   * @param spanName The name of the returned Span.
   * @return a {@code Span.Builder} to create and start a new {@code Span}.
   * @throws NullPointerException if {@code spanName} is {@code null}.
   * @since 0.1.0
   */
  Span.Builder spanBuilder(String spanName);

  /**
   * Returns a {@link Span.Builder} to create and start a new child {@link Span} (or root if parent
   * is {@code null} or has an invalid {@link SpanContext}), with parent being the designated {@code
   * Span}.
   *
   * <p>See {@link Span.Builder} for usage examples.
   *
   * <p>This <b>must</b> be used to create a {@code Span} when manual Context propagation is used OR
   * when creating a root {@code Span} with a {@code null} parent.
   *
   * @param spanName The name of the returned Span.
   * @param parent The parent of the returned Span. If {@code null} the {@code Span.Builder} will
   *     build a root {@code Span}.
   * @return a {@code Span.Builder} to create and start a new {@code Span}.
   * @throws NullPointerException if {@code spanName} is {@code null}.
   * @since 0.1.0
   */
  Span.Builder spanBuilderWithExplicitParent(String spanName, @Nullable Span parent);

  /**
   * Returns a {@link Span.Builder} to create and start a new child {@link Span} (or root if parent
   * is {@code null}), with parent being the remote {@link Span} designated by the {@link
   * SpanContext}.
   *
   * <p>See {@link Span.Builder} for usage examples.
   *
   * <p>This <b>must</b> be used to create a {@code Span} when the parent is in a different process.
   * This is only intended for use by RPC systems or similar.
   *
   * <p>If no {@link SpanContext} OR fail to parse the {@link SpanContext} on the server side, users
   * must call this method with a {@code null} remote parent {@code SpanContext}.
   *
   * @param spanName The name of the returned Span.
   * @param remoteParentSpanContext The remote parent of the returned Span.
   * @return a {@code Span.Builder} to create and start a new {@code Span}.
   * @throws NullPointerException if {@code spanName} is {@code null}.
   * @since 0.1.0
   */
  Span.Builder spanBuilderWithRemoteParent(
      String spanName, @Nullable SpanContext remoteParentSpanContext);

  /**
   * Sets the {@link Resource} to be associated with all {@link Span} and {@link SpanData} objects
   * recorded by this {@link Tracer}.
   *
   * @param resource Resource to be associated with all {@link Span} and {@link SpanData} objects.
   */
  void setResource(Resource resource);

  /**
   * Gets the {@link Resource} that is associating with all the {@link Span} and {@link SpanData}
   * objects recorded by this {@link Tracer}.
   *
   * @return {@link Resource} that is associating with all {@link Span} and {@link SpanData}
   *     objects.
   */
  Resource getResource();

  /**
   * Records a {@link SpanData}. This API allows to send a pre-populated span object to the
   * exporter. Sampling and recording decisions as well as other collection optimizations is a
   * responsibility of a caller. Note, the {@link SpanContext} object on the span population with
   * the values that will allow correlation of telemetry is also a caller responsibility.
   *
   * @param span Span Data to be reported to all exporters.
   */
  void recordSpanData(SpanData span);

  /**
   * Returns the {@link BinaryFormat} for this implementation.
   *
   * <p>If no implementation is provided then no-op implementation will be used.
   *
   * <p>Usually this will be the W3C Trace Context as the binary format. For more details, see <a
   * href="https://github.com/w3c/trace-context">trace-context</a>.
   *
   * <p>Example of usage on the client:
   *
   * <pre>{@code
   * private static final Tracer tracer = Trace.getTracer();
   * private static final BinaryFormat binaryFormat =
   *     Trace.getTracer().getBinaryFormat();
   * void onSendRequest() {
   *   Span span = tracer.spanBuilder("MyRequest").setSpanKind(Span.Kind.CLIENT).startSpan();
   *   try (Scope ss = tracer.withSpan(span)) {
   *     byte[] binaryValue = binaryFormat.toByteArray(tracer.getCurrentContext().context());
   *     // Send the request including the binaryValue and wait for the response.
   *   } finally {
   *     span.end();
   *   }
   * }
   * }</pre>
   *
   * <p>Example of usage on the server:
   *
   * <pre>{@code
   * private static final Tracer tracer = Trace.getTracer();
   * private static final BinaryFormat binaryFormat =
   *     Trace.getTracer().getBinaryFormat();
   * void onRequestReceived() {
   *   // Get the binaryValue from the request.
   *   SpanContext spanContext = SpanContext.INVALID;
   *   if (binaryValue != null) {
   *     spanContext = binaryFormat.fromByteArray(binaryValue);
   *   }
   *   Span span = tracer.spanBuilderWithRemoteParent("MyRequest", spanContext)
   *       .setSpanKind(Span.Kind.SERVER).startSpan();
   *   try (Scope ss = tracer.withSpan(span)) {
   *     // Handle request and send response back.
   *   } finally {
   *     span.end();
   *   }
   * }
   * }</pre>
   *
   * @return the {@code BinaryFormat} for this implementation.
   * @since 0.1.0
   */
  BinaryFormat<SpanContext> getBinaryFormat();

  /**
   * Returns the {@link HttpTextFormat} for this implementation.
   *
   * <p>If no implementation is provided then no-op implementation will be used.
   *
   * <p>Usually this will be the W3C Trace Context as the HTTP text format. For more details, see <a
   * href="https://github.com/w3c/trace-context">trace-context</a>.
   *
   * <p>Example of usage on the client:
   *
   * <pre>{@code
   * private static final Tracer tracer = Trace.getTracer();
   * private static final HttpTextFormat textFormat = Trace.getTracer().getHttpTextFormat();
   * private static final HttpTextFormat.Setter setter =
   *         new HttpTextFormat.Setter<HttpURLConnection>() {
   *   public void put(HttpURLConnection carrier, String key, String value) {
   *     carrier.setRequestProperty(field, value);
   *   }
   * }
   *
   * void makeHttpRequest() {
   *   Span span = tracer.spanBuilder("MyRequest").setSpanKind(Span.Kind.CLIENT).startSpan();
   *   try (Scope s = tracer.withSpan(span)) {
   *     HttpURLConnection connection =
   *         (HttpURLConnection) new URL("http://myserver").openConnection();
   *     textFormat.inject(span.getContext(), connection, httpURLConnectionSetter);
   *     // Send the request, wait for response and maybe set the status if not ok.
   *   }
   *   span.end();  // Can set a status.
   * }
   * }</pre>
   *
   * <p>Example of usage on the server:
   *
   * <pre>{@code
   * private static final Tracer tracer = Trace.getTracer();
   * private static final HttpTextFormat textFormat = Trace.getTracer().getHttpTextFormat();
   * private static final HttpTextFormat.Getter<HttpRequest> getter = ...;
   *
   * void onRequestReceived(HttpRequest request) {
   *   SpanContext spanContext = textFormat.extract(request, getter);
   *   Span span = tracer.spanBuilderWithRemoteParent("MyRequest", spanContext)
   *       .setSpanKind(Span.Kind.SERVER).startSpan();
   *   try (Scope s = tracer.withSpan(span)) {
   *     // Handle request and send response back.
   *   }
   *   span.end()
   * }
   * }</pre>
   *
   * @return the {@code HttpTextFormat} for this implementation.
   * @since 0.1.0
   */
  HttpTextFormat<SpanContext> getHttpTextFormat();
}
