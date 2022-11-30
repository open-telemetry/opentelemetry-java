/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
 */
public final class SimpleLogRecordProcessor implements LogRecordProcessor {

  private static final Logger logger = Logger.getLogger(SimpleLogRecordProcessor.class.getName());

  private final LogRecordExporter logRecordExporter;
  private final Set<CompletableResultCode> pendingExports =
      Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

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
    return new SimpleLogRecordProcessor(exporter);
  }

  SimpleLogRecordProcessor(LogRecordExporter logRecordExporter) {
    this.logRecordExporter = requireNonNull(logRecordExporter, "logRecordExporter");
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    try {
      List<LogRecordData> logs = Collections.singletonList(logRecord.toLogRecordData());
      CompletableResultCode result = logRecordExporter.export(logs);
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

  @Override
  public String toString() {
    return "SimpleLogRecordProcessor{" + "logRecordExporter=" + logRecordExporter + '}';
  }
}
