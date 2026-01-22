/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the {@link LogRecordProcessor} that passes {@link LogRecordData} directly to
 * the configured exporter.
 *
 * <p>This processor will cause all logs to be exported directly as they finish, meaning each export
 * request will have a single log. Most backends will not perform well with a single log per request
 * so unless you know what you're doing, strongly consider using {@link BatchLogRecordProcessor}
 * instead, including in special environments such as serverless runtimes. {@link
 * SimpleLogRecordProcessor} is generally meant to for testing only.
 *
 * @since 1.27.0
 */
public final class SimpleLogRecordProcessor implements LogRecordProcessor {

  private static final ComponentId COMPONENT_ID = ComponentId.generateLazy("simple_log_processor");

  private static final Logger logger = Logger.getLogger(SimpleLogRecordProcessor.class.getName());

  private final LogRecordExporter logRecordExporter;
  private final Set<CompletableResultCode> pendingExports =
      Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);
  private final LogRecordProcessorInstrumentation logProcessorInstrumentation;

  private final Object exporterLock = new Object();

  /**
   * Returns a new {@link SimpleLogRecordProcessor} which exports logs to the {@link
   * LogRecordExporter} synchronously.
   *
   * <p>This processor will cause all logs to be exported directly as they finish, meaning each
   * export request will have a single log. Most backends will not perform well with a single log
   * per request so unless you know what you're doing, strongly consider using {@link
   * BatchLogRecordProcessor} instead, including in special environments such as serverless
   * runtimes. {@link SimpleLogRecordProcessor} is generally meant to for testing only.
   */
  public static LogRecordProcessor create(LogRecordExporter exporter) {
    requireNonNull(exporter, "exporter");
    return builder(exporter).build();
  }

  /**
   * Returns a new Builder for {@link SimpleLogRecordProcessor}.
   *
   * @since 1.58.0
   */
  public static SimpleLogRecordProcessorBuilder builder(LogRecordExporter exporter) {
    requireNonNull(exporter, "exporter");
    return new SimpleLogRecordProcessorBuilder(exporter);
  }

  SimpleLogRecordProcessor(
      LogRecordExporter logRecordExporter, Supplier<MeterProvider> meterProvider) {
    this.logRecordExporter = requireNonNull(logRecordExporter, "logRecordExporter");
    logProcessorInstrumentation =
        LogRecordProcessorInstrumentation.get(
            InternalTelemetryVersion.LATEST, COMPONENT_ID, meterProvider);
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    try {
      List<LogRecordData> logs = Collections.singletonList(logRecord.toLogRecordData());
      CompletableResultCode result;

      synchronized (exporterLock) {
        result = logRecordExporter.export(logs);
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
            logProcessorInstrumentation.finishLogs(1, error);
          });
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "Exporter threw an Exception", e);
    }
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
          CompletableResultCode shutdownResult = logRecordExporter.shutdown();
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
   * Return the processor's configured {@link LogRecordExporter}.
   *
   * @since 1.37.0
   */
  public LogRecordExporter getLogRecordExporter() {
    return logRecordExporter;
  }

  @Override
  public String toString() {
    return "SimpleLogRecordProcessor{" + "logRecordExporter=" + logRecordExporter + '}';
  }
}
