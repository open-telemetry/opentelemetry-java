/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.logs.Severity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class LogsBenchmarks {

  private static final Random RANDOM = new Random();

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    private final SdkLoggerProvider sdk =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                (context, logRecord) -> {
                  // Do nothing
                })
            .build();

    private List<String> loggerNames;

    @Setup
    public void setup() {
      int numLoggers = 100;
      loggerNames = new ArrayList<>(numLoggers);
      for (int i = 0; i < numLoggers; i++) {
        loggerNames.add(
            IntStream.range(0, 50)
                .mapToObj(unused -> String.valueOf((char) RANDOM.nextInt(26)))
                .collect(joining()));
      }
    }
  }

  /**
   * Simulates the behavior of a log appender implementation, which has to bridge logs from logging
   * frameworks (Log4j, Logback, etc). The name of the logger being bridged is used as the
   * OpenTelemetry logger name. Therefore, each log processed has to obtain a logger.
   */
  @Benchmark
  @Threads(1)
  public void emitSimpleLog(BenchmarkState benchmarkState) {
    String loggerName =
        benchmarkState.loggerNames.get(RANDOM.nextInt(benchmarkState.loggerNames.size()));
    benchmarkState
        .sdk
        .get(loggerName)
        .logRecordBuilder()
        .setBody("log message body")
        .setSeverity(Severity.DEBUG)
        .emit();
  }
}
