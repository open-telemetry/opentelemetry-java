/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LowAllocationLogRequestMarshalerTest {

  private static final AttributeKey<Boolean> KEY_BOOL = AttributeKey.booleanKey("key_bool");
  private static final AttributeKey<String> KEY_STRING = AttributeKey.stringKey("key_string");
  private static final AttributeKey<Long> KEY_INT = AttributeKey.longKey("key_int");
  private static final AttributeKey<Double> KEY_DOUBLE = AttributeKey.doubleKey("key_double");
  private static final AttributeKey<List<String>> KEY_STRING_ARRAY =
      AttributeKey.stringArrayKey("key_string_array");
  private static final AttributeKey<List<Long>> KEY_LONG_ARRAY =
      AttributeKey.longArrayKey("key_long_array");
  private static final AttributeKey<List<Double>> KEY_DOUBLE_ARRAY =
      AttributeKey.doubleArrayKey("key_double_array");
  private static final AttributeKey<List<Boolean>> KEY_BOOLEAN_ARRAY =
      AttributeKey.booleanArrayKey("key_boolean_array");
  private static final AttributeKey<Value<?>> KEY_BYTES = AttributeKey.valueKey("key_bytes");
  private static final AttributeKey<Value<?>> KEY_MAP = AttributeKey.valueKey("key_map");
  private static final AttributeKey<Value<?>> KEY_HETEROGENEOUS_ARRAY =
      AttributeKey.valueKey("key_heterogeneous_array");
  private static final AttributeKey<Value<?>> KEY_EMPTY = AttributeKey.valueKey("key_empty");
  private static final String BODY = "Hello world from this log...";

  private static final Resource RESOURCE =
      Resource.create(
          Attributes.builder()
              .put(KEY_BOOL, true)
              .put(KEY_STRING, "string")
              .put(KEY_INT, 100L)
              .put(KEY_DOUBLE, 100.3)
              .put(KEY_STRING_ARRAY, Arrays.asList("string", "string"))
              .put(KEY_LONG_ARRAY, Arrays.asList(12L, 23L))
              .put(KEY_DOUBLE_ARRAY, Arrays.asList(12.3, 23.1))
              .put(KEY_BOOLEAN_ARRAY, Arrays.asList(true, false))
              .put(KEY_BYTES, Value.of(new byte[] {1, 2, 3}))
              .put(KEY_MAP, Value.of(KeyValue.of("nested", Value.of("value"))))
              .put(KEY_HETEROGENEOUS_ARRAY, Value.of(Value.of("string"), Value.of(123L)))
              .put(KEY_EMPTY, Value.empty())
              .build());

  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("name");
  private static final String TRACE_ID = "7b2e170db4df2d593ddb4ddf2ddf2d59";
  private static final String SPAN_ID = "170d3ddb4d23e81f";
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault());

  private final List<LogRecordData> logRecordDataList = createLogRecordDataList();

  private static List<LogRecordData> createLogRecordDataList() {
    List<LogRecordData> logRecordDataList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      logRecordDataList.add(createLogRecordData());
    }
    return logRecordDataList;
  }

  private static LogRecordData createLogRecordData() {
    return TestLogRecordData.builder()
        .setResource(RESOURCE)
        .setInstrumentationScopeInfo(INSTRUMENTATION_SCOPE_INFO)
        .setBody(BODY)
        .setSeverity(Severity.INFO)
        .setSeverityText("INFO")
        .setSpanContext(SPAN_CONTEXT)
        .setAttributes(
            Attributes.builder()
                .put(KEY_BOOL, true)
                .put(KEY_STRING, "string")
                .put(KEY_INT, 100L)
                .put(KEY_DOUBLE, 100.3)
                .build())
        .setTotalAttributeCount(2)
        .setTimestamp(12345, TimeUnit.NANOSECONDS)
        .setObservedTimestamp(6789, TimeUnit.NANOSECONDS)
        .build();
  }

  @Test
  void validateOutput() throws Exception {
    byte[] result;
    {
      LogsRequestMarshaler requestMarshaler = LogsRequestMarshaler.create(logRecordDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeBinaryTo(customOutput);
      result = customOutput.toByteArray();
    }

    byte[] lowAllocationResult;
    {
      LowAllocationLogsRequestMarshaler requestMarshaler = new LowAllocationLogsRequestMarshaler();
      requestMarshaler.initialize(logRecordDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeBinaryTo(customOutput);
      lowAllocationResult = customOutput.toByteArray();
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }

  @Test
  void validateJsonOutput() throws Exception {
    String result;
    {
      LogsRequestMarshaler requestMarshaler = LogsRequestMarshaler.create(logRecordDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeJsonTo(customOutput);
      result = new String(customOutput.toByteArray(), StandardCharsets.UTF_8);
    }

    String lowAllocationResult;
    {
      LowAllocationLogsRequestMarshaler requestMarshaler = new LowAllocationLogsRequestMarshaler();
      requestMarshaler.initialize(logRecordDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeJsonTo(customOutput);
      lowAllocationResult = new String(customOutput.toByteArray(), StandardCharsets.UTF_8);
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }
}
