/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the {@link SpanProcessor} that converts the {@link ReadableSpan} to {@link
 * SpanData} and passes it directly to the configured exporter.
 *
 * <p>This processor will cause all spans to be exported directly as they finish, meaning each
 * export request will have a single span. Most backends will not perform well with a single span
 * per request so unless you know what you're doing, strongly consider using {@link
 * BatchSpanProcessor} instead, including in special environments such as serverless runtimes.
 * {@link SimpleSpanProcessor} is generally meant to for logging exporters only.
 */
public final class SimpleSpanProcessor implements SpanProcessor {

  private static final Logger logger = Logger.getLogger(SimpleSpanProcessor.class.getName());

  private final SpanExporter spanExporter;
  private final boolean sampled;
  private final Set<CompletableResultCode> pendingExports =
      Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Returns a new {@link SimpleSpanProcessor} which exports spans to the {@link SpanExporter}
   * synchronously.
   *
   * <p>This processor will cause all spans to be exported directly as they finish, meaning each
   * export request will have a single span. Most backends will not perform well with a single span
   * per request so unless you know what you're doing, strongly consider using {@link
   * BatchSpanProcessor} instead, including in special environments such as serverless runtimes.
   * {@link SimpleSpanProcessor} is generally meant to for logging exporters only.
   */
  public static SpanProcessor create(SpanExporter exporter) {
    requireNonNull(exporter, "exporter");
    return new SimpleSpanProcessor(exporter, /* sampled= */ true);
  }

  SimpleSpanProcessor(SpanExporter spanExporter, boolean sampled) {
    this.spanExporter = requireNonNull(spanExporter, "spanExporter");
    this.sampled = sampled;
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    // Do nothing.
  }

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    if (sampled && !span.getSpanContext().isSampled()) {
      return;
    }
    try {
      List<SpanData> spans = Collections.singletonList(span.toSpanData());
      final CompletableResultCode result = spanExporter.export(spans);
      pendingExports.add(result);
      result.whenComplete(
          () -> {
            pendingExports.remove(result);
            if (!result.isSuccess()) {
              logger.log(Level.FINE, "Exporter failed");
            }
          });
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "Exporter threw an Exception", e);
    }
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    final CompletableResultCode result = new CompletableResultCode();

    final CompletableResultCode flushResult = forceFlush();
    flushResult.whenComplete(
        () -> {
          final CompletableResultCode shutdownResult = spanExporter.shutdown();
          shutdownResult.whenComplete(
              () -> {
                if (!flushResult.isSuccess() || !shutdownResult.isSuccess()) {
                  result.fail();
                } else {
                  result.succeed();
                }
              });
        });

    return result;
  }

  @Override
  public CompletableResultCode forceFlush() {
    return CompletableResultCode.ofAll(pendingExports);
  }
}
