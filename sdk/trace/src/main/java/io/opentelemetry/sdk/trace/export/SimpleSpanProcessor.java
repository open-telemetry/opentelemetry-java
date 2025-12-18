/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
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

  private static final ComponentId COMPONENT_ID = ComponentId.generateLazy("simple_span_processor");

  private static final Logger logger = Logger.getLogger(SimpleSpanProcessor.class.getName());

  private final SpanExporter spanExporter;
  private final boolean exportUnsampledSpans;
  private final Set<CompletableResultCode> pendingExports =
      Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);
  private final SpanProcessorMetrics spanProcessorMetrics;

  private final Object exporterLock = new Object();

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
    return builder(exporter).build();
  }

  /**
   * Returns a new Builder for {@link SimpleSpanProcessor}.
   *
   * @since 1.34.0
   */
  public static SimpleSpanProcessorBuilder builder(SpanExporter exporter) {
    requireNonNull(exporter, "exporter");
    return new SimpleSpanProcessorBuilder(exporter);
  }

  SimpleSpanProcessor(
      SpanExporter spanExporter,
      boolean exportUnsampledSpans,
      Supplier<MeterProvider> meterProvider) {
    this.spanExporter = requireNonNull(spanExporter, "spanExporter");
    this.exportUnsampledSpans = exportUnsampledSpans;
    spanProcessorMetrics =
        SpanProcessorMetrics.get(InternalTelemetryVersion.LATEST, COMPONENT_ID, meterProvider);
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
    if (span != null && (exportUnsampledSpans || span.getSpanContext().isSampled())) {
      try {
        List<SpanData> spans = Collections.singletonList(span.toSpanData());
        CompletableResultCode result;

        synchronized (exporterLock) {
          result = spanExporter.export(spans);
        }

        pendingExports.add(result);
        result.whenComplete(
            () -> {
              pendingExports.remove(result);
              String error = null;
              if (!result.isSuccess()) {
                logger.log(Level.FINE, "Exporter failed");
                if (result.getFailureThrowable() != null) {
                  error = result.getFailureThrowable().getClass().getName();
                } else {
                  error = "export_failed";
                }
              }
              spanProcessorMetrics.finishSpans(1, error);
            });
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "Exporter threw an Exception", e);
      }
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
    CompletableResultCode result = new CompletableResultCode();

    CompletableResultCode flushResult = forceFlush();
    flushResult.whenComplete(
        () -> {
          CompletableResultCode shutdownResult = spanExporter.shutdown();
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

  /**
   * Return the processor's configured {@link SpanExporter}.
   *
   * @since 1.37.0
   */
  public SpanExporter getSpanExporter() {
    return spanExporter;
  }

  @Override
  public String toString() {
    return "SimpleSpanProcessor{"
        + "spanExporter="
        + spanExporter
        + ", exportUnsampledSpans="
        + exportUnsampledSpans
        + '}';
  }
}
