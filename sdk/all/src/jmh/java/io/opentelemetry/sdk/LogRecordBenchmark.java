/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * This benchmark measures the performance of recording log records. It includes the following
 * dimensions:
 *
 * <ul>
 *   <li>{@link BenchmarkState#logSize}: the size of the log record, which is a composite of the
 *       number of attributes, length of body, and whether an attribute is attached to the log
 *       record.
 * </ul>
 *
 * <p>Each operation consists of recording {@link LogRecordBenchmark#RECORDS_PER_INVOCATION} log
 * records.
 *
 * <p>In order to isolate the record path while remaining realistic, the benchmark uses a {@link
 * BatchLogRecordProcessor} paired with a noop {@link LogRecordExporter}. In order to avoid quickly
 * outpacing the batch processor queue and dropping log records, the processor is configured with a
 * queue size of {@link LogRecordBenchmark#RECORDS_PER_INVOCATION} * {@link
 * LogRecordBenchmark#MAX_THREADS} and is flushed after each invocation.
 */
public class LogRecordBenchmark {

  private static final int RECORDS_PER_INVOCATION = BenchmarkUtils.RECORDS_PER_INVOCATION;
  private static final int MAX_THREADS = 4;
  private static final int QUEUE_SIZE = RECORDS_PER_INVOCATION * MAX_THREADS;

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    // The size of the log record, dictated by the number of attributes attached to it.
    @Param LogRecordSize logSize;

    SdkLoggerProvider loggerProvider;
    Logger logger;
    long timestampNanos = Clock.getDefault().now();
    List<AttributeKey<String>> attributeKeys;
    List<String> attributeValues;
    String body;
    @Nullable Throwable exception;

    @Setup
    public void setup() {
      loggerProvider =
          SdkLoggerProvider.builder()
              // Configure a batch processor with a noop exporter (LogRecordExporter.composite() is
              // a shortcut for a noop exporter). This allows testing the throughput / performance
              // impact of BatchLogRecordProcessor, which is essential for real workloads, while
              // avoiding noise from LogRecordExporters whose performance is subject to
              // implementation and network details.
              .addLogRecordProcessor(
                  BatchLogRecordProcessor.builder(LogRecordExporter.composite())
                      .setMaxQueueSize(QUEUE_SIZE)
                      .build())
              .build();
      logger = loggerProvider.get("benchmark");

      attributeKeys = new ArrayList<>(logSize.attributes);
      attributeValues = new ArrayList<>(logSize.attributes);
      for (int i = 0; i < logSize.attributes; i++) {
        attributeKeys.add(AttributeKey.stringKey("key" + i));
        attributeValues.add("value" + i);
      }

      body = randomString(logSize.bodyChars);

      exception = logSize.hasException ? new Exception("test exception") : null;
    }

    @TearDown(Level.Invocation)
    public void flush() {
      loggerProvider.forceFlush().join(10, TimeUnit.SECONDS);
    }

    @TearDown
    public void tearDown() {
      loggerProvider.shutdown();
    }
  }

  @Benchmark
  @Group("threads1")
  @GroupThreads(1)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  @OperationsPerInvocation(RECORDS_PER_INVOCATION)
  public void record_SingleThread(BenchmarkState benchmarkState) {
    record(benchmarkState);
  }

  @Benchmark
  @Group("threads" + MAX_THREADS)
  @GroupThreads(MAX_THREADS)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  @OperationsPerInvocation(RECORDS_PER_INVOCATION)
  public void record_MultipleThreads(BenchmarkState benchmarkState) {
    record(benchmarkState);
  }

  private static void record(BenchmarkState benchmarkState) {
    for (int i = 0; i < RECORDS_PER_INVOCATION; i++) {
      LogRecordBuilder builder =
          benchmarkState
              .logger
              .logRecordBuilder()
              .setTimestamp(benchmarkState.timestampNanos, TimeUnit.NANOSECONDS);
      for (int j = 0; j < benchmarkState.attributeKeys.size(); j++) {
        builder.setAttribute(
            benchmarkState.attributeKeys.get(j), benchmarkState.attributeValues.get(j));
      }
      if (benchmarkState.exception != null) {
        ((ExtendedLogRecordBuilder) builder).setException(benchmarkState.exception);
      }
      builder.setBody(benchmarkState.body);
      builder.emit();
    }
  }

  public enum LogRecordSize {
    SMALL(0, 0, /* hasException= */ false),
    MEDIUM(10, 100, /* hasException= */ false),
    LARGE(100, 10000, /* hasException= */ true);

    private final int attributes;
    private final int bodyChars;
    private final boolean hasException;

    LogRecordSize(int attributes, int bodyChars, boolean hasException) {
      this.attributes = attributes;
      this.bodyChars = bodyChars;
      this.hasException = hasException;
    }
  }

  private static String randomString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append((char) ('a' + (int) (Math.random() * 26)));
    }
    return sb.toString();
  }
}
