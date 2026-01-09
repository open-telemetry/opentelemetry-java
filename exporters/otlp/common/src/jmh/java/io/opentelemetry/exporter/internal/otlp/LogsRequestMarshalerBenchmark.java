/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.logs.LowAllocationLogsRequestMarshaler;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class LogsRequestMarshalerBenchmark {

  private static final Collection<LogRecordData> LOGS;
  private static final LowAllocationLogsRequestMarshaler MARSHALER =
      new LowAllocationLogsRequestMarshaler();
  private static final TestOutputStream OUTPUT = new TestOutputStream();

  static {
    InMemoryLogRecordExporter logRecordExporter = InMemoryLogRecordExporter.create();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .setResource(
                Resource.create(
                    Attributes.builder()
                        .put(AttributeKey.booleanKey("key_bool"), true)
                        .put(AttributeKey.stringKey("key_string"), "string")
                        .put(AttributeKey.longKey("key_int"), 100L)
                        .put(AttributeKey.doubleKey("key_double"), 100.3)
                        .put(
                            AttributeKey.stringArrayKey("key_string_array"),
                            Arrays.asList("string", "string"))
                        .put(AttributeKey.longArrayKey("key_long_array"), Arrays.asList(12L, 23L))
                        .put(
                            AttributeKey.doubleArrayKey("key_double_array"),
                            Arrays.asList(12.3, 23.1))
                        .put(
                            AttributeKey.booleanArrayKey("key_boolean_array"),
                            Arrays.asList(true, false))
                        .build()))
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter))
            .build();

    Logger logger1 = loggerProvider.get("logger");
    logger1
        .logRecordBuilder()
        .setBody("Hello world from this log...")
        .setAllAttributes(
            Attributes.builder()
                .put("key_bool", true)
                .put("key_String", "string")
                .put("key_int", 100L)
                .put("key_double", 100.3)
                .build())
        .setSeverity(Severity.INFO)
        .setSeverityText("INFO")
        .emit();

    ((ExtendedLogger) logger1)
        .logRecordBuilder()
        .setEventName("namespace.my-event-name")
        .setBody(
            Value.of(
                KeyValue.of("stringKey", Value.of("value")),
                KeyValue.of("longKey", Value.of(1)),
                KeyValue.of("doubleKey", Value.of(1.0)),
                KeyValue.of("boolKey", Value.of(true)),
                KeyValue.of("stringArrKey", Value.of(Value.of("value1"), Value.of("value2"))),
                KeyValue.of("longArrKey", Value.of(Value.of(1), Value.of(2))),
                KeyValue.of("doubleArrKey", Value.of(Value.of(1.0), Value.of(2.0))),
                KeyValue.of("boolArrKey", Value.of(Value.of(true), Value.of(false))),
                KeyValue.of(
                    "key", Value.of(Collections.singletonMap("childKey1", Value.of("value"))))))
        .emit();

    LOGS = logRecordExporter.getFinishedLogRecordItems();
  }

  @Benchmark
  public int marshalStateful() throws IOException {
    LogsRequestMarshaler marshaler = LogsRequestMarshaler.create(LOGS);
    OUTPUT.reset();
    marshaler.writeBinaryTo(OUTPUT);
    return OUTPUT.getCount();
  }

  @Benchmark
  public int marshalStatefulJson() throws IOException {
    LogsRequestMarshaler marshaler = LogsRequestMarshaler.create(LOGS);
    OUTPUT.reset();
    marshaler.writeJsonTo(OUTPUT);
    return OUTPUT.getCount();
  }

  @Benchmark
  public int marshalStateless() throws IOException {
    MARSHALER.initialize(LOGS);
    try {
      OUTPUT.reset();
      MARSHALER.writeBinaryTo(OUTPUT);
      return OUTPUT.getCount();
    } finally {
      MARSHALER.reset();
    }
  }

  @Benchmark
  public int marshalStatelessJson() throws IOException {
    MARSHALER.initialize(LOGS);
    try {
      OUTPUT.reset();
      MARSHALER.writeJsonTo(OUTPUT);
      return OUTPUT.getCount();
    } finally {
      MARSHALER.reset();
    }
  }
}
