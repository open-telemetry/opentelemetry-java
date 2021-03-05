/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import static java.lang.Thread.currentThread;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.internal.shaded.WeakConcurrentMap;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link SpanProcessor} which will detect spans that are never ended. It will detect spans
 * that are garbage collected without ever having `end()` called on them.
 *
 * <p>Note: using this SpanProcessor will definitely impact the performance of your application. It
 * is not recommended for production use, as it uses additional memory for each span to track
 * where a leaked span was created.
 */
public final class LeakDetectingSpanProcessor implements SpanProcessor {
  private static final Logger logger = Logger.getLogger(LeakDetectingSpanProcessor.class.getName());

  private final PendingSpans pendingSpans;

  /**
   * Create a new {@link LeakDetectingSpanProcessor} that will report any un-ended spans that get
   * garbage collected.
   */
  public static LeakDetectingSpanProcessor create() {
    return new LeakDetectingSpanProcessor();
  }

  private LeakDetectingSpanProcessor() {
    this(
        (message, throwable) ->
            logger.log(Level.WARNING, "Span garbage collected before being ended.", throwable));
  }

  // Visible for testing
  LeakDetectingSpanProcessor(BiConsumer<String, Throwable> reporter) {
    pendingSpans = PendingSpans.create(reporter);
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    CallerStackTrace caller = new CallerStackTrace(span);
    StackTraceElement[] stackTrace = caller.getStackTrace();

    // take off the first 3 stack frames, as they are from the SDK itself.
    caller.setStackTrace(
        Arrays.copyOfRange(stackTrace, Math.min(3, stackTrace.length), stackTrace.length));

    pendingSpans.put(span.getSpanContext(), caller);
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    pendingSpans.remove(span.getSpanContext()).ended = true;
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  private static class PendingSpans extends WeakConcurrentMap<SpanContext, CallerStackTrace> {

    private final ConcurrentHashMap<WeakKey<SpanContext>, CallerStackTrace> map;
    private final BiConsumer<String, Throwable> reporter;

    @SuppressWarnings("ThreadPriorityCheck")
    private static PendingSpans create(BiConsumer<String, Throwable> reporter) {
      PendingSpans pendingSpans = new PendingSpans(new ConcurrentHashMap<>(), reporter);
      // Start cleaner thread ourselves to make sure it runs after initializing our fields.
      Thread thread = new Thread(pendingSpans);
      thread.setName("weak-ref-cleaner-leakingspandetector");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.setDaemon(true);
      thread.start();
      return pendingSpans;
    }

    private PendingSpans(
        ConcurrentHashMap<WeakKey<SpanContext>, CallerStackTrace> map,
        BiConsumer<String, Throwable> reporter) {
      super(/* cleanerThread= */ false, /* reuseKeys= */ false, map);
      this.map = map;
      this.reporter = reporter;
    }

    // Called by cleaner thread.
    @Override
    public void run() {
      try {
        while (!Thread.interrupted()) {
          // call blocks until something is GC'd.
          Reference<? extends SpanContext> gcdReference = remove();
          CallerStackTrace caller = map.remove(gcdReference);
          if (caller != null && !caller.ended) {
            reporter.accept("Span garbage collected before being ended.", callerError(caller));
          }
        }
      } catch (InterruptedException ignored) {
        // do nothing
      }
    }
  }

  private static class CallerStackTrace extends Throwable {

    private static final long serialVersionUID = 1234567896L;

    final String threadName = currentThread().getName();
    final String spanInformation;

    volatile boolean ended;

    CallerStackTrace(ReadableSpan span) {
      super("Thread [" + currentThread().getName() + "] started span : " + span + " here:");
      this.spanInformation = span.getName() + " [" + span.getSpanContext() + "]";
    }
  }

  private static AssertionError callerError(CallerStackTrace caller) {
    AssertionError toThrow =
        new AssertionError(
            "Span garbage collected before being ended. Thread: ["
                + caller.threadName
                + "] started span : "
                + caller.spanInformation
                + " here:");
    toThrow.setStackTrace(caller.getStackTrace());
    return toThrow;
  }
}
